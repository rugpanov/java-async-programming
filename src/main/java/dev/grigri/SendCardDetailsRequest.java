package dev.grigri;

import java.io.IOException;
import java.net.Socket;

public class SendCardDetailsRequest {
    private final Socket socketRequest;
    private String decryptedPAN;
    private String decryptedExpDate;
    private String decryptedHolderName;

    public SendCardDetailsRequest(Socket socketRequest) {
        this.socketRequest = socketRequest;
    }

    public SendCardDetailsRequest setPAN(String decryptedPAN) {
        this.decryptedPAN = decryptedPAN;

        return this;
    }

    public SendCardDetailsRequest setExpDate(String decryptedExpDate) {
        this.decryptedExpDate = decryptedExpDate;
        return this;
    }

    public SendCardDetailsRequest setHolderName(String decryptedHolderName) {
        this.decryptedHolderName = decryptedHolderName;
        return this;
    }

    public void send() throws IOException {
        String sb = "PAN:" + decryptedPAN + "\\N" +
                "ExpDate:" + decryptedExpDate + "\\N" +
                "HolderName:" + decryptedHolderName;

        socketRequest.getOutputStream().write(sb.getBytes());
    }
}
