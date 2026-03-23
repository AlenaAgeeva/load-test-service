package ru.ageeva.loadtestservice.service.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        when(restTemplate.getForObject(eq("http://localhost:8080/api/users/ids"), eq(List.class)))
                .thenReturn(List.of(1L, 2L, 3L));
    }

    @Test
    void shouldCallRestTemplateToGetIdsAndThenGetUser() {
        readOperation.read();
        verify(restTemplate).getForObject("http://localhost:8080/api/users/ids", List.class);
        verify(restTemplate).getForObject(anyString(), eq(UserResponseDto.class));
        verify(restTemplate).getForObject(contains("/api/users/ids"), eq(List.class));
        verify(restTemplate).getForObject(contains("/api/users/"), eq(UserResponseDto.class));

    }
}