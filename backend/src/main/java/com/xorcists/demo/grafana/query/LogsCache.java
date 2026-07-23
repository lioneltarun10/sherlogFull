package com.xorcists.demo.grafana.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xorcists.demo.grafana.dto.GrafanaQueryResponse;

import java.io.InputStream;

public final class LogsCache {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LogsCache() {
    }

    public static GrafanaQueryResponse getResponse(String fileName) {
        try (InputStream is = LogsCache.class.getResourceAsStream("/mock/" + fileName)) {

            if (is == null) {
                throw new IllegalArgumentException("Mock file not found on classpath: /mock/" + fileName);
            }

            JsonNode root = OBJECT_MAPPER.readTree(is);

            GrafanaQueryResponse response = new GrafanaQueryResponse();
            response.setResults(root.get("results"));

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Unable to load mock file: " + fileName, e);
        }
    }
}
