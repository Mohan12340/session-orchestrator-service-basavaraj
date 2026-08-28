package com.echolife.session.model;

public record AccessResponse(
    boolean allowed,
    String reason,
    String userId,
    String role,
    boolean ageEligible,
    boolean consented,
    boolean personaAllowed,
    String[] effectiveModes,
    String[] effectiveChannels,
    Integer policyVersion
) {}
