package ru.ageeva.loadtestservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ru.ageeva.loadtestservice.model.LoadTestStats;
import ru.ageeva.loadtestservice.service.LoadExecutor;

import java.util.Map;

@RestController
@RequestMapping("/api/load")
@Tag(name = "Load Testing", description = "Endpoints for performing load tests on user-service")
public class LoadController {
    private final Map<String, LoadExecutor> executors;
    private final int defaultRequests;

    @Autowired
    public LoadController(Map<String, LoadExecutor> executors,
                          @Value("${load.requests:100000}") int defaultRequests) {
        this.executors = executors;
        this.defaultRequests = defaultRequests;
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
            throw new IllegalArgumentException("Unknown mode: " + mode);
        }
        int totalRequests = total != null ? total : defaultRequests;
        return executor.executeCreate(totalRequests);
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
            throw new IllegalArgumentException("Unknown mode: " + mode);
        }
        int totalRequests = total != null ? total : defaultRequests;
        return executor.executeRead(totalRequests);
    }
}
