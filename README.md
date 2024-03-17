# Asynchronous Programming in Java: The Era of Virtual Threads

Welcome to the showcase code for the Talk "Asynchronous Programming in Java: The Era of Virtual Threads."
This presentation aims to explore the evolution and application of asynchronous programming patterns in
Java, highlighting the transition to the use of virtual threads.

## Table of content

1. [ServerNoAsync](src/main/java/dev/grigri/ServerNoAsync.java): Implementation of a server without asynchronous programming techniques.
2. [ServerPlatformThreads](src/main/java/dev/grigri/ServerPlatformThreads.java): Demonstration of a server utilizing platform threads.
3. [ServerFuture](src/main/java/dev/grigri/ServerFuture.java): Server example using Future to handle asynchronous tasks.
4. [ServerFutureNoDeadlock](src/main/java/dev/grigri/ServerFutureNoDeadlock.java): Enhanced server example with Future, designed to avoid deadlock scenarios.
5. [ServerCompletableFuture](src/main/java/dev/grigri/ServerCompletableFuture.java): Server implementation leveraging CompletableFuture for asynchronous programming.
6. [ServerParSeq](src/main/java/dev/grigri/ServerParSeq.java): Example of a server utilizing ParSeq for managing parallel sequences of asynchronous operations.
7. [ServerVirtualThreads](src/main/java/dev/grigri/ServerVirtualThreads.java): Introduction to the use of virtual threads in server implementation for improved scalability and efficiency in handling concurrent tasks.
