package dev.grigri;

import com.linkedin.parseq.Engine;
import com.linkedin.parseq.EngineBuilder;
import com.linkedin.parseq.Task;
import com.linkedin.parseq.promise.Promises;
import com.linkedin.parseq.promise.SettablePromise;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerParSeq {

    private final ExecutorService es1 = Executors.newFixedThreadPool(128);
    private final ExecutorService es2 = Executors.newFixedThreadPool(256);
    final ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();
    private final Engine engine = new EngineBuilder().setTaskExecutor(es2).setTimerScheduler(timerScheduler).build();

    public void run() throws IOException {
        final ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);
            es1.submit(() -> {
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

        var task = Task.par(taskPAN, taskExpDate, taskHolderName)
                .andThen((pan, expDate, holderName) ->
                        request.setPAN(pan)
                                .setExpDate(expDate)
                                .setHolderName(holderName)
                                .send()
                );

        engine.run(task);
    }

    //mock
    private Task<String> runDetokenizeTask(Token token) {
        return Task.async(() -> {
                    SettablePromise<String> settablePromise = Promises.settable();
                    timerScheduler.schedule(() -> settablePromise.done(token.token()), 1000, TimeUnit.MILLISECONDS);
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