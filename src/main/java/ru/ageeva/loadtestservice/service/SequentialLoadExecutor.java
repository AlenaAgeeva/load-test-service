package ru.ageeva.loadtestservice.service;

import org.springframework.stereotype.Service;
import ru.ageeva.loadtestservice.model.LoadTestStats;
import ru.ageeva.loadtestservice.service.operation.CreateOperation;
import ru.ageeva.loadtestservice.service.operation.ReadOperation;
import ru.ageeva.loadtestservice.util.StatsUtil;

@Service("sequential")
public class SequentialLoadExecutor extends AbstractBaseLoadExecutor {
    private final CreateOperation createOperation;
    private final ReadOperation readOperation;

    public SequentialLoadExecutor(StatsUtil statsUtil,
                                  CreateOperation createOperation,
                                  ReadOperation readOperation) {
        super(statsUtil);
        this.createOperation = createOperation;
        this.readOperation = readOperation;
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
        for (int i = 0; i < totalRequests; i++) {
            task.execute();
        }
    }
}