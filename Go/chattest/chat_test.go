package chattest

import (
	"fmt"
	"math/rand"
	"os"
	"testing"
	"time"
)

func TestMain(m *testing.M) {
	mySetupFunction()
	retCode := m.Run()
	myTeardownFunction()
	os.Exit(retCode)
}

func mySetupFunction() {
	go startServer()
}

func myTeardownFunction() {

}

func assertEquals(t *testing.T, actual, expected int) {
	t.Helper()
	if actual != expected {
		t.Errorf("actual '%d' expected '%d'", actual, expected)
	}
}

func Test1ServerBroadcastLatency(t *testing.T) {

	start := time.Now()
	data := make([]string, 2)
	data[0] = "[bye]"
	done := make(chan bool, 2)

	go startClient(data, done)
	go startClient(nil, done)
	count := 0
	for i := 1; i <= 2; i++ {
		<-done
		count = count + 1
	}
	assertEquals(t, count, 2)
	elapsed := time.Since(start)
	fmt.Println(elapsed)

}

func Test2ClientChatUsingMultipleClients(t *testing.T) {

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
	// msg := make(chan string, 1)
	for i := 0; i < numclients; i++ {
		for j := 0; j < datapoints; j++ {
			data[j] = getstring(10)
		}
		go startClient(data, done)
	}
	count := 0
	for i := 1; i <= numclients; i++ {
		<-done
		count = count + 1
	}
	assertEquals(t, count, numclients)
	elapsed := time.Since(start)
	fmt.Println(elapsed)
}
