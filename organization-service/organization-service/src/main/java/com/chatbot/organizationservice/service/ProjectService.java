package com.chatbot.organizationservice.service;

import com.chatbot.organizationservice.entity.Project;
import com.chatbot.organizationservice.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<Project> getAllProjects() {
        return repository.findAll();
    }

    public Project getProjectById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Project createProject(Project project) {
        return repository.save(project);
    }

    public Project updateProject(Long id, Project project) {
        project.setId(id);
        return repository.save(project);
    }

    public void deleteProject(Long id) {
        repository.deleteById(id);
    }
}