package io.github.avinashio.ozhuku.domain.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
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

class PipelinePlanTest {

    @Test
    void shouldExposePlanComponents() {
        final PipelineDefinition definition =
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1),
                        "Test pipeline");

        final Flow flow =
                new Flow(
                        new FlowId("flow-1"),
                        "Transfer flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                resource("source");

        final Resource destination =
                resource("destination");

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        final PipelinePlan plan =
                new PipelinePlan(
                        definition,
                        flow,
                        source,
                        destination,
                        deliveryPolicy);

        assertEquals(definition, plan.pipelineDefinition());
        assertEquals(flow, plan.flow());
        assertEquals(source, plan.source());
        assertEquals(destination, plan.destination());
        assertEquals(deliveryPolicy, plan.deliveryPolicy());
    }

    @Test
    void shouldRejectNullPipelineDefinition() {
        assertThrows(
                ValidationException.class,
                () -> new PipelinePlan(
                        null,
                        flow(),
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy()));
    }

    @Test
    void shouldRejectNullFlow() {
        assertThrows(
                ValidationException.class,
                () -> new PipelinePlan(
                        pipelineDefinition(),
                        null,
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy()));
    }

    @Test
    void shouldRejectNullSource() {
        assertThrows(
                ValidationException.class,
                () -> new PipelinePlan(
                        pipelineDefinition(),
                        flow(),
                        null,
                        resource("destination"),
                        deliveryPolicy()));
    }

    @Test
    void shouldRejectNullDestination() {
        assertThrows(
                ValidationException.class,
                () -> new PipelinePlan(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        null,
                        deliveryPolicy()));
    }

    @Test
    void shouldRejectNullDeliveryPolicy() {
        assertThrows(
                ValidationException.class,
                () -> new PipelinePlan(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        resource("destination"),
                        null));
    }

    @Test
    void equalPlansShouldBeEqual() {
        final PipelinePlan first =
                new PipelinePlan(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        final PipelinePlan second =
                new PipelinePlan(
                        pipelineDefinition(),
                        flow(),
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotNull(first.toString());
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
                "Transfer flow",
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