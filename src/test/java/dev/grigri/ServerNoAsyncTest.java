package dev.grigri;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import java.io.IOException;

public class ServerNoAsyncTest {

    @Test
    public void testSendCombinedCardDetails() throws IOException {
        // Mock the dependencies
        Token mockName = new Token("Grigory");
        Token mockSurname = new Token("Panov");
        Token mockEmail = new Token("test@gmail.com");

        // Assuming SendCardDetailsRequest can be mocked and its methods chained
        SendDetokenizedDetailsRequest mockRequest = mock(SendDetokenizedDetailsRequest.class);
        when(mockRequest.setName(anyString())).thenReturn(mockRequest);
        when(mockRequest.setSurname(anyString())).thenReturn(mockRequest);
        when(mockRequest.setEmail(anyString())).thenReturn(mockRequest);
        doNothing().when(mockRequest).send();

        // Replace the real SendCardDetailsRequest creation with the mock
        ServerNoAsync server = new ServerNoAsync();
        server.processDetokenizedDetails(mockRequest, mockName, mockSurname, mockEmail);

        // Verify the interactions
        verify(mockRequest).setName("Grigory");
        verify(mockRequest).setSurname("Panov");
        verify(mockRequest).setEmail("test@gmail.com");
        verify(mockRequest).send();
    }
}
