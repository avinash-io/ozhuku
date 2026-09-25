package io.github.avinashio.ozhuku.application.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineDefinition;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PipelinePlanResolverTest {

    private final PipelinePlanResolver resolver =
            new PipelinePlanResolver();

    @Test
    void shouldResolvePipelinePlan() {

        final PipelineDefinition pipelineDefinition =
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1L),
                        "Test pipeline");

        final Flow flow =
                new Flow(
                        new FlowId("flow-1"),
                        "Test flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        new ResourceId("source-1"),
                        new ResourceLocation("file:///source"));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation("file:///destination"));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        final PipelinePlan result =
                resolver.resolve(
                        pipelineDefinition,
                        flow,
                        source,
                        destination,
                        deliveryPolicy);

        assertEquals(pipelineDefinition, result.pipelineDefinition());
        assertEquals(flow, result.flow());
        assertEquals(source, result.source());
        assertEquals(destination, result.destination());
        assertEquals(deliveryPolicy, result.deliveryPolicy());
    }

    @Test
    void shouldRejectNullPipelineDefinition() {

        final Flow flow =
                new Flow(
                        new FlowId("flow-1"),
                        "Test flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        new ResourceId("source-1"),
                        new ResourceLocation("file:///source"));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation("file:///destination"));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        assertThrows(
                ValidationException.class,
                () -> resolver.resolve(
                        null,
                        flow,
                        source,
                        destination,
                        deliveryPolicy));
    }

    @Test
    void shouldRejectNullFlow() {

        final PipelineDefinition pipelineDefinition =
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1L),
                        "Test pipeline");

        final Resource source =
                new Resource(
                        new ResourceId("source-1"),
                        new ResourceLocation("file:///source"));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation("file:///destination"));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        assertThrows(
                ValidationException.class,
                () -> resolver.resolve(
                        pipelineDefinition,
                        null,
                        source,
                        destination,
                        deliveryPolicy));
    }

    @Test
    void shouldRejectNullSource() {

        final PipelineDefinition pipelineDefinition =
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1L),
                        "Test pipeline");

        final Flow flow =
                new Flow(
                        new FlowId("flow-1"),
                        "Test flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation("file:///destination"));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        assertThrows(
                ValidationException.class,
                () -> resolver.resolve(
                        pipelineDefinition,
                        flow,
                        null,
                        destination,
                        deliveryPolicy));
    }

    @Test
    void shouldRejectNullDestination() {

        final PipelineDefinition pipelineDefinition =
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1L),
                        "Test pipeline");

        final Flow flow =
                new Flow(
                        new FlowId("flow-1"),
                        "Test flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        new ResourceId("source-1"),
                        new ResourceLocation("file:///source"));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        assertThrows(
                ValidationException.class,
                () -> resolver.resolve(
                        pipelineDefinition,
                        flow,
                        source,
                        null,
                        deliveryPolicy));
    }

    @Test
    void shouldRejectNullDeliveryPolicy() {

        final PipelineDefinition pipelineDefinition =
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1L),
                        "Test pipeline");

        final Flow flow =
                new Flow(
                        new FlowId("flow-1"),
                        "Test flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        new ResourceId("source-1"),
                        new ResourceLocation("file:///source"));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation("file:///destination"));

        assertThrows(
                ValidationException.class,
                () -> resolver.resolve(
                        pipelineDefinition,
                        flow,
                        source,
                        destination,
                        null));
    }
}