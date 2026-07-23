package com.xorcists.demo.grafana.query;

import com.xorcists.demo.grafana.dto.GrafanaQueryRequest;
import com.xorcists.demo.grafana.dto.GrafanaQueryResponse;
import com.xorcists.demo.grafana.dto.Query;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MockCache {

    public static final boolean ENABLED = false;

    // 32-char hex traceId
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("(?i)\\b([a-f0-9]{32})\\b");

    private static final String DEFAULT_LOKI_FILE = "logs-4406591402de0f67c5cd4407c2a9b809.json";

    private static final Map<String, String> TRACE_ID_TO_FILE = Map.of(
            "4406591402de0f67c5cd4407c2a9b809", "logs-4406591402de0f67c5cd4407c2a9b809.json",
            "04343212cace6af15d73f26ed65a0499", "logs-04343212cace6af15d73f26ed65a0499.json",
            "0d1406aadc172df9025eb4c47e4c0ffa", "logs-0d1406aadc172df9025eb4c47e4c0ffa.json"
    );

    private MockCache() {
    }

    public static GrafanaQueryResponse getResponse(
            String datasourceType,
            GrafanaQueryRequest request) {

        switch (datasourceType.toLowerCase()) {
            case "loki":
                return LogsCache.getResponse(resolveLokiMockFile(request));

            // case "prometheus": ...
            // case "tempo": ...

            default:
                throw new IllegalArgumentException(
                        "No mock configured for datasource: " + datasourceType);
        }
    }

    private static String resolveLokiMockFile(GrafanaQueryRequest request) {
        String expr = extractLastExpr(request);
        String traceId = extractLastTraceId(expr);

        System.out.println(traceId);

        if (traceId == null) {
            return DEFAULT_LOKI_FILE;
        }

        return TRACE_ID_TO_FILE.getOrDefault(traceId, "logs-" + traceId + ".json");
    }

    private static String extractLastExpr(GrafanaQueryRequest request) {
        if (request == null) {
            return null;
        }

        List<Query> queries = request.getQueries();
        if (queries == null || queries.isEmpty()) {
            return null;
        }

        Query lastQuery = queries.get(queries.size() - 1);
        return lastQuery != null ? lastQuery.getExpr() : null;
    }

    private static String extractLastTraceId(String expr) {
        if (expr == null || expr.isBlank()) {
            return null;
        }

        Matcher matcher = TRACE_ID_PATTERN.matcher(expr);
        String last = null;
        while (matcher.find()) {
            last = matcher.group(1).toLowerCase();
        }
        return last;
    }
}
