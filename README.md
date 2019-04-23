# COP5618
Concurrent Programming Project

Project Topic: The objective of the project is to perform a qualitative and quantitative comparison of two different coding languages: GoLang and Java. Go and Java offer two different concurrent coding philosophies. Go provides an abstraction called channels while Java offers the basic thread and shared variables paradigm. By implmenting a multiclient chat room, the project is able to explore multiple characteristics of concurrency and provides a good method to compare both languages. 

Project Members - GitID: 
Derek Wu - DerekJongWu
Ryan Clements - RyanClementsHax
Aswin Suresh Krishnan - aswinsureshk

Group Member Contributions: 
Ryan Clements - GoLang Chat Room implementation 
Derek Wu - Java Chat Room implementation
Aswin Suresh Krishnan - Test cases for both GoLang and Java 

Instructions to compile and run code and tests
-----------------------------------------------

GO
----

To run go code: 

1.  start server : cd COP5618/Go/cd server
                   go server.go
2.  start client : cd COP5618/Go/client
                   go client.go

To run go test:

cd COP5618/Go
go test-v

Java
------

To compile java code:

cd COP5618/Java/MultiThreaded\ Chat

javac -cp ../lib/junit-4.12.jar  multichat/*.java

To run java code:

cd COP5618/Java/MultiThreaded\ Chat

1. start server : java multichat.ChatServer 23001

2. start client : java multichat.ChatClient 127.0.0.1 23001

To run java test:

java -cp .:../lib/junit-4.12.jar:../lib/hamcrest-all-1.3.jar org.junit.runner.JUnitCore multichat.ChatTest



*All members were involved in code reiview and poster creation 
 
Instructions for compilation are found in the README.md in each folder. 
