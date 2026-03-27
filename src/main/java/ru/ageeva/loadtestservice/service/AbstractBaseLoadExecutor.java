package ru.ageeva.loadtestservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.HdrHistogram.Recorder;
import ru.ageeva.loadtestservice.model.LoadTestStats;
import ru.ageeva.loadtestservice.util.StatsUtil;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBaseLoadExecutor implements LoadExecutor {
    protected final StatsUtil statsUtil;

    protected abstract void runRequests(int totalRequests, RequestTask task);

    @FunctionalInterface
    protected interface RequestTask {
        void execute();
    }

    protected LoadTestStats executeWithStats(int totalRequests, RequestTask task) {
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);
        Recorder recorder = statsUtil.createRecorder();
        long startOverall = System.currentTimeMillis();
        runRequests(totalRequests, () -> {
            long startNano = System.nanoTime();
            try {
                task.execute();
                success.incrementAndGet();
            } catch (Exception e) {
                log.error("Increment stats failed - number: {}, ", failure, e);
                failure.incrementAndGet();
            } finally {
                long durationMicros = (System.nanoTime() - startNano) / 1000;
                recorder.recordValue(durationMicros / 1000);
            }
        });
        long totalTime = System.currentTimeMillis() - startOverall;
        return statsUtil.buildStats(recorder, totalTime, totalRequests, success.get(), failure.get());
    }
}