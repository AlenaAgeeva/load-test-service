package ru.ageeva.loadtestservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ru.ageeva.loadtestservice.model.LoadTestStats;
import ru.ageeva.loadtestservice.service.LoadExecutor;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/load")
@Tag(name = "Load Testing", description = "Endpoints for performing load tests on user-service")
public class LoadController {
    private final Map<String, LoadExecutor> executors;
    private final int readRequests;
    private final int createRequests;

    @Autowired
    public LoadController(Map<String, LoadExecutor> executors,
                          @Value("${load.readRequests:1000000}") int readRequests,
                          @Value("${load.createRequests:100000}") int createRequests) {
        this.executors = executors;
        this.readRequests = readRequests;
        this.createRequests = createRequests;
    }

    @Operation(summary = "Create users",
            description = "Performs a specified number of user creation requests. Mode can be 'sequential' or 'parallel'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Load test completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid mode parameter"),
            @ApiResponse(responseCode = "500", description = "Internal error during test execution")
    })
    @PostMapping("/create")
    public LoadTestStats createRecords(
            @Parameter(description = "Execution mode: sequential or parallel", example = "parallel")
            @RequestParam(defaultValue = "parallel") String mode,
            @Parameter(description = "Number of requests to perform (default: 100000)", example = "100000")
            @RequestParam(required = false) Integer total) {
        LoadExecutor executor = executors.get(mode);
        if (executor == null) {
            log.error("Invalid mode requested: {}. Available modes: {}", mode, executors.keySet());
            throw new IllegalArgumentException("Unknown mode: " + mode);
        }
        int totalRequests = total != null ? total : createRequests;
        try {
            LoadTestStats stats = executor.executeCreate(totalRequests);
            return stats;
        } catch (Exception e) {
            log.error("Create test failed - mode: {}, total requests: {}", mode, totalRequests, e);
            throw e;
        }
    }

    @Operation(summary = "Read users by random IDs",
            description = "Performs a specified number of user read requests by randomly selecting existing IDs. " +
                    "Mode can be 'sequential' or 'parallel'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Load test completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid mode parameter or no users exist"),
            @ApiResponse(responseCode = "500", description = "Internal error during test execution")
    })
    @GetMapping("/read")
    public LoadTestStats readRecords(
            @Parameter(description = "Execution mode: sequential or parallel", example = "parallel")
            @RequestParam(defaultValue = "sequential") String mode,
            @Parameter(description = "Number of requests to perform (default: 1000000)", example = "1000000")
            @RequestParam(required = false) Integer total) {
        LoadExecutor executor = executors.get(mode);
        if (executor == null) {
            log.error("Invalid mode requested: {}. Available modes: {}", mode, executors.keySet());
            throw new IllegalArgumentException("Unknown mode: " + mode);
        }
        int totalRequests = total != null ? total : readRequests;
        try {
            LoadTestStats stats = executor.executeCreate(totalRequests);
            return stats;
        } catch (Exception e) {
            log.error("Read test failed - mode: {}, total requests: {}", mode, totalRequests, e);
            throw e;
        }
    }
}
