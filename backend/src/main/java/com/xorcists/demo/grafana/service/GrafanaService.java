package com.xorcists.demo.grafana.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xorcists.demo.DatasourceTypes;
import com.xorcists.demo.grafana.builder.LokiQueryBuilder;
import com.xorcists.demo.grafana.builder.PrometheusQueryBuilder;
import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.dto.GrafanaQueryResponse;
import com.xorcists.demo.grafana.model.Datasource;
import com.xorcists.demo.grafana.model.Folder;
import com.xorcists.demo.grafana.query.QueryClient;
import com.xorcists.demo.grafana.resource.ResourceClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Service for Grafana operations.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @Service, @RequiredArgsConstructor (Lombok)
 * - Quarkus: @ApplicationScoped, @Inject on each field
 * 
 * Key changes:
 * - @Service -> @ApplicationScoped
 * - @RequiredArgsConstructor -> explicit @Inject
 * - tools.jackson.databind.ObjectMapper -> com.fasterxml.jackson.databind.ObjectMapper
 * - org.apache.logging.log4j.Logger -> org.jboss.logging.Logger
 * 
 * Business logic unchanged.
 */
@ApplicationScoped
public class GrafanaService {

    private static final Logger LOG = Logger.getLogger(GrafanaService.class);

    @Inject
    PrometheusQueryBuilder prometheusBuilder;

    @Inject
    LokiQueryBuilder lokiQueryBuilder;

    @Inject
    ResourceClient resourceClient;

    @Inject
    QueryClient queryClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Folder> getFolders() {
        return resourceClient.getFolders();
    }

    public List<Datasource> getDatasources() {
        return resourceClient.getDatasources();
    }

    public GrafanaQueryResponse executePrometheusQuery(String promQl) {
        Datasource prometheusDatasource = resourceClient
                .getDatasourceByType(DatasourceTypes.PROMETHEUS)
                .orElseThrow(() -> new RuntimeException("Prometheus datasource not found"));

        GrafanaQueryRequest request = prometheusBuilder.instantQuery(
                prometheusDatasource.getUid(),
                prometheusDatasource.getId(),
                promQl,
                "now-1h",
                "now");

        try {
            LOG.infof("Grafana Request: %s", objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            LOG.warn("Could not serialize request for logging", e);
        }

        return queryClient.execute(request, prometheusDatasource.getType());
    }

    public GrafanaQueryResponse executeLokiQuery(String logQl) {
        return executeLokiQuery(logQl, null, null);
    }

    /**
     * Executes a Loki query with custom time range.
     *
     * @param logQl the LogQL query expression
     * @param fromMillis start time in epoch milliseconds (null for default)
     * @param toMillis end time in epoch milliseconds (null for default)
     * @return the query response from Grafana
     */
    public GrafanaQueryResponse executeLokiQuery(String logQl, Long fromMillis, Long toMillis) {
        Datasource datasource = resourceClient
                .getDatasourceByType(DatasourceTypes.LOKI)
                .orElseThrow(() -> new RuntimeException("Loki datasource not found"));

        GrafanaQueryRequest request = lokiQueryBuilder.rangeQuery(
                datasource.getUid(),
                datasource.getId(),
                logQl,
                fromMillis,
                toMillis);

        try {
            LOG.infof("Loki query request: from=%s, to=%s, query=%s", 
                request.getFrom(), request.getTo(), logQl);
        } catch (Exception e) {
            LOG.warn("Could not log request details", e);
        }

        GrafanaQueryResponse response = queryClient.execute(request, datasource.getType());
        
        // Log if response contains empty frames
        if (response != null && response.getResults() != null) {
            LOG.debugf("Loki query response received");
        }
        
        return response;
    }

    // TODO: Implement when TempoQueryBuilder is ready
    // public GrafanaQueryResponse executeTempoQuery(String traceQl) {
    //     Datasource datasource = resourceClient
    //             .getDatasourceByType(DatasourceTypes.TEMPO)
    //             .orElseThrow(() -> new RuntimeException("Tempo datasource not found"));
    //
    //     GrafanaQueryRequest request = tempoBuilder.build(datasource, traceQl);
    //     return queryClient.execute(request, datasource.getType());
    // }
}
