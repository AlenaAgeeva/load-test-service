package ru.ageeva.loadtestservice.service.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import ru.ageeva.loadtestservice.dto.UserCreateDto;
import ru.ageeva.loadtestservice.dto.UserResponseDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateOperationImplTest {
    @Mock
    private RestTemplate restTemplate;
    private CreateOperationImpl createOperation;

    @BeforeEach
    void setUp() {
        createOperation = new CreateOperationImpl(restTemplate, "http://localhost:8080");
    }

    @Test
    void shouldCallRestTemplate() {
        createOperation.create();
        verify(restTemplate).postForObject(
                eq("http://localhost:8080/api/users"),
                any(UserCreateDto.class),
                eq(UserResponseDto.class)
        );
    }
}