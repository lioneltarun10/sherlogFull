package com.xorcists.demo.grafana.builder;

import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.dto.Query;
import com.xorcists.demo.grafana.model.Datasource;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

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

    private static final Logger LOG = Logger.getLogger(LokiQueryBuilder.class);

    /**
     * Default time window when no time range is provided: 7 days.
     */
    private static final Duration DEFAULT_TIME_WINDOW = Duration.ofDays(7);

    /**
     * Builds a range query for Loki.
     *
     * @param datasourceUid the Loki datasource UID
     * @param datasourceId the Loki datasource ID
     * @param expr the LogQL expression
     * @param fromMillis start time in epoch milliseconds (null for default 7 days ago)
     * @param toMillis end time in epoch milliseconds (null for default now)
     * @return the GrafanaQueryRequest
     */
    public GrafanaQueryRequest rangeQuery(
            String datasourceUid,
            Integer datasourceId,
            String expr,
            Long fromMillis,
            Long toMillis) {

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

        // Use provided time range or default to last 7 days
        long effectiveToMillis = (toMillis != null) ? toMillis : System.currentTimeMillis();
        long effectiveFromMillis = (fromMillis != null) ? fromMillis : (effectiveToMillis - DEFAULT_TIME_WINDOW.toMillis());

        request.setFrom(String.valueOf(effectiveFromMillis));
        request.setTo(String.valueOf(effectiveToMillis));

        LOG.infof("Built Loki query: from=%d, to=%d, expr=%s", 
            effectiveFromMillis, effectiveToMillis, expr);

        return request;
    }
}
