package com.arley.poc.as400.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arley.poc.as400.model.ProcessDefinition;
import com.arley.poc.as400.result.ExecutionResult;
import com.arley.poc.as400.service.ProcessExecutionService;

/**
 * Expone las operaciones relacionadas con la ejecución
 * de procesos AS400.
 */
@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final ProcessExecutionService executionService;

    public ProcessController(
        ProcessExecutionService executionService
    ) {
        this.executionService = executionService;
    }

    /**
     * Recibe una definición de proceso en JSON,
     * la ejecuta y devuelve el resultado estructurado.
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecutionResult> execute(
        @RequestBody ProcessDefinition process
    ) {
        ExecutionResult result =
            executionService.execute(process);

        return ResponseEntity.ok(result);
    }
}