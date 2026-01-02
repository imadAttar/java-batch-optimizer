package com.imadattar.batch.profiling;

import lombok.Builder;
import lombok.Getter;

/**
 * Métriques de performance pour un batch.
 *
 * @author Imad ATTAR
 * @since 1.0.0
 */
@Getter
@Builder
public class PerformanceMetrics {

    /**
     * Temps total d'exécution en millisecondes.
     */
    private final long totalTimeMs;

    /**
     * Mémoire utilisée en bytes.
     */
    private final long memoryUsedBytes;

    /**
     * Nombre d'items traités.
     */
    private final int itemsProcessed;

    /**
     * Throughput (items par seconde).
     */
    private final double throughput;

    /**
     * Retourne le temps total en secondes.
     */
    public double getTotalTimeSeconds() {
        return totalTimeMs / 1000.0;
    }

    /**
     * Retourne la mémoire utilisée en MB.
     */
    public double getMemoryUsedMB() {
        return memoryUsedBytes / (1024.0 * 1024.0);
    }

    @Override
    public String toString() {
        return String.format(
                "PerformanceMetrics{totalTime=%dms (%.2fs), memory=%.2fMB, items=%d, throughput=%.2f items/s}",
                totalTimeMs,
                getTotalTimeSeconds(),
                getMemoryUsedMB(),
                itemsProcessed,
                throughput
        );
    }
}
