package chattest

import (
	"bufio"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"
)

type Client struct {
	incoming chan string
	outgoing chan string
	reader   *bufio.Reader
	writer   *bufio.Writer
}

func (client *Client) Read() {
	for {
		line, err := client.reader.ReadString('\n')
		if err != nil {
			fmt.Println("A connection closed")
			return
		}
		client.incoming <- line
	}
}

func (client *Client) Write() {
	for data := range client.outgoing {
		client.writer.WriteString(data)
		client.writer.Flush()
	}
}

func (client *Client) Listen() {
	go client.Read()
	go client.Write()
}

func NewClient(connection net.Conn) *Client {
	writer := bufio.NewWriter(connection)
	reader := bufio.NewReader(connection)

	client := &Client{
		incoming: make(chan string),
		outgoing: make(chan string),
		reader:   reader,
		writer:   writer,
	}

	client.Listen()

	return client
}

type ChatRoom struct {
	clients  []*Client
	joins    chan net.Conn
	incoming chan string
}

func (chatRoom *ChatRoom) Broadcast(data string) {
	for _, client := range chatRoom.clients {
		client.outgoing <- data
	}
}

func (chatRoom *ChatRoom) Join(connection net.Conn) {
	client := NewClient(connection)
	chatRoom.clients = append(chatRoom.clients, client)
	go func() {
		for {
			chatRoom.incoming <- <-client.incoming
		}
	}()
}

func (chatRoom *ChatRoom) Listen() {
	go func() {
		for {
			select {
			case data := <-chatRoom.incoming:
				chatRoom.Broadcast(data)
			case conn := <-chatRoom.joins:
				chatRoom.Join(conn)
			}
		}
	}()
}

func NewChatRoom() *ChatRoom {
	chatRoom := &ChatRoom{
		clients:  make([]*Client, 0),
		joins:    make(chan net.Conn),
		incoming: make(chan string),
	}

	chatRoom.Listen()

	return chatRoom
}

func Listen(listener net.Listener, connections chan net.Conn) {
	for {
		conn, err := listener.Accept()
		if err != nil {
			fmt.Println(err)
			return
		}
		connections <- conn
	}
}

func startServer() {
	connections := make(chan net.Conn, 1)
	sigs := make(chan os.Signal, 1)
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)
	chatRoom := NewChatRoom()

	listener, err := net.Listen("tcp", ":3000")
	if err != nil {
		fmt.Println(err)
		return
	}
	defer listener.Close()

	go Listen(listener, connections)

	for {
		select {
		case <-sigs:
			fmt.Println("Closing")
			return
		case conn := <-connections:
			fmt.Println("Accepted connection")
			chatRoom.joins <- conn
		}
	}
}
