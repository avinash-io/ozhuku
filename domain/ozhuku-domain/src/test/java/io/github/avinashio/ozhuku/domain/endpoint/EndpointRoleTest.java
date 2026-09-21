package io.github.avinashio.ozhuku.domain.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EndpointRoleTest {

    @Test
    void shouldContainExactlyTwoRoles() {
        assertEquals(2, EndpointRole.values().length);
    }

    @Test
    void shouldContainSource() {
        assertNotNull(EndpointRole.valueOf("SOURCE"));
    }

    @Test
    void shouldContainDestination() {
        assertNotNull(EndpointRole.valueOf("DESTINATION"));
    }
}