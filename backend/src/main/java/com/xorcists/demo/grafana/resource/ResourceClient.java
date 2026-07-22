package com.xorcists.demo.grafana.resource;

import com.xorcists.demo.grafana.model.Dashboard;
import com.xorcists.demo.grafana.model.Datasource;
import com.xorcists.demo.grafana.model.Folder;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Grafana resource operations.
 * 
 * MIGRATION NOTE: NO CHANGES NEEDED
 * - Interface remains the same
 */
public interface ResourceClient {

    List<Folder> getFolders();

    List<Datasource> getDatasources();

    List<Dashboard> getDashboards();

    Optional<Datasource> getDatasourceByType(String type);

}
