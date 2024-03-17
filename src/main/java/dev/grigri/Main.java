package dev.grigri;

import java.time.Duration;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        Instant start = Instant.now();
        ThreadsBenchmarksUtils.startManyVirtualThreads(100_000);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Time elapsed to start virtual threads: " + timeElapsed + "ms");

        start = Instant.now();
        try {
            ThreadsBenchmarksUtils.startManyThreads(100_000);
        } catch (OutOfMemoryError e) {
            finish = Instant.now();
            timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("Time elapsed to start virtual threads: " + timeElapsed + "ms");
        }
    }
}