package com.xorcists.demo.grafana.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Grafana Folder model.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 * - Lombok @Data works the same in Quarkus
 * - Jackson annotations work the same
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Folder {

    private Integer id;

    private String uid;

    private String title;

}
