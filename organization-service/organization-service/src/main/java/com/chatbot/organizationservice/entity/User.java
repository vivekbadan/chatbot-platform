package com.chatbot.organizationservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @Column(name = "organization_id")
    private Long organizationId;

    public User() {
    }

    public User(String name, String email, Long organizationId) {
        this.name = name;
        this.email = email;
        this.organizationId = organizationId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}