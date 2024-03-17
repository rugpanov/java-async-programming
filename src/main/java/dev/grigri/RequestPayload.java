package dev.grigri;


import java.io.IOException;
import java.net.Socket;

record RequestPayload(Token tokenPAN, Token tokenExpDate, Token tokenHolderName) {
    /*
     Expected format of the request:
     InputStream: PAN:panToken\nExpDate:expDateToken\nHolderName:holderNameToken
    */
    static RequestPayload from(Socket request) throws IOException {
        var params = IOUtils.readLines(request.getInputStream(), "UTF-8");

        if (params.length != 3) {
            throw new IllegalArgumentException("Request has incorrect payload");
        }
        return new RequestPayload(
                new Token(params[0].split(":")[1]),
                new Token(params[1].split(":")[1]),
                new Token(params[2].split(":")[1])
        );
    }
}