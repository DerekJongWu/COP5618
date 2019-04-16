package chattest

import (
	"fmt"
	"math/rand"
	"os"
	"testing"
	"time"
)

//execution order for a test
func TestMain(m *testing.M) {
	mySetupFunction()
	retCode := m.Run()
	myTeardownFunction()
	os.Exit(retCode)
}

//start server before test
func mySetupFunction() {
	go startServer()
}

//nothing to do after test
func myTeardownFunction() {

}

//utitlity func to assert actual vs expected for a test
func assertEquals(t *testing.T, actual, expected int) {
	t.Helper()
	if actual != expected {
		t.Errorf("actual '%d' expected '%d'", actual, expected)
	}
}

//Two clients are created. First client sends a message to the server which then broadcasts to the second client
//Both clients exit after this and the broadcast latency is recorded
func Test1ServerBroadcastLatency(t *testing.T) {

	start := time.Now()
	data := make([]string, 2)
	data[0] = "[bye]"//single datapoint that will be used
	done := make(chan bool, 2)
	//start both the clients
	go startClient(data, done)
	go startClient(nil, done)
	count := 0
	//wait for the clients to exit
	for i := 1; i <= 2; i++ {
		<-done
		count = count + 1
	}
	assertEquals(t, count, 2)
	elapsed := time.Since(start)
	//elapsed time depicts the server broadcast latency
	fmt.Println(elapsed)

}

/* 'numclients(=100)' clients are created. Each clients sends 'datapoints(=2)' messages to the server which then broadcasts the msg to 
the remaining clients. All the clients exit after this process and we measure the time recorded to be an indicator 
of the chatroom application's bandwidth */
func Test2ClientChatUsingMultipleClients(t *testing.T) {

	//code to create random string for the datapoint
	const charset = "abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

	const numclients = 100
	const datapoints = 2

	var seededRand = rand.New(
		rand.NewSource(time.Now().UnixNano()))

	stringmaker := func(length int, charset string) string {
		b := make([]byte, length)
		for i := range b {
			b[i] = charset[seededRand.Intn(len(charset))]
		}
		return string(b)
	}
	getstring := func(length int) string {
		return stringmaker(length, charset)
	}

	start := time.Now()
	data := make([]string, datapoints)
	done := make(chan bool, numclients)
	//start all the clients and pass the two msgs to each of them to broadcast
	for i := 0; i < numclients; i++ {
		for j := 0; j < datapoints; j++ {
			data[j] = getstring(10)
		}
		go startClient(data, done)
	}
	//wait for all the clients to finish
	count := 0
	for i := 1; i <= numclients; i++ {
		<-done
		count = count + 1
	}
	assertEquals(t, count, numclients)
	elapsed := time.Since(start)
	//measure the elapsed time
	fmt.Println(elapsed)
}
