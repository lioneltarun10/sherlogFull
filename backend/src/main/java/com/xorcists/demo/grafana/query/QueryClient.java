package com.xorcists.demo.grafana.query;

import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.dto.GrafanaQueryResponse;

/**
 * Interface for Grafana query operations.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 * - Interface remains the same
 */
public interface QueryClient {

    GrafanaQueryResponse execute(
            GrafanaQueryRequest request,
            String datasourceType);

}
