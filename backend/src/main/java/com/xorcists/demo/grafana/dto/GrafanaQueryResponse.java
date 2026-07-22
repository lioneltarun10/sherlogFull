package com.xorcists.demo.grafana.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * Response from Grafana query API.
 * 
 * MIGRATION NOTE:
 * - Changed from tools.jackson.databind.JsonNode to com.fasterxml.jackson.databind.JsonNode
 * - Spring Boot used a different Jackson package path
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrafanaQueryResponse {

    private JsonNode results;

}
