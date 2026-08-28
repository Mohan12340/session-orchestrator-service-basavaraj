package com.echolife.session.service;

import com.echolife.session.client.IdentityConsentClient;
import com.echolife.session.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class SessionService {
    private final IdentityConsentClient identity;
    private final DynamoDbTable<SessionRecord> table;
    private final long ttlSeconds;

    public SessionService(IdentityConsentClient identity, DynamoDbTable<SessionRecord> table,
                          @Value("${echolife.session.ttl-seconds:1800}") long ttlSeconds) {
        if (ttlSeconds <= 0) throw new IllegalArgumentException("SESSION_TTL_SECONDS must be positive");
        this.identity = identity;
        this.table = table;
        this.ttlSeconds = ttlSeconds;
    }

    public SessionResponse start(StartSessionRequest request, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        String personaId = request.personaId().trim();
        String mode = request.mode().trim().toUpperCase(Locale.ROOT);
        String input = request.inputChannel().trim().toUpperCase(Locale.ROOT);
        String output = request.outputChannel().trim().toUpperCase(Locale.ROOT);
        String clientType = request.clientType().trim().toUpperCase(Locale.ROOT);

        AccessResponse access;
        try {
            access = identity.check(new IdentityConsentClient.SessionAccessRequest(userId, personaId, mode, input, output));
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            throw new IllegalStateException("IDENTITY_SERVICE_ERROR", ex);
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new IllegalStateException("IDENTITY_SERVICE_UNAVAILABLE", ex);
        }

        if (access == null) throw new IllegalStateException("IDENTITY_SERVICE_INVALID_RESPONSE");
        if (!access.allowed()) throw new AccessDeniedException(access.reason());

        String id = "sess_" + UUID.randomUUID();
        Instant now = Instant.now();
        SessionRecord session = new SessionRecord();
        session.setSessionId(id);
        session.setUserId(userId);
        session.setPersonaId(personaId);
        session.setMode(mode);
        session.setInputChannel(input);
        session.setOutputChannel(output);
        session.setClientType(clientType);
        session.setStatus("ACTIVE");
        session.setCreatedAt(now);
        session.setExpiresAtEpoch(now.plusSeconds(ttlSeconds).getEpochSecond());
        session.setPolicyVersion(access.policyVersion());
        table.putItem(PutItemEnhancedRequest.builder(SessionRecord.class).item(session).build());

        return new SessionResponse(id, userId, personaId, mode, "ACTIVE", output, false, access.policyVersion());
    }

    public SessionRecord get(String sessionId, Authentication authentication) {
        String userId = ((Jwt) authentication.getPrincipal()).getSubject();
        SessionRecord record = table.getItem(r -> r.key(k -> k.partitionValue(sessionId)));
        if (record == null) throw new IllegalArgumentException("SESSION_NOT_FOUND");
        if (!userId.equals(record.getUserId())) throw new AccessDeniedException("SESSION_ACCESS_DENIED");
        if (record.getExpiresAtEpoch() != null && record.getExpiresAtEpoch() <= Instant.now().getEpochSecond() && "ACTIVE".equals(record.getStatus())) {
            record.setStatus("EXPIRED");
            table.updateItem(record);
        }
        return record;
    }

    public SessionResponse end(String sessionId, Authentication authentication) {
        SessionRecord record = get(sessionId, authentication);
        if ("ENDED".equals(record.getStatus())) return toResponse(record);
        if ("EXPIRED".equals(record.getStatus())) return toResponse(record);
        record.setStatus("ENDED");
        table.updateItem(record);
        return toResponse(record);
    }

    private SessionResponse toResponse(SessionRecord r) {
        return new SessionResponse(r.getSessionId(), r.getUserId(), r.getPersonaId(), r.getMode(), r.getStatus(), r.getOutputChannel(), false, r.getPolicyVersion());
    }
}
