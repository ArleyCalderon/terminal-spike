package com.arley.poc.as400.job;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.arley.poc.as400.model.ProcessDefinition;
import com.arley.poc.as400.result.ExecutionResult;
import com.arley.poc.as400.result.ExecutionStatus;
import com.arley.poc.as400.service.ProcessExecutionService;

/**
 * Administra la creación, ejecución y consulta
 * de trabajos AS400 almacenados en memoria.
 */
@Service
public class JobService {

    private final ProcessExecutionService executionService;
    private final TaskExecutor taskExecutor;

    /*
     * Almacenamiento temporal de trabajos.
     *
     * ConcurrentHashMap permite consultar y actualizar
     * trabajos desde diferentes hilos de forma segura.
     */
    private final Map<String, JobView> jobs =
        new ConcurrentHashMap<>();

    public JobService(
        ProcessExecutionService executionService,
        @Qualifier("as400JobExecutor")
        TaskExecutor taskExecutor
    ) {
        this.executionService =
            Objects.requireNonNull(
                executionService,
                "ProcessExecutionService no puede ser nulo."
            );

        this.taskExecutor =
            Objects.requireNonNull(
                taskExecutor,
                "TaskExecutor no puede ser nulo."
            );
    }

    /**
     * Registra el trabajo y lo envía a la cola.
     *
     * Este método no espera a que el AS400 termine.
     */
    public JobView submit(
        ProcessDefinition process
    ) {
        Objects.requireNonNull(
            process,
            "La definición del proceso no puede ser nula."
        );

        String jobId =
            UUID.randomUUID().toString();

        JobView pendingJob =
            new JobView(
                jobId,
                process.id(),
                process.name(),
                JobStatus.PENDING,
                Instant.now(),
                null,
                null,
                null,
                null
            );

        jobs.put(
            jobId,
            pendingJob
        );

        try {
            taskExecutor.execute(
                () -> executeJob(
                    jobId,
                    process
                )
            );
        } catch (RuntimeException exception) {
            /*
             * Si la cola rechaza el trabajo, evitamos dejar
             * un PENDING que nunca será ejecutado.
             */
            jobs.remove(jobId);

            throw exception;
        }

        return pendingJob;
    }

    /**
     * Busca un trabajo por su identificador.
     */
    public Optional<JobView> findById(
        String jobId
    ) {
        if (
            jobId == null
                || jobId.isBlank()
        ) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            jobs.get(jobId)
        );
    }

    /**
     * Método ejecutado por el trabajador de la cola.
     */
    private void executeJob(
        String jobId,
        ProcessDefinition process
    ) {
        markAsRunning(jobId);

        try {
            ExecutionResult executionResult =
                executionService.execute(process);

            JobStatus finalStatus =
                executionResult.status()
                    == ExecutionStatus.FAILED
                        ? JobStatus.FAILED
                        : JobStatus.COMPLETED;

            finishJob(
                jobId,
                finalStatus,
                executionResult,
                executionResult.errorMessage()
            );

        } catch (RuntimeException exception) {
            finishJob(
                jobId,
                JobStatus.FAILED,
                null,
                safeErrorMessage(exception)
            );
        }
    }

    private void markAsRunning(
        String jobId
    ) {
        jobs.computeIfPresent(
            jobId,
            (id, current) ->
                new JobView(
                    current.jobId(),
                    current.processId(),
                    current.processName(),
                    JobStatus.RUNNING,
                    current.createdAt(),
                    Instant.now(),
                    null,
                    null,
                    null
                )
        );
    }

    private void finishJob(
        String jobId,
        JobStatus status,
        ExecutionResult result,
        String errorMessage
    ) {
        jobs.computeIfPresent(
            jobId,
            (id, current) ->
                new JobView(
                    current.jobId(),
                    current.processId(),
                    current.processName(),
                    status,
                    current.createdAt(),
                    current.startedAt(),
                    Instant.now(),
                    result,
                    errorMessage
                )
        );
    }

    private String safeErrorMessage(
        RuntimeException exception
    ) {
        String message =
            exception.getMessage();

        if (
            message == null
                || message.isBlank()
        ) {
            return "La ejecución terminó con un error inesperado.";
        }

        return message;
    }
}

/*
submit()
  → crea UUID
  → guarda PENDING
  → entrega el trabajo al executor
  → responde inmediatamente

executeJob()
  → cambia a RUNNING
  → llama ProcessExecutionService
  → guarda COMPLETED o FAILED
 */