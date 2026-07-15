package com.arley.poc.as400.job;

import java.time.Instant;

import com.arley.poc.as400.result.ExecutionResult;

/**
 * Representación pública de un trabajo.
 *
 * Es el objeto que devolverán los endpoints REST.
 */
public record JobView(
    String jobId,
    String processId,
    String processName,
    JobStatus status,
    Instant createdAt,
    Instant startedAt,
    Instant finishedAt,
    ExecutionResult result,
    String errorMessage
) {
}