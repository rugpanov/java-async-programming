package dev.grigri;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerNoAsync {

    public void run() throws IOException {
        ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);

            var request = new SendDetokenizedDetailsRequest(socket);
            processDetokenizedDetails(request, r.tokenName(), r.tokenSurname(), r.tokenEmail());
        }
    }

    void processDetokenizedDetails(SendDetokenizedDetailsRequest request, Token tokenName, Token tokenSurname, Token tokenEmail) throws IOException {
        request.setName(detokenize(tokenName))
                .setSurname(detokenize(tokenSurname))
                .setEmail(detokenize(tokenEmail))
                .send();
    }

    //mock
    private String detokenize(Token token) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return token.token();
    }
}