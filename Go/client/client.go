package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"
)

// starts listening for messages from the socket
func listenForMessages(reader *bufio.Reader, chat chan string) {
	for {
		// gets message
		msg, err := reader.ReadString('\n')
		// handles error
		if err != nil {
			fmt.Println(err)
			return
		}
		// publishes message
		chat <- msg
	}
}

// listens for input from the console
func handleInput(scanner *bufio.Scanner, msg chan string) {
	for scanner.Scan() {
		// gets message
		m := scanner.Text()
		// publishes message
		msg <- m
	}
}

// used in the test folder
func handleDataInput(data []string, msg chan string) {
	for i := 0; i < len(data); i++ {
		m := data[i]
		msg <- m
	}
}

// starts client
func main() {
	// makes channel
	msg := make(chan string, 1)
	chat := make(chan string, 1)
	sigs := make(chan os.Signal, 1)
	// configures to listen for the cancel signals
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

	// connects to server
	conn, _ := net.Dial("tcp", "localhost:3000")
	// makes readers and writers
	scanner := bufio.NewScanner(os.Stdin)
	reader := bufio.NewReader(conn)

	// starts goroutines for handling console and server messages
	go handleInput(scanner, msg)
	go listenForMessages(reader, chat)

	// switches on the different signals
loop:
	for {
		select {
		case <-sigs:
			// we need to cancel
			fmt.Println("Closing")
			break loop
		case s := <-msg:
			// we got a message from the console so we send to server
			fmt.Fprintf(conn, s+"\n")
		case c := <-chat:
			// we got a message from the server so we send to console
			fmt.Print("> " + c)
		}
	}
}
