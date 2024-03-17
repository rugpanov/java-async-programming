package dev.grigri;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerNoAsync server = new ServerNoAsync();
        server.run();
    }
}