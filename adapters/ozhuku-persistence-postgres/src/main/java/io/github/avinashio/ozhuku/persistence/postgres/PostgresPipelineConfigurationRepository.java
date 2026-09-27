package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineConfiguration;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineDefinition;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.PipelineConfigurationRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * PostgreSQL persistence implementation for versioned pipeline configuration.
 *
 * <p>Pipeline configurations are immutable once persisted. A duplicate
 * pipeline/version therefore results in a persistence failure rather than
 * replacing the existing configuration.</p>
 */
public final class PostgresPipelineConfigurationRepository
        implements PipelineConfigurationRepository {

    private static final String FIND_BY_VERSION_SQL = """
            SELECT
                pipeline_id,
                pipeline_version,
                description,
                flow_id,
                flow_name,
                flow_mode,
                source_resource_id,
                source_location,
                destination_resource_id,
                destination_location,
                conflict_behavior
            FROM pipeline_configurations
            WHERE pipeline_id = ?
              AND pipeline_version = ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO pipeline_configurations (
                pipeline_id,
                pipeline_version,
                description,
                flow_id,
                flow_name,
                flow_mode,
                source_resource_id,
                source_location,
                destination_resource_id,
                destination_location,
                conflict_behavior
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final DataSource dataSource;

    /**
     * Creates a PostgreSQL pipeline configuration repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresPipelineConfigurationRepository(final DataSource dataSource) {
        this.dataSource = Validation.requireNonNull(dataSource, "dataSource");
    }

    /**
     * Finds an immutable pipeline configuration by pipeline identity and version.
     *
     * @param pipelineId pipeline identifier
     * @param pipelineVersion pipeline configuration version
     * @return persisted configuration when present
     */
    @Override
    public Optional<PipelineConfiguration> findByVersion(
            final PipelineId pipelineId,
            final PipelineVersion pipelineVersion) {
        Validation.requireNonNull(pipelineId, "pipelineId");
        Validation.requireNonNull(pipelineVersion, "pipelineVersion");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_VERSION_SQL)) {

            statement.setString(1, pipelineId.value());
            statement.setLong(2, pipelineVersion.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapConfiguration(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find pipeline configuration",
                    exception);
        }
    }

    /**
     * Persists a new pipeline configuration version.
     *
     * <p>The operation intentionally uses a plain INSERT. Existing versions
     * must remain immutable and must not be silently replaced.</p>
     *
     * @param configuration pipeline configuration to persist
     */
    @Override
    public void save(final PipelineConfiguration configuration) {
        Validation.requireNonNull(configuration, "configuration");

        final PipelineDefinition pipelineDefinition =
                configuration.pipelineDefinition();
        final Flow flow = configuration.flow();
        final Resource source = configuration.source();
        final Resource destination = configuration.destination();
        final DeliveryPolicy deliveryPolicy = configuration.deliveryPolicy();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, pipelineDefinition.pipelineId().value());
            statement.setLong(2, pipelineDefinition.version().value());
            statement.setString(3, pipelineDefinition.description());

            statement.setString(4, flow.id().value());
            statement.setString(5, flow.name());
            statement.setString(6, flow.mode().name());

            statement.setString(7, source.id().value());
            statement.setString(8, source.location().value());

            statement.setString(9, destination.id().value());
            statement.setString(10, destination.location().value());

            statement.setString(11, deliveryPolicy.conflictBehavior().name());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save pipeline configuration",
                    exception);
        }
    }

    private PipelineConfiguration mapConfiguration(final ResultSet resultSet)
            throws SQLException {
        final PipelineDefinition pipelineDefinition =
                new PipelineDefinition(
                        new PipelineId(resultSet.getString("pipeline_id")),
                        new PipelineVersion(resultSet.getLong("pipeline_version")),
                        resultSet.getString("description"));

        final Flow flow =
                new Flow(
                        new FlowId(resultSet.getString("flow_id")),
                        resultSet.getString("flow_name"),
                        FlowMode.valueOf(resultSet.getString("flow_mode")));

        final Resource source =
                new Resource(
                        new ResourceId(resultSet.getString("source_resource_id")),
                        new ResourceLocation(resultSet.getString("source_location")));

        final Resource destination =
                new Resource(
                        new ResourceId(resultSet.getString("destination_resource_id")),
                        new ResourceLocation(
                                resultSet.getString("destination_location")));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(
                        ConflictBehavior.valueOf(
                                resultSet.getString("conflict_behavior")));

        return new PipelineConfiguration(
                pipelineDefinition,
                flow,
                source,
                destination,
                deliveryPolicy);
    }
}