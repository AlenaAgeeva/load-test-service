package ru.ageeva.loadtestservice.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.ageeva.loadtestservice.model.LoadTestStats;

import java.util.List;
import java.util.stream.LongStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoadControllerIntegrationTest {
    private static WireMockServer wireMockServer;
    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
        registry.add("user-service.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        stubFor(post(urlEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"firstName\":\"loadtest\",\"lastName\":\"Load\",\"passport\":\"Test\"}")));

        List<Long> ids = LongStream.rangeClosed(1, 100).boxed().toList();
        stubFor(get(urlEqualTo("/api/users/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ids.toString())));

        stubFor(get(urlMatching("/api/users/\\d+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"firstName\":\"Ivan\",\"lastName\":\"Ivanov\",\"passport\":\"sfwe54tg\"}")));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
    }

    @Test
    void shouldCreateRecordsSequentially() {
        ResponseEntity<LoadTestStats> response = restTemplate.postForEntity(
                "/api/load/create?mode=sequential&total=5",
                HttpEntity.EMPTY,
                LoadTestStats.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        LoadTestStats stats = response.getBody();
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalRequests()).isEqualTo(5);
        assertThat(stats.getSuccessCount()).isEqualTo(5);
        assertThat(stats.getFailureCount()).isZero();
    }

    @Test
    void shouldCreateRecordsInParallel() {
        ResponseEntity<LoadTestStats> response = restTemplate.postForEntity(
                "/api/load/create?mode=parallel&total=10",
                HttpEntity.EMPTY,
                LoadTestStats.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        LoadTestStats stats = response.getBody();
        assertThat(stats.getTotalRequests()).isEqualTo(10);
        assertThat(stats.getSuccessCount()).isEqualTo(10);
    }

    @Test
    void shouldReadRecordsSequentially() {
        ResponseEntity<LoadTestStats> response = restTemplate.getForEntity(
                "/api/load/read?mode=sequential&total=3",
                LoadTestStats.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        LoadTestStats stats = response.getBody();
        assertThat(stats.getTotalRequests()).isEqualTo(3);
        assertThat(stats.getSuccessCount()).isEqualTo(3);
    }

    @Test
    void shouldReadRecordsInParallel() {
        ResponseEntity<LoadTestStats> response = restTemplate.getForEntity(
                "/api/load/read?mode=parallel&total=20",
                LoadTestStats.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        LoadTestStats stats = response.getBody();
        assertThat(stats.getTotalRequests()).isEqualTo(20);
        assertThat(stats.getSuccessCount()).isEqualTo(20);
    }

    @Test
    void shouldHandleCreateFailures() {
        stubFor(post(urlEqualTo("/api/users"))
                .willReturn(aResponse().withStatus(500)));

        ResponseEntity<LoadTestStats> response = restTemplate.postForEntity(
                "/api/load/create?mode=sequential&total=5",
                HttpEntity.EMPTY,
                LoadTestStats.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        LoadTestStats stats = response.getBody();
        assertThat(stats.getTotalRequests()).isEqualTo(5);
        assertThat(stats.getSuccessCount()).isZero();
        assertThat(stats.getFailureCount()).isEqualTo(5);
    }
}