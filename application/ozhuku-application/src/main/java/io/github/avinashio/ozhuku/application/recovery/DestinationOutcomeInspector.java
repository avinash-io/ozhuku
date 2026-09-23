package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.CommitStatus;

public interface DestinationOutcomeInspector {

    CommitStatus inspect(
            DestinationCommitReference reference);
}