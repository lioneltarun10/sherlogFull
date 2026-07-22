package com.xorcists.demo.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Producer for HTTP client used for Grafana API calls.
 * 
 * MIGRATION NOTE:
 * - Spring Boot: @Configuration class with @Bean methods returning WebClient
 * - Quarkus: @ApplicationScoped class with @Produces methods
 * 
 * Key differences:
 * - Spring used reactive WebClient from WebFlux
 * - Quarkus uses java.net.http.HttpClient (simpler, blocking calls)
 * - For reactive in Quarkus, use Mutiny or Vert.x Web Client
 */
@ApplicationScoped
public class GrafanaClientProducer {

    private static final Logger LOG = Logger.getLogger(GrafanaClientProducer.class);

    @Produces
    @Singleton
    public HttpClient grafanaHttpClient(GrafanaConfig config) {
        LOG.infof("Creating HTTP client with connect timeout: %dms, read timeout: %dms",
                config.connectTimeout(), config.readTimeout());

        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.connectTimeout()))
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Builds the cookie header for Grafana authentication.
     */
    public static String buildCookieHeader(GrafanaConfig config) {
        return String.format(
                "ajs_user_id=%s; ajs_anonymous_id=%s; grafana_session=%s; grafana_session_expiry=%s",
                config.ajsUserId(),
                config.ajsAnonymousId(),
                config.session(),
                config.sessionExpiry()
        );
    }
}
