package com.xorcists.demo.grafana.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Grafana Dashboard model.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 * - Lombok @Data works the same in Quarkus
 * - Jackson annotations work the same
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dashboard {

    private Integer id;

    private String uid;

    private String title;

    private String type;

    private String folderTitle;

    private String url;
}
