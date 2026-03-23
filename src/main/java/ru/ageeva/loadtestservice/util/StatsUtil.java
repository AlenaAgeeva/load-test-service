package ru.ageeva.loadtestservice.util;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;
import org.springframework.stereotype.Component;
import ru.ageeva.loadtestservice.model.LoadTestStats;

@Component
public class StatsUtil {
    private static final long LOWEST_TRACKABLE_VALUE = 1;
    private static final long HIGHEST_TRACKABLE_VALUE = 60_000;
    private static final int NUMBER_OF_SIGNIFICANT_VALUE_DIGITS = 3;

    public Recorder createRecorder() {
        return new Recorder(LOWEST_TRACKABLE_VALUE, HIGHEST_TRACKABLE_VALUE, NUMBER_OF_SIGNIFICANT_VALUE_DIGITS);
    }

    public LoadTestStats buildStats(Recorder recorder, long totalTimeMillis, int totalRequests,
                                    int success, int failure) {
        Histogram histogram = recorder.getIntervalHistogram();
        LoadTestStats stats = new LoadTestStats();
        stats.setTotalTimeMillis(totalTimeMillis);
        stats.setTotalRequests(totalRequests);
        stats.setSuccessCount(success);
        stats.setFailureCount(failure);
        stats.setMedianMillis(histogram.getValueAtPercentile(50.0));
        stats.setPercentile95Millis(histogram.getValueAtPercentile(95.0));
        stats.setPercentile99Millis(histogram.getValueAtPercentile(99.0));
        return stats;
    }
}
