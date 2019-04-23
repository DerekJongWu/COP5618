package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"
)

// to represent each of the clients
type Client struct {
	incoming chan string
	outgoing chan string
	reader   *bufio.Reader
	writer   *bufio.Writer
}

// handles reading the messages from the client socket
func (client *Client) Read() {
	for {
		// reads the string
		line, err := client.reader.ReadString('\n')
		// handles error
		if err != nil {
			fmt.Println("A connection closed")
			return
		}
		// publishes message on its incoming channel
		client.incoming <- line
	}
}

// handles writing messages to the client socket
func (client *Client) Write() {
	// for each message in the outgoing channel
	for data := range client.outgoing {
		// write the message to the buffer and flush
		client.writer.WriteString(data)
		client.writer.Flush()
	}
}

// starts the read and write goroutines
func (client *Client) Listen() {
	go client.Read()
	go client.Write()
}

// makes a new client object
func NewClient(connection net.Conn) *Client {
	// makes new reader and writer objects based on the socket
	writer := bufio.NewWriter(connection)
	reader := bufio.NewReader(connection)

	// makes the new client object
	client := &Client{
		incoming: make(chan string),
		outgoing: make(chan string),
		reader:   reader,
		writer:   writer,
	}

	// starts the listening
	client.Listen()

	return client
}

// object to control the chatroom
type ChatRoom struct {
	clients  []*Client
	joins    chan net.Conn
	incoming chan string
}

// broadcasts a message by putting it in all of the clients' outgoing channels
func (chatRoom *ChatRoom) Broadcast(data string) {
	for _, client := range chatRoom.clients {
		client.outgoing <- data
	}
}

// puts the client in the chatroom
func (chatRoom *ChatRoom) Join(connection net.Conn) {
	// makes a new client
	client := NewClient(connection)
	// appends it in the list
	chatRoom.clients = append(chatRoom.clients, client)
	// starts a goroutine to configure the incoming messages to be put in the chatroom's incoming
	// messages to broadcast
	go func() {
		for {
			chatRoom.incoming <- <-client.incoming
		}
	}()
}

// starts the chatroom
func (chatRoom *ChatRoom) Listen() {
	go func() {
		for {
			select {
			case data := <-chatRoom.incoming:
				// broadcasts message
				chatRoom.Broadcast(data)
			case conn := <-chatRoom.joins:
				// adds client to the room
				chatRoom.Join(conn)
			}
		}
	}()
}

// makes new chatroom
func NewChatRoom() *ChatRoom {
	// new's up object
	chatRoom := &ChatRoom{
		clients:  make([]*Client, 0),
		joins:    make(chan net.Conn),
		incoming: make(chan string),
	}

	// starts listening
	chatRoom.Listen()

	return chatRoom
}

// accepts client connections
func Listen(listener net.Listener, connections chan net.Conn) {
	for {
		// accepts a connection
		conn, err := listener.Accept()
		// handles error
		if err != nil {
			fmt.Println(err)
			return
		}
		// adds new connection into the channel
		connections <- conn
	}
}

func main() {
	// makes channels
	connections := make(chan net.Conn, 1)
	sigs := make(chan os.Signal, 1)
	// configures sigs channel to publish sigint and sigterm signals
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)
	// makes new chatroom
	chatRoom := NewChatRoom()

	// starts listening socket
	listener, err := net.Listen("tcp", ":3000")
	if err != nil {
		fmt.Println(err)
		return
	}
	defer listener.Close()

	// starts listening for clients
	go Listen(listener, connections)

	// accepts connections and listens for cancel signals
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
