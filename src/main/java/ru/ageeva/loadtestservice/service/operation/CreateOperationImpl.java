package ru.ageeva.loadtestservice.service.operation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.ageeva.loadtestservice.dto.UserCreateDto;
import ru.ageeva.loadtestservice.dto.UserResponseDto;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class CreateOperationImpl implements CreateOperation {
    private final RestTemplate restTemplate;
    private final String userServiceUrl;
    private final AtomicLong counter = new AtomicLong(1);
    private final long startTime = System.currentTimeMillis();

    public CreateOperationImpl(RestTemplate restTemplate,
                               @Value("${user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    @Override
    public void create() {
        String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                .path("/api/users")
                .build()
                .toUriString();
        String passport = "loadtest_" + startTime + "_" + counter.getAndIncrement();
        UserCreateDto dto = new UserCreateDto("Load", "Test", passport);
        try {
            restTemplate.postForObject(url, dto, UserResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to create user with passport: {}", passport, e);
            throw e;
        }
    }
}
