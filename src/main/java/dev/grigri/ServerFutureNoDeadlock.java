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
                    var request = new SendCardDetailsRequest(socket);
                    sendCombinedCardDetails(request, r.tokenPAN(), r.tokenExpDate(), r.tokenHolderName());
                } catch (IOException | InterruptedException | ExecutionException e) {
                    handleError(e);
                }
            });
        }
    }

    void sendCombinedCardDetails(SendCardDetailsRequest request, Token tokenPAN, Token tokenExpDate, Token tokenHolderName) throws IOException, InterruptedException, ExecutionException {
        Future<String> futurePAN = es2.submit(() -> detokenize(tokenPAN));
        Future<String> futureExpDate = es2.submit(() -> detokenize(tokenExpDate));
        Future<String> futureHolderName = es2.submit(() -> detokenize(tokenHolderName));

        request.setPAN(futurePAN.get())
                .setExpDate(futureExpDate.get())
                .setHolderName(futureHolderName.get())
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