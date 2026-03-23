package ru.ageeva.loadtestservice.util;

import org.HdrHistogram.Recorder;
import org.junit.jupiter.api.Test;
import ru.ageeva.loadtestservice.model.LoadTestStats;

import static org.assertj.core.api.Assertions.assertThat;

class StatsUtilTest {
    private final StatsUtil statsUtil = new StatsUtil();

    @Test
    void shouldCreateRecorder() {
        Recorder recorder = statsUtil.createRecorder();
        assertThat(recorder).isNotNull();
    }

    @Test
    void shouldBuildStats() {
        Recorder recorder = statsUtil.createRecorder();
        recorder.recordValue(10);
        recorder.recordValue(20);
        recorder.recordValue(30);
        recorder.recordValue(100);
        recorder.recordValue(200);
        LoadTestStats stats = statsUtil.buildStats(recorder, 5000L, 5, 5, 0);
        assertThat(stats.getTotalRequests()).isEqualTo(5);
        assertThat(stats.getSuccessCount()).isEqualTo(5);
        assertThat(stats.getFailureCount()).isZero();
        assertThat(stats.getMedianMillis()).isGreaterThan(0);
        assertThat(stats.getPercentile95Millis()).isGreaterThan(0);
        assertThat(stats.getPercentile99Millis()).isGreaterThan(0);
    }
}