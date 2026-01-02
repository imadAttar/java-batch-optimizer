import com.imadattar.batch.parallel.ParallelBatchProcessor;
import com.imadattar.batch.parallel.PartitionStrategy;
import com.imadattar.batch.profiling.BatchProfiler;
import com.imadattar.batch.profiling.PerformanceMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Exemple basique d'utilisation du Java Batch Optimizer.
 *
 * Démontre comment passer d'un batch séquentiel (lent) à un batch parallèle (rapide).
 *
 * @author Imad ATTAR
 */
public class BasicExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=== Java Batch Optimizer - Basic Example ===\n");

        // Génération de données de test
        List<Integer> data = generateTestData(100_000);
        System.out.println("Generated " + data.size() + " test items\n");

        // ❌ AVANT : Traitement séquentiel
        System.out.println("--- Sequential Processing (BEFORE) ---");
        long sequentialTime = measureSequentialProcessing(data);
        System.out.println("Sequential processing time: " + sequentialTime + "ms\n");

        // ✅ APRÈS : Traitement parallèle
        System.out.println("--- Parallel Processing (AFTER) ---");
        long parallelTime = measureParallelProcessing(data);
        System.out.println("Parallel processing time: " + parallelTime + "ms\n");

        // Calcul du gain
        double improvement = ((double) (sequentialTime - parallelTime) / sequentialTime) * 100;
        System.out.println("=== Results ===");
        System.out.println("Performance improvement: " + String.format("%.1f%%", improvement));
        System.out.println("Speed-up factor: " + String.format("%.1fx", (double) sequentialTime / parallelTime));
    }

    /**
     * Génère des données de test.
     */
    private static List<Integer> generateTestData(int size) {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(i);
        }
        return data;
    }

    /**
     * Traitement séquentiel (AVANT).
     */
    private static long measureSequentialProcessing(List<Integer> data) {
        long startTime = System.currentTimeMillis();

        List<Integer> results = new ArrayList<>();
        for (Integer item : data) {
            results.add(processItem(item));
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Processed " + results.size() + " items");
        return duration;
    }

    /**
     * Traitement parallèle (APRÈS).
     */
    private static long measureParallelProcessing(List<Integer> data)
            throws ExecutionException, InterruptedException {

        BatchProfiler profiler = new BatchProfiler();
        profiler.start();

        ParallelBatchProcessor processor = ParallelBatchProcessor.builder()
                .parallelism(8)  // 8 threads
                .chunkSize(1000) // 1000 items par chunk
                .strategy(PartitionStrategy.DYNAMIC)
                .build();

        List<Integer> results = processor.process(data, BasicExample::processItem);

        PerformanceMetrics metrics = profiler.stop();

        System.out.println("Processed " + results.size() + " items");
        System.out.println("Throughput: " + String.format("%.2f", metrics.getThroughput()) + " items/s");
        System.out.println("Memory used: " + String.format("%.2f", metrics.getMemoryUsedMB()) + " MB");

        return metrics.getTotalTimeMs();
    }

    /**
     * Simule le traitement d'un item (avec calcul CPU-intensif).
     */
    private static Integer processItem(Integer item) {
        // Simulation d'un traitement complexe
        int result = item;
        for (int i = 0; i < 100; i++) {
            result = (result * 2) % 1000000;
        }
        return result;
    }
}
