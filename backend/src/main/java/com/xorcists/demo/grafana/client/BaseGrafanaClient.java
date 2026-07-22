package com.xorcists.demo.grafana.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xorcists.demo.config.GrafanaClientProducer;
import com.xorcists.demo.config.GrafanaConfig;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base client for Grafana API calls.
 * 
 * MIGRATION NOTE:
 * This class was completely rewritten for Quarkus.
 * 
 * Spring Boot version:
 * - Used reactive WebClient from Spring WebFlux
 * - Used ParameterizedTypeReference<T> for generic type handling
 * - Used ExchangeFilterFunction for request/response logging
 * - Blocking calls with .block()
 * 
 * Quarkus version:
 * - Uses java.net.http.HttpClient (simpler, synchronous)
 * - Uses Jackson TypeReference<T> for generic type handling
 * - Manual request/response logging
 * - Direct blocking calls
 * 
 * Why the change:
 * - Spring WebFlux is not available in Quarkus
 * - java.net.http.HttpClient is standard Java 11+ API
 * - For reactive in Quarkus, Mutiny or Vert.x Web Client would be used
 * - The original code used .block() anyway, so reactive wasn't really needed
 */
public abstract class BaseGrafanaClient {

    private static final Logger LOG = Logger.getLogger(BaseGrafanaClient.class);

    protected final HttpClient httpClient;
    protected final GrafanaConfig config;
    protected final ObjectMapper objectMapper;

    protected BaseGrafanaClient(HttpClient httpClient, GrafanaConfig config) {
        this.httpClient = httpClient;
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Perform a GET request.
     */
    protected <T> T get(String path, TypeReference<T> responseType) {
        String url = config.baseUrl() + path;

        LOG.debugf("===== REQUEST =====");
        LOG.debugf("GET %s", url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .header("Cookie", GrafanaClientProducer.buildCookieHeader(config))
                    .timeout(Duration.ofMillis(config.readTimeout()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());

            LOG.debugf("===== RESPONSE =====");
            LOG.debugf("Status: %d", response.statusCode());

            if (response.statusCode() >= 400) {
                LOG.errorf("Error response: %s", response.body());
                throw new RuntimeException("HTTP error " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), responseType);

        } catch (Exception e) {
            LOG.errorf(e, "Error executing GET request to %s", url);
            throw new RuntimeException("Failed to execute GET request", e);
        }
    }

    /**
     * Perform a POST request with query parameters.
     */
    protected <T, R> R post(String path, Map<String, String> queryParams, T requestBody, 
                            TypeReference<R> responseType) {
        
        String queryString = queryParams.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + 
                         URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String url = config.baseUrl() + path + (queryString.isEmpty() ? "" : "?" + queryString);

        LOG.debugf("===== REQUEST =====");
        LOG.debugf("POST %s", url);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            LOG.debugf("Request body: %s", jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Cookie", GrafanaClientProducer.buildCookieHeader(config))
                    .timeout(Duration.ofMillis(config.readTimeout()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());

            LOG.debugf("===== RESPONSE =====");
            LOG.debugf("Status: %d", response.statusCode());

            if (response.statusCode() >= 400) {
                LOG.errorf("Status Code: %d", response.statusCode());
                LOG.errorf("Response Body: %s", response.body());
                throw new RuntimeException("HTTP error " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), responseType);

        } catch (Exception e) {
            LOG.errorf(e, "Error executing POST request to %s", url);
            throw new RuntimeException("Failed to execute POST request", e);
        }
    }

}
