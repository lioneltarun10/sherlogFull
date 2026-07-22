package com.xorcists.demo.grafana;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Integration tests for Grafana API.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @SpringBootTest
 * - Quarkus: @QuarkusTest
 * 
 * Note: These tests require a running Grafana instance with valid credentials.
 * Disable in CI/CD or use test containers.
 */
@QuarkusTest
@Disabled("Requires valid Grafana credentials - enable for manual testing")
class GrafanaControllerTest {

    @Test
    void testGetFolders() {
        given()
            .when()
                .get("/grafana/folders")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testGetDatasources() {
        given()
            .when()
                .get("/grafana/datasources")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testMetricsQuery() {
        given()
            .queryParam("query", "up")
            .when()
                .post("/grafana/metrics")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("results", notNullValue());
    }

    @Test
    void testLogsQuery() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"query\": \"{job=\\\"varlogs\\\"}\"}")
            .when()
                .post("/grafana/logs")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("results", notNullValue());
    }

}
