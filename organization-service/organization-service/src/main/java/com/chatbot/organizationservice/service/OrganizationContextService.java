package com.chatbot.organizationservice.service;

import com.chatbot.organizationservice.dto.ContextResponse;
import com.chatbot.organizationservice.entity.Organization;
import com.chatbot.organizationservice.entity.Project;
import com.chatbot.organizationservice.entity.User;
import com.chatbot.organizationservice.repository.OrganizationRepository;
import com.chatbot.organizationservice.repository.ProjectRepository;
import com.chatbot.organizationservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationContextService {

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public OrganizationContextService(
            OrganizationRepository organizationRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ContextResponse getOrganizationContext(Long organizationId) {

        Organization organization =
                organizationRepository.findById(organizationId).orElse(null);

        if (organization == null) {
            throw new RuntimeException("Organization not found");
        }

        List<Project> projects =
                projectRepository.findByOrganizationId(organizationId);

        List<User> users =
                userRepository.findByOrganizationId(organizationId);

        return new ContextResponse(
                organization,
                projects,
                users
        );
    }
}