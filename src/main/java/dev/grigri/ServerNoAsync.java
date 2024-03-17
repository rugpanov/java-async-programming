package dev.grigri;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerNoAsync {

    public void run() throws IOException {
        ServerSocket server = new ServerSocket(8080);

        while (!server.isClosed()) {
            var socket = server.accept();
            var r = RequestPayload.from(socket);

            var request = new SendCardDetailsRequest(socket);
            sendCombinedCardDetails(request, r.tokenPAN(), r.tokenExpDate(), r.tokenHolderName());
        }
    }

    void sendCombinedCardDetails(SendCardDetailsRequest request, Token tokenPAN, Token tokenExpDate, Token tokenHolderName) throws IOException {
        request.setPAN(detokenize(tokenPAN))
                .setExpDate(detokenize(tokenExpDate))
                .setHolderName(detokenize(tokenHolderName))
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