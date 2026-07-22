package com.xorcists.demo.grafana.builder;

import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.model.Datasource;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Builder for Tempo (traces) queries to Grafana.
 * 
 * MIGRATION NOTE:
 * - Was commented out in Spring Boot project
 * - Placeholder for future implementation
 */
@ApplicationScoped
public class TempoQueryBuilder {

    public GrafanaQueryRequest build(
            Datasource datasource,
            String query) {

        // TODO: Implement Tempo query building
        throw new UnsupportedOperationException("Tempo query builder not yet implemented");
    }

}
