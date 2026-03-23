package ru.ageeva.loadtestservice.service.operation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.ageeva.loadtestservice.dto.UserResponseDto;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class ReadOperationImpl implements ReadOperation {
    private final RestTemplate restTemplate;
    private final String userServiceUrl;
    private List<Long> allIds;
    private final Random random = new Random();

    public ReadOperationImpl(RestTemplate restTemplate,
                             @Value("${user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    @Override
    public void read() {
        ensureIdsLoaded();
        long id = allIds.get(random.nextInt(allIds.size()));
        String url = userServiceUrl + "/api/users/" + id;
        restTemplate.getForObject(url, UserResponseDto.class);
    }

    private synchronized void ensureIdsLoaded() {
        if (allIds == null) {
            String idsUrl = userServiceUrl + "/api/users/ids";
            allIds = restTemplate.exchange(idsUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Long>>() {
                    }).getBody();
            if (allIds == null || allIds.isEmpty()) {
                throw new IllegalStateException("No user IDs available");
            }
        }
    }
}
