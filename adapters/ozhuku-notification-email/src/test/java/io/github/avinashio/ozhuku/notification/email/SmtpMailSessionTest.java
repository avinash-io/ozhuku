package io.github.avinashio.ozhuku.notification.email;

import io.github.avinashio.ozhuku.notification.model.MailServerProfile;
import io.github.avinashio.ozhuku.security.secret.SecretReference;
import io.github.avinashio.ozhuku.security.secret.SecretResolver;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SmtpMailSessionTest {

    @Test
    void shouldCreateSessionWithCredentials() {
        final SecretResolver resolver = ref ->
                "super-secret".contentEquals(ref.reference())
                        ? Optional.of("p@ssw0rd".toCharArray())
                        : Optional.empty();

        final MailServerProfile profile = MailServerProfile.builder(
                "test-profile",
                "smtp.example.com")
                .port(587)
                .sender("noreply@example.com")
                .secretReference(
                        new SecretReference("super-secret"))
                .build();

        final SmtpMailSession session =
                new SmtpMailSession(profile, resolver);

        assertEquals("smtp.example.com", session.host());
        assertEquals(587, session.port());
        assertEquals("noreply@example.com", session.sender());
        assertNotNull(session.password());
    }

    @Test
    void shouldCreateSessionWithoutCredentials() {
        final MailServerProfile profile = MailServerProfile.builder(
                "no-auth",
                "smtp.example.com")
                .port(25)
                .securityMode(MailServerProfile.SecurityMode.NONE)
                .authenticationMode(
                        MailServerProfile.AuthenticationMode.NONE)
                .build();

        final SmtpMailSession session =
                new SmtpMailSession(profile, null);

        assertEquals("smtp.example.com", session.host());
        assertEquals(25, session.port());
        assertEquals(MailServerProfile.SecurityMode.NONE,
                session.securityMode());
        assertNull(session.password());
        assertNull(session.sender());
    }

    @Test
    void shouldHandleUnresolvableSecret() {
        final SecretResolver resolver = ref -> Optional.empty();

        final MailServerProfile profile = MailServerProfile.builder(
                "test-profile",
                "smtp.example.com")
                .secretReference(
                        new SecretReference("vault://missing/secret"))
                .build();

        final SmtpMailSession session =
                new SmtpMailSession(profile, resolver);

        assertNull(session.password());
    }
}
