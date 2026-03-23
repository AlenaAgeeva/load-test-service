package ru.ageeva.loadtestservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ageeva.loadtestservice.model.LoadTestStats;
import ru.ageeva.loadtestservice.service.operation.CreateOperation;
import ru.ageeva.loadtestservice.service.operation.ReadOperation;
import ru.ageeva.loadtestservice.util.StatsUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParallelLoadExecutorTest {
    @Mock
    private StatsUtil statsUtil;
    @Mock
    private CreateOperation createOperation;
    @Mock
    private ReadOperation readOperation;
    private ParallelLoadExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new ParallelLoadExecutor(statsUtil, createOperation, readOperation, 2);
    }

    @Test
    void shouldExecuteCreateInParallel() {
        LoadTestStats expectedStats = new LoadTestStats();
        when(statsUtil.createRecorder()).thenReturn(new org.HdrHistogram.Recorder(1, 60000, 3));
        when(statsUtil.buildStats(any(), anyLong(), anyInt(), anyInt(), anyInt()))
                .thenReturn(expectedStats);
        LoadTestStats result = executor.executeCreate(10);
        assertThat(result).isEqualTo(expectedStats);
        verify(createOperation, times(10)).create();
        verify(readOperation, never()).read();
    }

    @Test
    void shouldExecuteReadInParallel() {
        LoadTestStats expectedStats = new LoadTestStats();
        when(statsUtil.createRecorder()).thenReturn(new org.HdrHistogram.Recorder(1, 60000, 3));
        when(statsUtil.buildStats(any(), anyLong(), anyInt(), anyInt(), anyInt()))
                .thenReturn(expectedStats);
        LoadTestStats result = executor.executeRead(10);
        assertThat(result).isEqualTo(expectedStats);
        verify(readOperation, times(10)).read();
        verify(createOperation, never()).create();
    }
}