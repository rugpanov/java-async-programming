package dev.grigri;

import java.io.IOException;
import java.net.Socket;

public class SendDetokenizedDetailsRequest {
    private final Socket socketRequest;
    private String detokenizedName;
    private String ddetokenizedSurname;
    private String detokenizedEmail;

    public SendDetokenizedDetailsRequest(Socket socketRequest) {
        this.socketRequest = socketRequest;
    }

    public SendDetokenizedDetailsRequest setName(String detokenizedName) {
        this.detokenizedName = detokenizedName;

        return this;
    }

    public SendDetokenizedDetailsRequest setSurname(String ddetokenizedSurname) {
        this.ddetokenizedSurname = ddetokenizedSurname;
        return this;
    }

    public SendDetokenizedDetailsRequest setEmail(String detokenizedEmail) {
        this.detokenizedEmail = detokenizedEmail;
        return this;
    }

    public void send() throws IOException {
        String sb = "Name:" + detokenizedName + "\\N" +
                "Surname:" + ddetokenizedSurname + "\\N" +
                "Email:" + detokenizedEmail;

        socketRequest.getOutputStream().write(sb.getBytes());
    }
}
