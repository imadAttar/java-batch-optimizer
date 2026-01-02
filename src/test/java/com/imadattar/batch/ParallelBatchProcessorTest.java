package com.imadattar.batch;

import com.imadattar.batch.parallel.ParallelBatchProcessor;
import com.imadattar.batch.parallel.PartitionStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour ParallelBatchProcessor.
 *
 * @author Imad ATTAR
 */
class ParallelBatchProcessorTest {

    @Test
    void shouldProcessItemsInParallel() throws ExecutionException, InterruptedException {
        // Given
        ParallelBatchProcessor processor = ParallelBatchProcessor.builder()
                .parallelism(4)
                .chunkSize(100)
                .strategy(PartitionStrategy.DYNAMIC)
                .build();

        List<Integer> input = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            input.add(i);
        }

        // When
        List<Integer> results = processor.process(input, item -> item * 2);

        // Then
        assertThat(results).hasSize(1000);
        assertThat(results).contains(0, 2, 4, 1998);
    }

    @Test
    void shouldHandleEmptyList() throws ExecutionException, InterruptedException {
        // Given
        ParallelBatchProcessor processor = ParallelBatchProcessor.builder().build();
        List<Integer> input = new ArrayList<>();

        // When
        List<Integer> results = processor.process(input, item -> item * 2);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldProcessSingleItem() throws ExecutionException, InterruptedException {
        // Given
        ParallelBatchProcessor processor = ParallelBatchProcessor.builder().build();
        List<Integer> input = List.of(42);

        // When
        List<Integer> results = processor.process(input, item -> item * 2);

        // Then
        assertThat(results).containsExactly(84);
    }

    @Test
    void shouldHandleLargeDataset() throws ExecutionException, InterruptedException {
        // Given
        ParallelBatchProcessor processor = ParallelBatchProcessor.builder()
                .parallelism(8)
                .chunkSize(1000)
                .build();

        List<Integer> input = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            input.add(i);
        }

        // When
        long startTime = System.currentTimeMillis();
        List<Integer> results = processor.process(input, item -> item * 2);
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertThat(results).hasSize(100_000);
        System.out.println("Processed 100K items in " + duration + "ms");
        assertThat(duration).isLessThan(5000); // Should be very fast
    }
}
