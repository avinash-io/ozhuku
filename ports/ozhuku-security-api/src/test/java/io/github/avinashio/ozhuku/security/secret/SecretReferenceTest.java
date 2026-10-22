package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecretReferenceTest {

    @Test
    void shouldCreateSecretReference() {
        final SecretReference ref =
                new SecretReference("vault://smtp/password");

        assertEquals("vault://smtp/password", ref.reference());
    }

    @Test
    void shouldRejectBlankReference() {
        assertThrows(
                ValidationException.class,
                () -> new SecretReference(""));
    }

    @Test
    void shouldRejectNullReference() {
        assertThrows(
                ValidationException.class,
                () -> new SecretReference(null));
    }

    @Test
    void shouldEqualWhenReferenceIsSame() {
        final SecretReference ref1 =
                new SecretReference("vault://secret/key");

        final SecretReference ref2 =
                new SecretReference("vault://secret/key");

        assertEquals(ref1, ref2);
        assertEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    void shouldNotEqualWhenReferenceDiffers() {
        final SecretReference ref1 =
                new SecretReference("vault://secret/key1");

        final SecretReference ref2 =
                new SecretReference("vault://secret/key2");

        assertEquals(false, ref1.equals(ref2));
    }
}
