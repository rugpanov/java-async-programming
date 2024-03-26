package dev.grigri;

public class ThreadsBenchmarksUtils {
    public static void startManyPlatformThreads(int nThreads) {
        for (int i = 0; i < nThreads; i++) {
            System.out.println("Starting thread " + i);
            new Thread(() -> {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void startManyVirtualThreads(int nThreads) {
        for (int i = 0; i < nThreads; i++) {
            System.out.println("Starting virtual thread " + i);
            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
