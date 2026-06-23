package com.chatbot.organizationservice.dto;

import com.chatbot.organizationservice.entity.Organization;
import com.chatbot.organizationservice.entity.Project;
import com.chatbot.organizationservice.entity.User;

import java.util.List;

public class ContextResponse {

    private Organization organization;
    private List<Project> projects;
    private List<User> users;

    public ContextResponse() {
    }

    public ContextResponse(
            Organization organization,
            List<Project> projects,
            List<User> users) {

        this.organization = organization;
        this.projects = projects;
        this.users = users;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}