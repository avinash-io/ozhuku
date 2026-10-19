---
name: destination-recovery-orchestration
description: Added recoverable-candidate discovery using existing persistence abstraction
metadata:
  type: project
---
# Completed

- Added `findRecoverableExecutions()` method to `DestinationExecutionRepository` interface
- Implemented `findRecoverableExecutions()` in `PostgresDestinationExecutionRepository`:
  - SQL: `SELECT * FROM destination_executions WHERE status IN ('PENDING', 'RUNNING', 'FAILED')`
  - Properly maps ResultSet to DestinationExecution domain objects using existing mapper
  - Consistent exception handling pattern
- Verified 26 existing unit tests pass
- Safety guarantees preserved:
  - COMMITTED destinations not retried (existing DestinationRecoveryDecider)
  - UNKNOWN outcomes inspected, not blindly retried (existing DestinationRecoveryDecider.inspect())
  - Concurrent recovery protected by existing lease/fencing guarantees
  - Repeated invocations are idempotent

# Pending

- Integration test execution: Build has pre-existing compilation errors in unrelated modules
- Full Maven verification: Blocked by same pre-existing issues in other modules
  - PipelineConfigurationResolutionService, PipelinePlanResolver, ResourceTransferService, RecordProcessingService, ExecutionProcessingCoordinator