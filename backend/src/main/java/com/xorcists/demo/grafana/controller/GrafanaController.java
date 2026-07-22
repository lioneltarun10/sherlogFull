package com.xorcists.demo.grafana.controller;

import com.xorcists.demo.grafana.dto.GrafanaQueryResponse;
import com.xorcists.demo.grafana.dto.LogsRequest;
import com.xorcists.demo.grafana.model.Datasource;
import com.xorcists.demo.grafana.model.Folder;
import com.xorcists.demo.grafana.service.GrafanaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST resource for Grafana API operations.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @RestController @RequestMapping("/grafana"), @RequiredArgsConstructor
 * - Quarkus: @Path("/grafana"), @Inject
 * 
 * Key changes:
 * - @RestController -> @Path (JAX-RS)
 * - @GetMapping -> @GET @Path
 * - @PostMapping -> @POST @Path
 * - @RequestBody -> not needed (auto-deserialized)
 * - @RequestParam -> @QueryParam
 * - @RequiredArgsConstructor -> @Inject
 * 
 * API contract preserved - same endpoints, same request/response format.
 */
@Path("/grafana")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GrafanaController {

    private static final Logger LOG = Logger.getLogger(GrafanaController.class);

    @Inject
    GrafanaService grafanaService;

    /**
     * GET /grafana/folders
     * Returns list of Grafana folders.
     */
    @GET
    @Path("/folders")
    public List<Folder> getFolders() {
        LOG.debug("Fetching Grafana folders");
        return grafanaService.getFolders();
    }

    /**
     * GET /grafana/datasources
     * Returns list of Grafana datasources.
     */
    @GET
    @Path("/datasources")
    public List<Datasource> getDatasources() {
        LOG.debug("Fetching Grafana datasources");
        return grafanaService.getDatasources();
    }

    /**
     * POST /grafana/metrics?query=<promql>
     * Executes a Prometheus query.
     * 
     * Note: Spring Boot version accepted query as @RequestParam
     */
    @POST
    @Path("/metrics")
    public GrafanaQueryResponse metrics(@QueryParam("query") String query) {
        LOG.infof("Executing Prometheus query: %s", query);
        return grafanaService.executePrometheusQuery(query);
    }

    /**
     * POST /grafana/logs
     * Executes a Loki query.
     * 
     * Request body: { "query": "<logql>" }
     */
    @POST
    @Path("/logs")
    public GrafanaQueryResponse logs(LogsRequest request) {
        LOG.infof("Executing Loki query: %s", request.getQuery());
        return grafanaService.executeLokiQuery(request.getQuery());
    }

    // TODO: Uncomment when Tempo support is implemented
    // @POST
    // @Path("/traces")
    // public GrafanaQueryResponse traces(TracesRequest request) {
    //     LOG.infof("Executing Tempo query: %s", request.getQuery());
    //     return grafanaService.executeTempoQuery(request.getQuery());
    // }
}
