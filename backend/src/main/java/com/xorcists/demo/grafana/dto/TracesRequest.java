package com.xorcists.demo.grafana.dto;

import lombok.Data;

/**
 * Request DTO for traces query.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 */
@Data
public class TracesRequest {

    private String query;

}
