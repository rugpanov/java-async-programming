package dev.grigri;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class ServerPlatformThreadsTest {

    @Test
    public void testSendCombinedCardDetails() throws IOException, InterruptedException {
        // Mock the dependencies
        Token mockPAN = new Token("1234567890123456");
        Token mockExpDate = new Token("12/25");
        Token mockHolderName = new Token("Grigory Panov");

        // Assuming SendCardDetailsRequest can be mocked and its methods chained
        SendCardDetailsRequest mockRequest = mock(SendCardDetailsRequest.class);
        when(mockRequest.setPAN(anyString())).thenReturn(mockRequest);
        when(mockRequest.setExpDate(anyString())).thenReturn(mockRequest);
        when(mockRequest.setHolderName(anyString())).thenReturn(mockRequest);
        doNothing().when(mockRequest).send();

        // Replace the real SendCardDetailsRequest creation with the mock
        ServerPlatformThreads server = new ServerPlatformThreads();
        server.sendCombinedCardDetails(mockRequest, mockPAN, mockExpDate, mockHolderName);

        // Verify the interactions
        verify(mockRequest).setPAN("1234567890123456");
        verify(mockRequest).setExpDate("12/25");
        verify(mockRequest).setHolderName("Grigory Panov");
        verify(mockRequest).send();
    }
}
