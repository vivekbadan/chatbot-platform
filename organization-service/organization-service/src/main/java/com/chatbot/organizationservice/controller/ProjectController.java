package com.chatbot.organizationservice.controller;

import com.chatbot.organizationservice.entity.Project;
import com.chatbot.organizationservice.repository.OrganizationRepository;
import com.chatbot.organizationservice.repository.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectRepository repository;
    private final OrganizationRepository organizationRepository;

    public ProjectController(
            ProjectRepository repository,
            OrganizationRepository organizationRepository) {

        this.repository = repository;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping
    public Project createProject(@RequestBody Project project) {

        if (!organizationRepository.existsById(project.getOrganizationId())) {
            throw new RuntimeException("Organization not found");
        }

        return repository.save(project);
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Project updateProject(
            @PathVariable Long id,
            @RequestBody Project project) {

        if (!organizationRepository.existsById(project.getOrganizationId())) {
            throw new RuntimeException("Organization not found");
        }

        project.setId(id);
        return repository.save(project);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        repository.deleteById(id);
    }
}