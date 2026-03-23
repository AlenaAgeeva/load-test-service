package ru.ageeva.loadtestservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoadTestStats {
    private long totalTimeMillis;
    private double medianMillis;
    private double percentile95Millis;
    private double percentile99Millis;
    private int totalRequests;
    private int successCount;
    private int failureCount;
}
