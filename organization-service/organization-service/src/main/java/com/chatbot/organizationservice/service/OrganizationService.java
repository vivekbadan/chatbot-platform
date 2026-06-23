package com.chatbot.organizationservice.service;

import com.chatbot.organizationservice.entity.Organization;
import com.chatbot.organizationservice.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository repository;

    public OrganizationService(OrganizationRepository repository) {
        this.repository = repository;
    }

    public List<Organization> getAllOrganizations() {
        return repository.findAll();
    }

    public Organization getOrganizationById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Organization createOrganization(Organization organization) {
        return repository.save(organization);
    }

    public Organization updateOrganization(Long id, Organization organization) {
        organization.setId(id);
        return repository.save(organization);
    }

    public void deleteOrganization(Long id) {
        repository.deleteById(id);
    }
}