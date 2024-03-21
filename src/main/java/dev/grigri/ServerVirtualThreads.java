package dev.grigri;

import com.linkedin.parseq.Task;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class ServerVirtualThreads {

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);
            Thread.ofVirtual().start(() -> {
                try {
                    var request = new SendDetokenizedDetailsRequest(socket);
                    processDetokenizedDetails(request, r.tokenName(), r.tokenSurname(), r.tokenEmail());
                } catch (InterruptedException | IOException | ExecutionException e) {
                    handleError(e);
                }
            });
        }
    }

    void processDetokenizedDetails(SendDetokenizedDetailsRequest request, Token tokenName, Token tokenSurname, Token tokenEmail) throws IOException, InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var supplierName = scope.fork(() -> detokenize(tokenName));
            var supplierSurname = scope.fork(() -> detokenize(tokenSurname));
            var supplierEmail = scope.fork(() -> detokenize(tokenEmail));

            scope.join();
            scope.throwIfFailed();

            request.setName(supplierName.get())
                    .setSurname(supplierSurname.get())
                    .setEmail(supplierEmail.get())
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