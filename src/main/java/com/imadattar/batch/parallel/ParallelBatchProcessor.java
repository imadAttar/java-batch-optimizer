package com.imadattar.batch.parallel;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processeur de batch parallèle pour optimiser le traitement de grandes volumétries.
 *
 * <p>Permet de passer de traitements séquentiels lents (heures) à des traitements
 * parallèles rapides (minutes) grâce à une gestion intelligente des threads.</p>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 * ParallelBatchProcessor processor = ParallelBatchProcessor.builder()
 *     .parallelism(8)
 *     .chunkSize(1000)
 *     .build();
 *
 * List<Result> results = processor.process(data, item -> processItem(item));
 * }</pre>
 *
 * <h2>Cas réel de production</h2>
 * <p>Ce pattern a permis de réduire un batch de réconciliation financière
 * de <strong>15 heures à 10 minutes</strong> (-95%) en production.</p>
 *
 * @author Imad ATTAR
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Builder
public class ParallelBatchProcessor {

    /**
     * Nombre de threads parallèles.
     * Par défaut : nombre de cœurs CPU disponibles.
     */
    @Builder.Default
    private final int parallelism = Runtime.getRuntime().availableProcessors();

    /**
     * Taille de chaque chunk (lot) de traitement.
     * Recommandation : 1000-5000 pour équilibrer parallélisme et overhead.
     */
    @Builder.Default
    private final int chunkSize = 1000;

    /**
     * Stratégie de partitionnement des données.
     * - STATIC : Partitionnement fixe
     * - DYNAMIC : Partitionnement adaptatif (work-stealing)
     */
    @Builder.Default
    private final PartitionStrategy strategy = PartitionStrategy.DYNAMIC;

    /**
     * Traite une liste d'éléments en parallèle.
     *
     * <p>Les éléments sont découpés en chunks, chaque chunk est traité dans un thread séparé,
     * puis les résultats sont agrégés.</p>
     *
     * @param items Liste d'éléments à traiter
     * @param processor Fonction de traitement d'un élément (doit être thread-safe)
     * @param <T> Type des éléments en entrée
     * @param <R> Type des résultats
     * @return Liste des résultats (ordre non garanti)
     * @throws InterruptedException si le traitement est interrompu
     * @throws ExecutionException si une erreur survient pendant le traitement
     */
    public <T, R> List<R> process(List<T> items, Function<T, R> processor)
            throws InterruptedException, ExecutionException {

        if (items == null || items.isEmpty()) {
            log.warn("Empty or null items list provided");
            return new ArrayList<>();
        }

        log.info("Starting parallel batch processing: {} items, parallelism={}, chunkSize={}",
                items.size(), parallelism, chunkSize);

        long startTime = System.currentTimeMillis();

        ExecutorService executor = createExecutor();

        try {
            // Découpe en chunks
            List<List<T>> chunks = partitionList(items, chunkSize);
            log.debug("Partitioned into {} chunks", chunks.size());

            // Soumet chaque chunk comme tâche
            List<Future<List<R>>> futures = new ArrayList<>();
            for (List<T> chunk : chunks) {
                futures.add(executor.submit(() -> processChunk(chunk, processor)));
            }

            // Collecte les résultats
            List<R> results = new ArrayList<>();
            for (Future<List<R>> future : futures) {
                results.addAll(future.get());
            }

            long duration = System.currentTimeMillis() - startTime;
            double throughput = items.size() / (duration / 1000.0);

            log.info("Batch processing completed: {} items in {}ms ({} items/s)",
                    items.size(), duration, String.format("%.2f", throughput));

            return results;

        } finally {
            shutdownExecutor(executor);
        }
    }

    /**
     * Traite un chunk d'éléments.
     */
    private <T, R> List<R> processChunk(List<T> chunk, Function<T, R> processor) {
        log.debug("Processing chunk of {} items", chunk.size());
        return chunk.stream()
                .map(processor)
                .collect(Collectors.toList());
    }

    /**
     * Crée l'ExecutorService selon la stratégie.
     */
    private ExecutorService createExecutor() {
        return switch (strategy) {
            case STATIC -> Executors.newFixedThreadPool(parallelism);
            case DYNAMIC -> Executors.newWorkStealingPool(parallelism);
            default -> Executors.newFixedThreadPool(parallelism);
        };
    }

    /**
     * Arrête proprement l'ExecutorService.
     */
    private void shutdownExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
            log.warn("Executor did not terminate in 5 minutes, forcing shutdown");
            executor.shutdownNow();
        }
    }

    /**
     * Découpe une liste en sous-listes de taille fixe.
     *
     * @param list Liste à découper
     * @param chunkSize Taille de chaque chunk
     * @return Liste de chunks
     */
    private <T> List<List<T>> partitionList(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, list.size());
            chunks.add(list.subList(i, end));
        }
        return chunks;
    }
}
