package dev.grigri;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

public class ServerCompletableFuture {

    private final ExecutorService es1 = Executors.newFixedThreadPool(128);
    private final ExecutorService es2 = Executors.newFixedThreadPool(256);

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);

            var request = new SendDetokenizedDetailsRequest(socket);
            es1.submit(() -> processDetokenizedDetails(request, r.tokenName(), r.tokenSurname(), r.tokenEmail()));
        }
    }

    void processDetokenizedDetails(SendDetokenizedDetailsRequest request, Token tokenName, Token tokenSurname, Token tokenEmail) {
        var futureName = CompletableFuture.supplyAsync(() -> detokenize(tokenName), es2);
        var futureSurname = CompletableFuture.supplyAsync(() -> detokenize(tokenSurname), es2);
        var futureEmail = CompletableFuture.supplyAsync(() -> detokenize(tokenEmail), es2);

        futureName.exceptionally(e -> {
            handleError(e);
            return null;
        }).thenAccept(name ->
                futureSurname.exceptionally(e -> {
                    handleError(e);
                    return null;
                }).thenAccept(surname ->
                        futureEmail.exceptionally(e -> {
                            handleError(e);
                            return null;
                        }).thenAccept(email ->
                                {
                                    try {
                                        request.setName(name)
                                                .setSurname(surname)
                                                .setEmail(email)
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