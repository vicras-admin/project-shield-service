package com.vicras.projectshield.service;

import com.vicras.projectshield.config.ResendConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient resendRestClient;

    @Mock
    private ResendConfig resendConfig;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendInvitationEmail_callsResendApi() {
        when(resendConfig.getFromEmail()).thenReturn("noreply@projectshield.app");

        RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(resendRestClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri("/emails")).thenReturn(bodySpec);
        when(bodySpec.body(any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        emailService.sendInvitationEmail(
                "user@example.com",
                "Admin User",
                "Test Org",
                "http://localhost:5173/#accept-invite?token=abc123",
                "member"
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(bodySpec).body(bodyCaptor.capture());

        Map<String, Object> emailBody = bodyCaptor.getValue();
        assertThat(emailBody.get("from")).isEqualTo("noreply@projectshield.app");
        assertThat((String[]) emailBody.get("to")).contains("user@example.com");
        assertThat((String) emailBody.get("subject")).contains("Admin User");
        assertThat((String) emailBody.get("subject")).contains("Test Org");
        assertThat((String) emailBody.get("html")).contains("Accept Invitation");
    }

    @Test
    void sendInvitationEmail_doesNotThrowOnFailure() {
        when(resendConfig.getFromEmail()).thenReturn("noreply@projectshield.app");

        when(resendRestClient.post()).thenThrow(new RuntimeException("Network error"));

        // Should not throw
        emailService.sendInvitationEmail(
                "user@example.com", "Admin", "Org", "http://link", "member"
        );
    }
}
