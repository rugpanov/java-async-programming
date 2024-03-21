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
                    var request = new SendCardDetailsRequest(socket);
                    sendCombinedCardDetails(request, r.tokenPAN(), r.tokenExpDate(), r.tokenHolderName());
                } catch (IOException | InterruptedException e) {
                    handleError(e);
                }
            });
        }
    }

    void sendCombinedCardDetails(SendCardDetailsRequest request, Token tokenPAN, Token tokenExpDate, Token tokenHolderName) throws IOException, InterruptedException {
        Thread t1 = new Thread(() -> request.setPAN(detokenize(tokenPAN)));
        Thread t2 = new Thread(() -> request.setExpDate(detokenize(tokenExpDate)));
        Thread t3 = new Thread(() -> request.setHolderName(detokenize(tokenHolderName)));
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