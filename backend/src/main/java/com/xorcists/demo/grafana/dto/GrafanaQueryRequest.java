package com.xorcists.demo.grafana.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Request body for Grafana query API.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrafanaQueryRequest {

    private List<Query> queries;

    private String from;

    private String to;
}
