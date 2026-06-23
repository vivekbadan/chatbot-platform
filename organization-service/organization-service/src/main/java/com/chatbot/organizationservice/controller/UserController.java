package com.chatbot.organizationservice.controller;

import com.chatbot.organizationservice.entity.User;
import com.chatbot.organizationservice.repository.OrganizationRepository;
import com.chatbot.organizationservice.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;
    private final OrganizationRepository organizationRepository;

    public UserController(
            UserRepository repository,
            OrganizationRepository organizationRepository) {

        this.repository = repository;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {

        if (!organizationRepository.existsById(user.getOrganizationId())) {
            throw new RuntimeException("Organization not found");
        }

        return repository.save(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        if (!organizationRepository.existsById(user.getOrganizationId())) {
            throw new RuntimeException("Organization not found");
        }

        user.setId(id);
        return repository.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
    repository.deleteById(id);}
}