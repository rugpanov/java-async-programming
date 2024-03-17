package dev.grigri;

import com.linkedin.parseq.Task;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

public class ServerVirtualThreads {

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);
            Thread.ofVirtual().start(() -> {
                try {
                    var request = new SendCardDetailsRequest(socket);
                    sendCombinedCardDetails(request, r.tokenPAN(), r.tokenExpDate(), r.tokenHolderName());
                } catch (InterruptedException | IOException | ExecutionException e) {
                    handleError(e);
                }
            });
        }
    }

    void sendCombinedCardDetails(SendCardDetailsRequest request, Token tokenPAN, Token tokenExpDate, Token tokenHolderName) throws IOException, InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Supplier<String> supplierPAN = scope.fork(() -> detokenize(tokenPAN));
            Supplier<String> supplierExpDate = scope.fork(() -> detokenize(tokenExpDate));
            Supplier<String> supplierHolderName = scope.fork(() -> detokenize(tokenHolderName));

            scope.join();
            scope.throwIfFailed();

            request.setPAN(supplierPAN.get())
                    .setExpDate(supplierExpDate.get())
                    .setHolderName(supplierHolderName.get())
                    .send();
        }
    }

    //mock
    private String detokenize(Token token) throws InterruptedException {
        Thread.sleep(1000);
        return token.token();
    }

    //mock
    private Task<String> handleError(Throwable e) {
        e.printStackTrace();
        return Task.failure(e);
    }
}