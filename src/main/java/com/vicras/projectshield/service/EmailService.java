package com.vicras.projectshield.service;

import com.vicras.projectshield.config.ResendConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final RestClient resendRestClient;
    private final ResendConfig resendConfig;

    public EmailService(RestClient resendRestClient, ResendConfig resendConfig) {
        this.resendRestClient = resendRestClient;
        this.resendConfig = resendConfig;
    }

    public void sendInvitationEmail(String toEmail, String inviterName, String orgName,
                                     String inviteLink, String role) {
        String subject = inviterName + " invited you to join " + orgName + " on Project Shield";
        String html = buildInvitationHtml(inviterName, orgName, inviteLink, role);

        Map<String, Object> emailRequest = Map.of(
                "from", resendConfig.getFromEmail(),
                "to", new String[]{toEmail},
                "subject", subject,
                "html", html
        );

        try {
            resendRestClient.post()
                    .uri("/emails")
                    .body(emailRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        logger.error("Resend API error sending invitation email: {} - {}", res.getStatusCode(), body);
                    })
                    .toBodilessEntity();
            logger.info("Invitation email sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildInvitationHtml(String inviterName, String orgName, String inviteLink, String role) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f1f5f9;padding:40px 20px;">
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#2563eb,#4f46e5);padding:32px 40px;text-align:center;">
                              <h1 style="color:#ffffff;margin:0;font-size:24px;font-weight:700;">Project Shield</h1>
                            </td>
                          </tr>
                          <!-- Body -->
                          <tr>
                            <td style="padding:40px;">
                              <h2 style="color:#1e293b;margin:0 0 16px;font-size:20px;">You've been invited!</h2>
                              <p style="color:#475569;font-size:16px;line-height:1.6;margin:0 0 24px;">
                                <strong>%s</strong> has invited you to join <strong>%s</strong> on Project Shield as a <strong>%s</strong>.
                              </p>
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td align="center" style="padding:8px 0 24px;">
                                    <a href="%s" style="display:inline-block;background-color:#2563eb;color:#ffffff;text-decoration:none;padding:14px 32px;border-radius:8px;font-size:16px;font-weight:600;">
                                      Accept Invitation
                                    </a>
                                  </td>
                                </tr>
                              </table>
                              <p style="color:#94a3b8;font-size:14px;line-height:1.5;margin:0;">
                                This invitation expires in 7 days. If you didn't expect this email, you can safely ignore it.
                              </p>
                            </td>
                          </tr>
                          <!-- Footer -->
                          <tr>
                            <td style="background-color:#f8fafc;padding:24px 40px;text-align:center;border-top:1px solid #e2e8f0;">
                              <p style="color:#94a3b8;font-size:12px;margin:0;">Project Shield - Capacity Planning Platform</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(inviterName, orgName, role, inviteLink);
    }
}
