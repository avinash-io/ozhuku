package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.report.ExecutionReport;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for execution report storage and retrieval.
 */
public interface ExecutionReportRepository {

    /**
     * Persists an execution report.
     *
     * @param executionReport the report to persist
     */
    void save(ExecutionReport executionReport);

    /**
     * Retrieves an execution report by its identifier.
     *
     * @param reportId the report identifier
     * @return the report, or empty if not found
     */
    Optional<ExecutionReport> findById(String reportId);

    /**
     * Finds all reports for a given execution.
     *
     * @param executionId the execution identifier
     * @return list of reports for the execution
     */
    List<ExecutionReport> findByExecutionId(String executionId);
}
