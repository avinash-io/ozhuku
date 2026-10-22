package io.github.avinashio.ozhuku.notification.model;

import io.github.avinashio.ozhuku.security.secret.SecretReference;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MailServerProfileTest {

    @Test
    void shouldCreateProfileWithDefaults() {
        final MailServerProfile profile = MailServerProfile.builder(
                "operations-mail",
                "smtp.example.com")
                .port(587)
                .sender("noreply@example.com")
                .secretReference(new SecretReference("env://SMTP_PASSWORD"))
                .build();

        assertEquals("operations-mail", profile.id());
        assertEquals("smtp.example.com", profile.host());
        assertEquals(587, profile.port());
        assertEquals(
                MailServerProfile.SecurityMode.STARTTLS,
                profile.securityMode());
        assertEquals(
                MailServerProfile.AuthenticationMode.USERNAME_PASSWORD,
                profile.authenticationMode());
        assertEquals("noreply@example.com", profile.sender());
        assertEquals(
                "env://SMTP_PASSWORD",
                profile.secretReference().reference());
    }

    @Test
    void shouldCreateProfileWithCustomSettings() {
        final MailServerProfile profile = MailServerProfile.builder(
                "secure-mail",
                "smtp.secure.com")
                .port(465)
                .securityMode(MailServerProfile.SecurityMode.TLS)
                .authenticationMode(MailServerProfile.AuthenticationMode.OAUTH2)
                .connectionTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(15))
                .writeTimeout(Duration.ofSeconds(15))
                .sender("alerts@example.com")
                .replyTo("support@example.com")
                .secretReference(new SecretReference("vault://smtp/oauth-token"))
                .build();

        assertEquals(465, profile.port());
        assertEquals(MailServerProfile.SecurityMode.TLS, profile.securityMode());
        assertEquals(
                MailServerProfile.AuthenticationMode.OAUTH2,
                profile.authenticationMode());
        assertEquals(Duration.ofSeconds(10), profile.connectionTimeout());
        assertEquals(Duration.ofSeconds(15), profile.readTimeout());
        assertEquals(Duration.ofSeconds(15), profile.writeTimeout());
        assertEquals("alerts@example.com", profile.sender());
        assertEquals("support@example.com", profile.replyTo());
    }

    @Test
    void shouldCreateProfileWithNoSecurity() {
        final MailServerProfile profile = MailServerProfile.builder(
                "insecure",
                "smtp.local")
                .port(25)
                .securityMode(MailServerProfile.SecurityMode.NONE)
                .authenticationMode(MailServerProfile.AuthenticationMode.NONE)
                .build();

        assertEquals(MailServerProfile.SecurityMode.NONE, profile.securityMode());
        assertEquals(
                MailServerProfile.AuthenticationMode.NONE,
                profile.authenticationMode());
    }
}
