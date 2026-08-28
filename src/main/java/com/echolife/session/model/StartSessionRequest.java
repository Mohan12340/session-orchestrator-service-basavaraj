package com.echolife.session.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartSessionRequest(
    @NotBlank @Size(max=120) String personaId,
    @NotBlank @Size(max=40) String mode,
    @NotBlank @Size(max=40) String inputChannel,
    @NotBlank @Size(max=40) String outputChannel,
    @NotBlank @Size(max=40) String clientType) {}
