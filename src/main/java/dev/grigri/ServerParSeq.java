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
                    var request = new SendDetokenizedDetailsRequest(socket);
                    processDetokenizedDetails(request, r.tokenName(), r.tokenSurname(), r.tokenEmail());
                } catch (InterruptedException e) {
                    handleError(e);
                }
            });
        }
    }

    void processDetokenizedDetails(SendDetokenizedDetailsRequest request, Token tokenName, Token tokenSurname, Token tokenEmail) throws InterruptedException {
        var taskName = runDetokenizeTask(tokenName);
        var taskSurname = runDetokenizeTask(tokenSurname);
        var taskEmail = runDetokenizeTask(tokenEmail);

        var task = Task.par(taskName, taskSurname, taskEmail)
                .andThen((name, surname, email) ->
                        request.setName(name)
                                .setSurname(surname)
                                .setEmail(email)
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