package com.arley.poc.as400.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.arley.poc.as400.job.JobService;
import com.arley.poc.as400.job.JobView;
import com.arley.poc.as400.model.ProcessDefinition;

/**
 * API para enviar procesos a la cola y consultar
 * posteriormente su estado.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(
        JobService jobService
    ) {
        this.jobService = jobService;
    }

    /**
     * Registra un proceso para ejecución asíncrona.
     */
    @PostMapping
    public ResponseEntity<JobView> submit(
        @RequestBody ProcessDefinition process
    ) {
        JobView job =
            jobService.submit(process);

        URI location =
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{jobId}")
                .buildAndExpand(job.jobId())
                .toUri();

        return ResponseEntity
            .accepted()
            .location(location)
            .body(job);
    }

    /**
     * Consulta el estado y resultado de un trabajo.
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<JobView> findById(
        @PathVariable String jobId
    ) {
        return jobService
            .findById(jobId)
            .map(ResponseEntity::ok)
            .orElseGet(
                () -> ResponseEntity
                    .notFound()
                    .build()
            );
    }
}