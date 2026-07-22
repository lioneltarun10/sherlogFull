package com.xorcists.demo.grafana.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xorcists.demo.config.GrafanaConfig;
import com.xorcists.demo.grafana.client.BaseGrafanaClient;
import com.xorcists.demo.grafana.model.Dashboard;
import com.xorcists.demo.grafana.model.Datasource;
import com.xorcists.demo.grafana.model.Folder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

/**
 * Client for Grafana resource API operations.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @Component, extends BaseGrafanaClient, constructor with WebClient
 * - Quarkus: @ApplicationScoped, extends BaseGrafanaClient, uses @Inject
 * 
 * Key changes:
 * - ParameterizedTypeReference<>() -> TypeReference<>()
 * - Constructor receives HttpClient and GrafanaConfig instead of just WebClient
 * - Added protected no-args constructor required by Quarkus CDI for proxying
 */
@ApplicationScoped
public class ResourceClientImpl extends BaseGrafanaClient implements ResourceClient {

    // Required by Quarkus CDI for proxy creation
    protected ResourceClientImpl() {
        super(null, null);
    }

    @Inject
    public ResourceClientImpl(HttpClient httpClient, GrafanaConfig config) {
        super(httpClient, config);
    }

    @Override
    public List<Folder> getFolders() {
        return get("/api/folders", new TypeReference<>() {});
    }

    @Override
    public List<Datasource> getDatasources() {
        return get("/api/datasources", new TypeReference<>() {});
    }

    @Override
    public List<Dashboard> getDashboards() {
        List<Dashboard> dashboards = get("/api/search", new TypeReference<>() {});

        return dashboards.stream()
                .filter(d -> "dash-db".equals(d.getType()))
                .toList();
    }

    @Override
    public Optional<Datasource> getDatasourceByType(String type) {
        return getDatasources()
                .stream()
                .filter(ds -> type.equalsIgnoreCase(ds.getType()))
                .findFirst();
    }

}
