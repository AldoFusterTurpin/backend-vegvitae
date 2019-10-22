package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.exceptions.UserNotFoundException;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.hash.Hashing;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@RestController
@RequestMapping("/api" + UserController.PATH)
class UserController {

  public final static String PATH = "/users";

  @Autowired
  private UserRepository userRepository;

  @GetMapping
  Resources<Resource<User>> getAllUsers() {

    List<Resource<User>> users = userRepository.findAll().stream()
        .map(user -> new Resource<>(user,
            linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),
            linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")))
        .collect(Collectors.toList());

    return new Resources<>(users,
        linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
  }

  @PostMapping
  User newUser(@Valid @RequestBody User createNewUser) {
    createNewUser.setPassword(
        Hashing.sha256().hashString(createNewUser.getPassword(), StandardCharsets.UTF_8)
            .toString());
    return userRepository.save(createNewUser);
  }

  @PostMapping("/login")
  User login(@RequestBody Map<String, String> userData) {
    if (!userData.containsKey("username") || userData.get("username").length() <= 0) {
      throw new GenericException(HttpStatus.BAD_REQUEST, "Username cannot be null nor empty");
    }
    if (!userData.containsKey("password") || userData.get("password").length() <= 0) {
      throw new GenericException(HttpStatus.BAD_REQUEST, "Password cannot be null nor empty");
    }
    List<User> allUsers = userRepository.findAll();
    String password = userData.get("password");
    User loginUser = StreamSupport
        .stream(allUsers.spliterator(), false).filter(u -> u.getUsername()
            .equals(userData.get("username"))).findFirst()
        .orElseThrow(() -> new GenericException(HttpStatus.FORBIDDEN, "User does not exist"));
    if (!loginUser.getPassword()
        .equals(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString())) {
      throw new GenericException(HttpStatus.FORBIDDEN, "Credentials are not correct. Try again");
    }
    return loginUser;
  }

  @GetMapping("/{id}")
  Resource<User> getUserById(@PathVariable Long id) {

    User user = userRepository.findById(id)
        .orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "Cannot find user with id" + id));
    return new Resource<>(user,
        linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
        linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
  }

  @PutMapping("{id}")
  User replaceUserById(@Valid @RequestBody User newUser, @PathVariable Long id) {

    return userRepository.findById(id)
        .map(user -> {
          user.setUsername(newUser.getUsername());
          user.setPassword(
              Hashing.sha256().hashString(newUser.getPassword(), StandardCharsets.UTF_8)
                  .toString());
          user.setEmail(newUser.getEmail());
          user.setPersonalDescription(newUser.getPersonalDescription());
          user.setSocialMediaLinks(newUser.getSocialMediaLinks());
          return userRepository.save(user);
        }).get();
  }

  @DeleteMapping("{id}")
  void deleteUserById(@PathVariable Long id) {
    userRepository.deleteById(id);
  }
}
