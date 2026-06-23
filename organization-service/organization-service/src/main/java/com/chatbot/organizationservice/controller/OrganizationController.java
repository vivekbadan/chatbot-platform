package com.chatbot.organizationservice.controller;

import com.chatbot.organizationservice.entity.Organization;
import com.chatbot.organizationservice.repository.OrganizationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationRepository repository;

    public OrganizationController(OrganizationRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Organization createOrganization(@RequestBody Organization organization) {
        return repository.save(organization);
    }

    @GetMapping
    public List<Organization> getAllOrganizations() {
        return repository.findAll();
    }
    //GET METHOD
    @GetMapping("/{id}")
    public Organization getOrganizationById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }
    //UPDATE METHOD
    @PutMapping("/{id}")
    public Organization updateOrganization(
            @PathVariable Long id,
            @RequestBody Organization organization) {

        organization.setId(id);

        return repository.save(organization);
    }
    //DELETE METHOD
    @DeleteMapping("/{id}")
    public void deleteOrganization(@PathVariable Long id) {
        repository.deleteById(id);
    }

}