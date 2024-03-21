package dev.grigri;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerPlatformThreads {

    private final ExecutorService es = Executors.newFixedThreadPool(256);

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);

            es.submit(() -> {
                try {
                    var request = new SendDetokenizedDetailsRequest(socket);
                    processDetokenizedDetails(request, r.tokenName(), r.tokenSurname(), r.tokenEmail());
                } catch (IOException | InterruptedException e) {
                    handleError(e);
                }
            });
        }
    }

    void processDetokenizedDetails(SendDetokenizedDetailsRequest request, Token tokenName, Token tokenSurname, Token tokenEmail) throws IOException, InterruptedException {
        Thread t1 = new Thread(() -> request.setName(detokenize(tokenName)));
        Thread t2 = new Thread(() -> request.setSurname(detokenize(tokenSurname)));
        Thread t3 = new Thread(() -> request.setEmail(detokenize(tokenEmail)));
        t1.start(); t2.start(); t3.start();

        t1.join(); t2.join(); t3.join();
        request.send();
    }

    //mock
    private String detokenize(Token token) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return token.token();
    }

    //mock
    private void handleError(Exception e) {
        e.printStackTrace();
    }
}