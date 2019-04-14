package chattest

import (
	"fmt"
	"math/rand"
	"testing"
	"time"
)

func assertEquals(t *testing.T, actual, expected int) {
	t.Helper()
	if actual != expected {
		t.Errorf("actual '%d' expected '%d'", actual, expected)
	}
}

func Test_1_testServerBroadcastLatency(t *testing.T) {

	const charset = "abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

	const numclients = 1000
	const datapoints = 100

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
	go startServer()
	data := make([]string, 100)
	// msg := make(chan string, 1)
	for i := 0; i < numclients; i++ {
		for i := 0; i < datapoints; i++ {
			data[i] = getstring(10)
		}
		go startClient(data)
	}
	elapsed := time.Since(start)
	fmt.Println(elapsed)
}
