package ru.ageeva.loadtestservice.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ageeva.loadtestservice.model.LoadTestStats;
import ru.ageeva.loadtestservice.service.operation.CreateOperation;
import ru.ageeva.loadtestservice.service.operation.ReadOperation;
import ru.ageeva.loadtestservice.util.StatsUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("parallel")
public class ParallelLoadExecutor extends AbstractBaseLoadExecutor {
    private final CreateOperation createOperation;
    private final ReadOperation readOperation;
    private final int threads;
    private final ExecutorService executor;

    public ParallelLoadExecutor(StatsUtil statsUtil,
                                CreateOperation createOperation,
                                ReadOperation readOperation,
                                @Value("${load.threads:100}") int threads) {
        super(statsUtil);
        this.createOperation = createOperation;
        this.readOperation = readOperation;
        this.threads = threads;
        this.executor = Executors.newFixedThreadPool(threads);
    }

    @Override
    public LoadTestStats executeCreate(int totalRequests) {
        return executeWithStats(totalRequests, createOperation::create);
    }

    @Override
    public LoadTestStats executeRead(int totalRequests) {
        return executeWithStats(totalRequests, readOperation::read);
    }

    @Override
    protected void runRequests(int totalRequests, RequestTask task) {
        int batchSize = totalRequests / threads;
        CountDownLatch latch = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            int startIdx = t * batchSize;
            int endIdx = (t == threads - 1) ? totalRequests : startIdx + batchSize;
            executor.submit(() -> {
                for (int i = startIdx; i < endIdx; i++) {
                    task.execute();
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for load test completion", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}