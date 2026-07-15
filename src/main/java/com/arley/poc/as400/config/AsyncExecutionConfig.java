package com.arley.poc.as400.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuración del trabajador encargado de ejecutar
 * los procesos AS400 en segundo plano.
 */
@Configuration
public class AsyncExecutionConfig {

    @Bean(name = "as400JobExecutor")
    public TaskExecutor as400JobExecutor() {
        ThreadPoolTaskExecutor executor =
            new ThreadPoolTaskExecutor();

        /*
         * Un solo trabajador por ahora.
         *
         * Si un proceso está ejecutándose, los siguientes
         * permanecen en la cola.
         */
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);

        /*
         * Máximo de trabajos esperando en memoria.
         */
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix(
            "as400-job-"
        );

        /*
         * Al apagar la aplicación, permitimos que el trabajo
         * actual tenga tiempo de finalizar limpiamente.
         */
        executor.setWaitForTasksToCompleteOnShutdown(
            true
        );

        executor.setAwaitTerminationSeconds(
            30
        );

        executor.initialize();

        return executor;
    }
}