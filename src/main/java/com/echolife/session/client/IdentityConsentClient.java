package com.echolife.session.client;

import com.echolife.session.model.AccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityConsentClient {

    private final RestClient rest;

    // Hard-coded internal service key
    private static final String SERVICE_KEY =
            "wG1AxRxM9wZ0peREH89kzcR5y8EEHoe9Tugs5o+E9XQNTzOEBGGQFElW++h9HT69";

    public IdentityConsentClient(
            RestClient.Builder builder,
            @Value("${echolife.identity.base-url}") String baseUrl) {

        this.rest = builder
                .baseUrl(baseUrl)
                .build();
    }

    public AccessResponse check(SessionAccessRequest request) {

        return rest.post()
                .uri("/api/v1/internal/session-access-check")
                .header("X-Internal-Service-Key", SERVICE_KEY)
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(request)
                .retrieve()
                .body(AccessResponse.class);
    }

    public record SessionAccessRequest(
            String userId,
            String personaId,
            String mode,
            String inputChannel,
            String outputChannel
    ) {
    }
}