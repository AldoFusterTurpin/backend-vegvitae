package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.exceptions.UserNotFoundException;
import com.vegvitae.vegvitae.repository.UserRepository;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@RestController
class UserController {

  private final UserRepository repository;

  UserController(UserRepository repository) {
    this.repository = repository;
  }


  @GetMapping("/users")
  Resources<Resource<User>> getAllUsers() {

    List<Resource<User>> users = repository.findAll().stream()
        .map(user -> new Resource<>(user,
            linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),

            linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")))
        .collect(Collectors.toList());

    return new Resources<>(users,
        linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
  }

  @PostMapping("/users")
  User newUser(@RequestBody User createNewUser) {
    return repository.save(createNewUser);
  }


  @GetMapping("/users/{id}")
  Resource<User> getUserById(@PathVariable Long id) {

    User user = repository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    return new Resource<>(user,
        linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
        linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
  }

  @PutMapping("/users/{id}")
  User replaceUserById(@RequestBody User newUser, @PathVariable Long id) {

    return repository.findById(id)
        .map(user -> {
          user.setName(newUser.getName());
          user.setPassword(newUser.getPassword());
          return repository.save(user);
        })
        .orElseGet(() -> {
          newUser.setId(id);
          return repository.save(newUser);
        });
  }

  @DeleteMapping("/users/{id}")
  void deleteUserById(@PathVariable Long id) {
    repository.deleteById(id);
  }
}
