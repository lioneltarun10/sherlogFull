package com.xorcists.demo.grafana.builder;

import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.dto.Query;
import com.xorcists.demo.grafana.model.Datasource;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.List;

/**
 * Builder for Prometheus (metrics) queries to Grafana.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @Component
 * - Quarkus: @ApplicationScoped
 * 
 * Business logic unchanged.
 */
@ApplicationScoped
public class PrometheusQueryBuilder {

    public GrafanaQueryRequest instantQuery(
            String datasourceUid,
            Integer datasourceId,
            String expr,
            String from,
            String to) {

        Datasource datasource = new Datasource();
        datasource.setType("prometheus");
        datasource.setUid(datasourceUid);

        Query rangeQuery = new Query();
        rangeQuery.setRefId("A");
        rangeQuery.setExpr(expr);
        rangeQuery.setRange(true);
        rangeQuery.setInstant(false);
        rangeQuery.setDatasource(datasource);
        rangeQuery.setEditorMode("code");
        rangeQuery.setLegendFormat("__auto__");
        rangeQuery.setExemplar(false);
        rangeQuery.setRequestId("A");
        rangeQuery.setUtcOffsetSec(19800);
        rangeQuery.setInterval("");
        rangeQuery.setDatasourceId(datasourceId);
        rangeQuery.setIntervalMs(15000);
        rangeQuery.setMaxDataPoints(540);

        Query instantQuery = new Query();
        instantQuery.setRefId("A-Instant");
        instantQuery.setExpr(expr);
        instantQuery.setRange(false);
        instantQuery.setInstant(true);
        instantQuery.setDatasource(datasource);
        instantQuery.setEditorMode("code");
        instantQuery.setLegendFormat("__auto__");
        instantQuery.setExemplar(false);
        instantQuery.setRequestId("A");
        instantQuery.setUtcOffsetSec(19800);
        instantQuery.setInterval("");
        instantQuery.setDatasourceId(datasourceId);
        instantQuery.setIntervalMs(15000);
        instantQuery.setMaxDataPoints(540);

        GrafanaQueryRequest request = new GrafanaQueryRequest();
        request.setQueries(List.of(rangeQuery, instantQuery));

        long toLong = System.currentTimeMillis();
        long fromLong = toLong - Duration.ofHours(1).toMillis();

        request.setFrom(String.valueOf(fromLong));
        request.setTo(String.valueOf(toLong));

        return request;
    }
}
