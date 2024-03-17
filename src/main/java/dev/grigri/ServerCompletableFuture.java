package dev.grigri;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class ServerCompletableFuture {

    private final ExecutorService es1 = Executors.newFixedThreadPool(128);
    private final ExecutorService es2 = Executors.newFixedThreadPool(256);

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);

            var request = new SendCardDetailsRequest(socket);
            es1.submit(() -> sendCombinedCardDetails(request, r.tokenPAN(), r.tokenExpDate(), r.tokenHolderName()));
        }
    }

    void sendCombinedCardDetails(SendCardDetailsRequest request, Token tokenPAN, Token tokenExpDate, Token tokenHolderName) {
        var futurePAN = CompletableFuture.supplyAsync(() -> detokenize(tokenPAN), es2);
        var futureExpDate = CompletableFuture.supplyAsync(() -> detokenize(tokenExpDate), es2);
        var futureHolderName = CompletableFuture.supplyAsync(() -> detokenize(tokenHolderName), es2);

        futurePAN.exceptionally(e -> {
            handleError(e);
            return null;
        }).thenAccept(pan ->
                futureExpDate.exceptionally(e -> {
                    handleError(e);
                    return null;
                }).thenAccept(expDate ->
                        futureHolderName.exceptionally(e -> {
                            handleError(e);
                            return null;
                        }).thenAccept(holderName ->
                                {
                                    try {
                                        request.setPAN(pan)
                                                .setExpDate(expDate)
                                                .setHolderName(holderName)
                                                .send();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                )
        );
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
    private void handleError(Throwable e) {
        e.printStackTrace();
    }
}