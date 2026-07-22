package com.xorcists.demo.grafana.dto;

import lombok.Data;

/**
 * Request DTO for logs query.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 */
@Data
public class LogsRequest {

    private String query;

}
