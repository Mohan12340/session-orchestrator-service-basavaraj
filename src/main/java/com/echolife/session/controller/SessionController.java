package com.echolife.session.controller;

import com.echolife.session.model.*;
import com.echolife.session.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {
    private final SessionService service;
    public SessionController(SessionService service) { this.service = service; }

    @PostMapping
    public SessionResponse start(@Valid @RequestBody StartSessionRequest request, Authentication authentication) {
        return service.start(request, authentication);
    }

    @GetMapping("/{sessionId}")
    public SessionRecord get(@PathVariable String sessionId, Authentication authentication) {
        return service.get(sessionId, authentication);
    }

    @PostMapping("/{sessionId}/end")
    public SessionResponse end(@PathVariable String sessionId, Authentication authentication) {
        return service.end(sessionId, authentication);
    }
}
