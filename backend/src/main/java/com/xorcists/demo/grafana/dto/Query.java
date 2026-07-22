package com.xorcists.demo.grafana.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xorcists.demo.grafana.model.Datasource;
import lombok.Data;

/**
 * Query object for Grafana API requests.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 * - Lombok and Jackson work identically in Quarkus
 */
@Data
public class Query {

    private String refId;

    private String expr;

    private Boolean range;

    private Boolean instant;

    private Datasource datasource;

    private String editorMode;

    private String legendFormat;

    private Boolean exemplar;

    private String requestId;

    private Integer utcOffsetSec;

    private String interval;

    private Integer datasourceId;

    private Integer intervalMs;

    private Integer maxDataPoints;

    private String queryType;

    private Integer maxLines;

    private String supportingQueryType;

    private String step;

}
