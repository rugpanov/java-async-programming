package dev.grigri;

import com.linkedin.parseq.Task;
import com.linkedin.parseq.promise.Promises;
import com.linkedin.parseq.promise.SettablePromise;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerParSeq {

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
                } catch (InterruptedException e) {
                    handleError(e);
                }
            });
        }
    }

    void sendCombinedCardDetails(SendCardDetailsRequest request, Token tokenPAN, Token tokenExpDate, Token tokenHolderName) throws InterruptedException {
        var taskPAN = runDetokenizeTask(tokenPAN);
        var taskExpDate = runDetokenizeTask(tokenExpDate);
        var taskHolderName = runDetokenizeTask(tokenHolderName);

        Task.par(taskPAN, taskExpDate, taskHolderName)
                .andThen((pan, expDate, holderName) ->
                        request.setPAN(pan)
                                .setExpDate(expDate)
                                .setHolderName(holderName)
                                .send()
                );
    }

    //mock
    private Task<String> runDetokenizeTask(Token token) {
        return Task.async(() -> {
                    SettablePromise<String> settablePromise = Promises.settable();
                    Thread.sleep(1000);
                    settablePromise.done(token.token());
                    return settablePromise;
                })
                .recoverWith(this::handleError);
    }

    //mock
    private Task<String> handleError(Throwable e) {
        e.printStackTrace();
        return Task.failure(e);
    }
}