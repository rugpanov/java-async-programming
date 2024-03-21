package dev.grigri;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerFutureNoDeadlock {

    private final ExecutorService es1 = Executors.newFixedThreadPool(128);
    private final ExecutorService es2 = Executors.newFixedThreadPool(256);

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);

            es1.submit(() -> {
                try {
                    var request = new SendDetokenizedDetailsRequest(socket);
                    processDetokenizedDetails(request, r.tokenName(), r.tokenSurname(), r.tokenEmail());
                } catch (IOException | InterruptedException | ExecutionException e) {
                    handleError(e);
                }
            });
        }
    }

    void processDetokenizedDetails(SendDetokenizedDetailsRequest request, Token tokenName, Token tokenSurname, Token tokenEmail) throws IOException, InterruptedException, ExecutionException {
        Future<String> futureName = es2.submit(() -> detokenize(tokenName));
        Future<String> futureSurname = es2.submit(() -> detokenize(tokenSurname));
        Future<String> futureEmail = es2.submit(() -> detokenize(tokenEmail));

        request.setName(futureName.get())
                .setSurname(futureSurname.get())
                .setEmail(futureEmail.get())
                .send();
    }

    //mock
    private String detokenize(Token token) throws InterruptedException {
        Thread.sleep(1000);
        return token.token();
    }

    //mock
    private void handleError(Exception e) {
        e.printStackTrace();
    }
}