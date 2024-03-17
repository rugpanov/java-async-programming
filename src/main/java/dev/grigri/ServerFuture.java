package dev.grigri;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerFuture {

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
                } catch (IOException | InterruptedException | ExecutionException e) {
                    handleError(e);
                }
            });
        }
    }

    void sendCombinedCardDetails(SendCardDetailsRequest request, Token tokenPAN, Token tokenExpDate, Token tokenHolderName) throws IOException, InterruptedException, ExecutionException {
        Future<String> futurePAN = es.submit(() -> detokenize(tokenPAN));
        Future<String> futureExpDate = es.submit(() -> detokenize(tokenExpDate));
        Future<String> futureHolderName = es.submit(() -> detokenize(tokenHolderName));

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