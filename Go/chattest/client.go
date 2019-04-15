package chattest

import (
	"bufio"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"
)

func listenForMessages(reader *bufio.Reader, chat chan string) {
	for {
		msg, err := reader.ReadString('\n')
		if err != nil {
			fmt.Println(err)
			return
		}
		chat <- msg
	}
}

func handleInput(scanner *bufio.Scanner, msg chan string) {
	for scanner.Scan() {
		m := scanner.Text()
		msg <- m
	}
}

func handleDataInput(data []string, msg chan string, done chan bool) {
	for i := 0; i < len(data); i++ {
		m := data[i]
		msg <- m
		done <- true
	}
	if data == nil {
		done <- true
	}
}

func startClient(data []string, done chan bool) {
	msg := make(chan string, 1)
	chat := make(chan string, 1)
	sigs := make(chan os.Signal, 1)
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

	conn, _ := net.Dial("tcp", "localhost:3000")
	// scanner := bufio.NewScanner(os.Stdin)
	reader := bufio.NewReader(conn)

	// go handleInput(scanner, msg)
	go listenForMessages(reader, chat)
	go handleDataInput(data, msg, done)

	// loop:
	// 	for {
	// 		select {
	// 		case <-sigs:
	// 			fmt.Println("Closing")
	// 			break loop
	// 		case s := <-msg:
	// 			fmt.Fprintf(conn, s+"\n")
	// 		case c := <-chat:
	// 			fmt.Print("> " + c)
	// 		}
	// 	}
}
