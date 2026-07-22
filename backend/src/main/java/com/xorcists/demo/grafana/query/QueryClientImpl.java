package com.xorcists.demo.grafana.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xorcists.demo.config.GrafanaConfig;
import com.xorcists.demo.grafana.client.BaseGrafanaClient;
import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.dto.GrafanaQueryResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.http.HttpClient;
import java.util.Map;

/**
 * Client for Grafana query API operations.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @Component, extends BaseGrafanaClient, used WebClient's UriBuilder
 * - Quarkus: @ApplicationScoped, uses Map<String,String> for query params
 * 
 * Key changes:
 * - UriBuilder lambda -> Map of query parameters
 * - ParameterizedTypeReference<>() -> TypeReference<>()
 * - Added protected no-args constructor required by Quarkus CDI for proxying
 */
@ApplicationScoped
public class QueryClientImpl extends BaseGrafanaClient implements QueryClient {

    // Required by Quarkus CDI for proxy creation
    protected QueryClientImpl() {
        super(null, null);
    }

    @Inject
    public QueryClientImpl(HttpClient httpClient, GrafanaConfig config) {
        super(httpClient, config);
    }

    @Override
    public GrafanaQueryResponse execute(GrafanaQueryRequest request, String datasourceType) {
        Map<String, String> queryParams = Map.of(
                "ds_type", datasourceType,
                "requestId", "explore"
        );

        return post("/api/ds/query", queryParams, request, new TypeReference<>() {});
    }
}
