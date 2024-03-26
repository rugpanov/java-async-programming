package dev.grigri;

import com.linkedin.parseq.Task;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerVirtualThreadsScopedValues {
    public final static ScopedValue<String> REQUEST_ID
            = ScopedValue.newInstance();

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);
            Thread.ofVirtual().start(() -> {
                var request = new SendDetokenizedDetailsRequest(socket);

                ScopedValue.where(REQUEST_ID, r.requestId()).run(() -> {
                    try {
                        processDetokenizedDetails(request, r.tokenName(), r.tokenSurname(), r.tokenEmail());
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        handleError(e);
                    }
                });
            });
        }
    }

    private final ExecutorService es = Executors.newVirtualThreadPerTaskExecutor();

    void processDetokenizedDetails(SendDetokenizedDetailsRequest request, Token tokenName, Token tokenSurname, Token tokenEmail) throws IOException, InterruptedException, ExecutionException {
        Future<String> futureName = es.submit(() -> detokenize(tokenName));
        Future<String> futureSurname = es.submit(() -> detokenize(tokenSurname));
        Future<String> futureEmail = es.submit(() -> detokenize(tokenEmail));

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
    private Task<String> handleError(Throwable e) {
        e.printStackTrace();
        return Task.failure(e);
    }
}
