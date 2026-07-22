package com.xorcists.demo.grafana.builder;

import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.dto.Query;
import com.xorcists.demo.grafana.model.Datasource;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.List;

/**
 * Builder for Loki (logs) queries to Grafana.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @Component
 * - Quarkus: @ApplicationScoped
 * 
 * Business logic unchanged.
 */
@ApplicationScoped
public class LokiQueryBuilder {

    public GrafanaQueryRequest rangeQuery(
            String datasourceUid,
            Integer datasourceId,
            String expr,
            String from,
            String to) {

        Datasource datasource = new Datasource();
        datasource.setType("loki");
        datasource.setUid(datasourceUid);

        Query query = new Query();
        query.setRefId("loki-data-samples");
        query.setExpr(expr);
        query.setQueryType("range");
        query.setMaxLines(10);
        query.setSupportingQueryType("dataSample");
        query.setStep("");
        query.setLegendFormat("");
        query.setDatasource(datasource);
        query.setDatasourceId(datasourceId);
        query.setIntervalMs(3600000);

        GrafanaQueryRequest request = new GrafanaQueryRequest();
        request.setQueries(List.of(query));

        long toLong = System.currentTimeMillis();
        long fromLong = toLong - Duration.ofHours(1).toMillis();

        request.setFrom(String.valueOf(fromLong));
        request.setTo(String.valueOf(toLong));

        return request;
    }
}
