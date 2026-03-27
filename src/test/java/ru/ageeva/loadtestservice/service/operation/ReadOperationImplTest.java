package ru.ageeva.loadtestservice.service.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.ageeva.loadtestservice.dto.UserResponseDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadOperationImplTest {
    @Mock
    private RestTemplate restTemplate;
    private ReadOperationImpl readOperation;

    @BeforeEach
    void setUp() {
        readOperation = new ReadOperationImpl(restTemplate, "http://localhost:8080");
        ResponseEntity<List<Long>> responseEntity = ResponseEntity.ok(List.of(1L, 2L, 3L));
        when(restTemplate.exchange(
                eq("http://localhost:8080/api/users/ids"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
    }

    @Test
    void shouldCallRestTemplateToGetIdsAndThenGetUser() {
        readOperation.read();
        verify(restTemplate).exchange(
                eq("http://localhost:8080/api/users/ids"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
        verify(restTemplate).getForObject(
                contains("/api/users/"),
                eq(UserResponseDto.class)
        );
    }
}