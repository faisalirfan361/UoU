package com.UoU.core.diagnostics;

/**
 * Info about a run id, including the run id itself.
 */
public record RunIdInfo(
    RunId runId,
    boolean isNew
) {
}
