package io.github.avinashio.ozhuku.notification.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MailMessageTest {

    @Test
    void shouldCreateMailMessageWithDefaults() {
        final MailMessage message = MailMessage.builder(
                List.of("recipient@example.com"),
                "Test Subject",
                "Test body")
                .build();

        assertEquals(List.of("recipient@example.com"), message.to());
        assertEquals("Test Subject", message.subject());
        assertEquals("Test body", message.body());
        assertEquals(MimeContentType.TEXT_PLAIN, message.contentType());
        assertNotNull(message.cc());
        assertNotNull(message.attachments());
    }

    @Test
    void shouldCreateMailMessageWithHtmlContent() {
        final MailMessage message = MailMessage.builder(
                List.of("recipient@example.com"),
                "HTML Subject",
                "<html><body>Hello</body></html>")
                .contentType(MimeContentType.TEXT_HTML)
                .build();

        assertEquals(MimeContentType.TEXT_HTML, message.contentType());
    }

    @Test
    void shouldCreateMailMessageWithCcAndAttachments() {
        final Attachment attachment = new Attachment(
                "report.csv",
                "a,b,c\n1,2,3".getBytes(),
                "text/csv");

        final MailMessage message = MailMessage.builder(
                List.of("to@example.com"),
                "Subject",
                "Body")
                .cc(List.of("cc@example.com"))
                .attachments(List.of(attachment))
                .build();

        assertEquals(List.of("to@example.com"), message.to());
        assertEquals(List.of("cc@example.com"), message.cc());
        assertEquals(1, message.attachments().size());
        assertEquals("report.csv", message.attachments().get(0).filename());
    }

    @Test
    void shouldRejectNullTo() {
        assertThrows(
                NullPointerException.class,
                () -> MailMessage.builder(
                        null,
                        "Subject",
                        "Body")
                        .build());
    }

    @Test
    void shouldRejectNullSubject() {
        assertThrows(
                NullPointerException.class,
                () -> MailMessage.builder(
                        List.of("to@example.com"),
                        null,
                        "Body")
                        .build());
    }

    @Test
    void shouldRejectNullBody() {
        assertThrows(
                NullPointerException.class,
                () -> MailMessage.builder(
                        List.of("to@example.com"),
                        "Subject",
                        null)
                        .build());
    }
}
