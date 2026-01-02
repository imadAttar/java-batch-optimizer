package com.imadattar.batch.profiling;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Profileur de performance pour batchs.
 *
 * <p>Mesure automatiquement :
 * <ul>
 *     <li>Temps d'exécution total</li>
 *     <li>Consommation mémoire (heap, non-heap)</li>
 *     <li>Throughput (items/seconde)</li>
 * </ul>
 * </p>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 * BatchProfiler profiler = new BatchProfiler();
 * profiler.start();
 *
 * // Votre batch ici
 * processor.process(data, this::processItem);
 *
 * PerformanceMetrics metrics = profiler.stop();
 * System.out.println("Temps: " + metrics.getTotalTimeMs() + "ms");
 * }</pre>
 *
 * @author Imad ATTAR
 * @since 1.0.0
 */
@Slf4j
public class BatchProfiler {

    private final MemoryMXBean memoryBean;
    private long startTime;
    private long startMemory;
    private int itemsProcessed;

    public BatchProfiler() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }

    /**
     * Démarre le profiling.
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.startMemory = getUsedMemory();
        this.itemsProcessed = 0;
        log.debug("Batch profiling started");
    }

    /**
     * Arrête le profiling et retourne les métriques.
     *
     * @return Métriques de performance
     */
    public PerformanceMetrics stop() {
        long endTime = System.currentTimeMillis();
        long endMemory = getUsedMemory();

        long totalTimeMs = endTime - startTime;
        long memoryUsedBytes = endMemory - startMemory;
        double throughput = itemsProcessed > 0
                ? (itemsProcessed / (totalTimeMs / 1000.0))
                : 0.0;

        log.info("Batch profiling stopped: {}ms, {} MB, {} items/s",
                totalTimeMs,
                memoryUsedBytes / (1024 * 1024),
                String.format("%.2f", throughput));

        return PerformanceMetrics.builder()
                .totalTimeMs(totalTimeMs)
                .memoryUsedBytes(memoryUsedBytes)
                .itemsProcessed(itemsProcessed)
                .throughput(throughput)
                .build();
    }

    /**
     * Incrémente le compteur d'items traités.
     *
     * @param count Nombre d'items à ajouter
     */
    public void addProcessedItems(int count) {
        this.itemsProcessed += count;
    }

    /**
     * Récupère la mémoire utilisée (heap + non-heap).
     */
    private long getUsedMemory() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        return heapMemory.getUsed() + nonHeapMemory.getUsed();
    }
}
