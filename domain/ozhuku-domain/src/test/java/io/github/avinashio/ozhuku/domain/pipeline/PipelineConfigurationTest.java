package io.github.avinashio.ozhuku.domain.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PipelineConfigurationTest {

    @Test
    void shouldCreateConfiguration() {
        final PipelineDefinition definition = pipelineDefinition();
        final Flow flow = flow();
        final Resource source = resource("source");
        final Resource destination = resource("destination");
        final DeliveryPolicy deliveryPolicy = deliveryPolicy();
        final DuplicatePolicy duplicatePolicy =
                DuplicatePolicy.SKIP_IF_PROCESSED;

        final PipelineConfiguration configuration =
                new PipelineConfiguration(
                        definition,
                        flow,
                        source,
                        destination,
                        deliveryPolicy,
                        duplicatePolicy);

        assertEquals(definition, configuration.pipelineDefinition());
        assertEquals(flow, configuration.flow());
        assertEquals(source, configuration.source());
        assertEquals(destination, configuration.destination());
        assertEquals(deliveryPolicy, configuration.deliveryPolicy());
        assertEquals(duplicatePolicy, configuration.duplicatePolicy());
    }

    @Test
    void shouldRejectNullPipelineDefinition() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineConfiguration(
                        null,
                        flow(),
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        DuplicatePolicy.SKIP_IF_PROCESSED));
    }

    @Test
    void shouldRejectNullFlow() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineConfiguration(
                        pipelineDefinition(),
                        null,
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        DuplicatePolicy.SKIP_IF_PROCESSED));
    }

    @Test
    void shouldRejectNullSource() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineConfiguration(
                        pipelineDefinition(),
                        flow(),
                        null,
                        resource("destination"),
                        deliveryPolicy(),
                        DuplicatePolicy.SKIP_IF_PROCESSED));
    }

    @Test
    void shouldRejectNullDestination() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineConfiguration(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        null,
                        deliveryPolicy(),
                        DuplicatePolicy.SKIP_IF_PROCESSED));
    }

    @Test
    void shouldRejectNullDeliveryPolicy() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineConfiguration(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        resource("destination"),
                        null,
                        DuplicatePolicy.SKIP_IF_PROCESSED));
    }

    @Test
    void shouldRejectNullDuplicatePolicy() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineConfiguration(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        null));
    }

    @Test
    void shouldCompareConfigurationsByValue() {
        final PipelineConfiguration first =
                new PipelineConfiguration(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        DuplicatePolicy.SKIP_IF_PROCESSED);

        final PipelineConfiguration second =
                new PipelineConfiguration(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        DuplicatePolicy.SKIP_IF_PROCESSED);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private PipelineDefinition pipelineDefinition() {
        return new PipelineDefinition(
                new PipelineId("pipeline-1"),
                new PipelineVersion(1),
                "Test pipeline");
    }

    private Flow flow() {
        return new Flow(
                new FlowId("flow-1"),
                "Test flow",
                FlowMode.RESOURCE_TRANSFER);
    }

    private Resource resource(final String name) {
        return new Resource(
                new ResourceId(name),
                new ResourceLocation("file:///" + name));
    }

    private DeliveryPolicy deliveryPolicy() {
        return new DeliveryPolicy(ConflictBehavior.REPLACE);
    }
}