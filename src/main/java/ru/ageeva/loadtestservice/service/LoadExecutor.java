package ru.ageeva.loadtestservice.service;

import ru.ageeva.loadtestservice.model.LoadTestStats;

public interface LoadExecutor {
    LoadTestStats executeCreate(int totalRequests);

    LoadTestStats executeRead(int totalRequests);
}
