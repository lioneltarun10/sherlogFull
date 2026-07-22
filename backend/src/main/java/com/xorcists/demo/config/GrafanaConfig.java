package com.xorcists.demo.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;

/**
 * Grafana configuration properties.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @ConfigurationProperties(prefix = "grafana")
 * - Quarkus: @ConfigMapping(prefix = "grafana")
 * 
 * Key differences:
 * - Quarkus uses interface-based config mapping (immutable)
 * - Property names use kebab-case in properties file
 * - No setters needed (interface methods are getters)
 */
@ConfigMapping(prefix = "grafana")
public interface GrafanaConfig {

    @WithName("base-url")
    String baseUrl();

    @WithName("org-id")
    @WithDefault("1")
    Integer orgId();

    @WithName("ajs-user-id")
    String ajsUserId();

    @WithName("ajs-anonymous-id")
    String ajsAnonymousId();

    @WithName("session")
    String session();

    @WithName("session-expiry")
    String sessionExpiry();

    @WithName("connect-timeout")
    @WithDefault("5000")
    Integer connectTimeout();

    @WithName("read-timeout")
    @WithDefault("30000")
    Integer readTimeout();

}
