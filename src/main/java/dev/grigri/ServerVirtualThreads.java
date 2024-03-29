package dev.grigri;

import com.linkedin.parseq.Task;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

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