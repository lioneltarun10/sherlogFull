package com.xorcists.demo.grafana.dto;

import lombok.Data;

/**
 * Request DTO for metrics query.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 */
@Data
public class MetricsRequest {

    private String query;
}
