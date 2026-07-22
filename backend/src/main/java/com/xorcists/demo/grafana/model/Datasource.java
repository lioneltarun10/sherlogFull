package com.xorcists.demo.grafana.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Grafana Datasource model.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 * - Lombok @Data works the same in Quarkus
 * - Jackson annotations work the same
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Datasource {

    private Integer id;

    private String uid;

    private String name;

    private String type;

    private String url;

    private String access;

    private Boolean isDefault;

}
