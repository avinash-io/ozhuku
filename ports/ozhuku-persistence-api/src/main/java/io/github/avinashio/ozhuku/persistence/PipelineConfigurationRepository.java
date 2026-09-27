package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineConfiguration;
import java.util.Optional;

/**
 * Persistence port for versioned pipeline configuration.
 *
 * <p>The port exposes domain configuration concepts only and remains
 * independent of the underlying persistence technology.</p>
 */
public interface PipelineConfigurationRepository {

    /**
     * Finds a specific immutable pipeline configuration version.
     *
     * @param pipelineId pipeline identifier
     * @param pipelineVersion pipeline configuration version
     * @return persisted configuration when present
     */
    Optional<PipelineConfiguration> findByVersion(
            PipelineId pipelineId,
            PipelineVersion pipelineVersion);

    /**
     * Persists a pipeline configuration.
     *
     * <p>The implementation must preserve the identity of the supplied
     * pipeline and version.</p>
     *
     * @param configuration pipeline configuration to persist
     */
    void save(PipelineConfiguration configuration);
}