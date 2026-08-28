package com.echolife.session.model;

public record SessionResponse(
    String sessionId,
    String userId,
    String personaId,
    String mode,
    String status,
    String outputChannel,
    boolean degraded,
    Integer policyVersion
) {}
