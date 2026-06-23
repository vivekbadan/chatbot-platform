package com.chatbot.organizationservice.controller;

import com.chatbot.organizationservice.dto.ContextResponse;
import com.chatbot.organizationservice.service.OrganizationContextService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/context")
public class ContextController {

    private final OrganizationContextService organizationContextService;

    public ContextController(
            OrganizationContextService organizationContextService) {

        this.organizationContextService = organizationContextService;
    }

    @GetMapping("/{organizationId}")
    public ContextResponse getContext(
            @PathVariable Long organizationId) {

        return organizationContextService
                .getOrganizationContext(organizationId);
    }
}