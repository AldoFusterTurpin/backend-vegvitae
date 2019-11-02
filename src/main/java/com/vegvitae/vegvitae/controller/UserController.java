package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.hash.Hashing;

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
  @ResponseStatus(HttpStatus.CREATED)
  Resource<User> newUser(@Valid @RequestBody User createNewUser) {
    if (userRepository.findByUsername(createNewUser.getUsername()) != null) {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.USERNAME_IN_USE.getErrorMessage());
    }
    if (userRepository.findByEmail(createNewUser.getEmail()) != null) {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.EMAIL_IN_USE.getErrorMessage());
    }
    if (!isValidPassword(createNewUser.getPassword())) {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.INVALID_PASSWORD.getErrorMessage());
    }
    if (createNewUser.getSocialMediaLinks() != null
        && createNewUser.getSocialMediaLinks().size() > 4) {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.INVALID_SOCIAL_MEDIA_LINKS_LENGTH.getErrorMessage());
    }
    if (createNewUser.getPersonalDescription() != null
        && createNewUser.getPersonalDescription().length() > 160) {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.INVALID_DESCRIPTION.getErrorMessage());
    }

    createNewUser.setPassword(
        Hashing.sha256().hashString(createNewUser.getPassword(), StandardCharsets.UTF_8)
            .toString());
    userRepository.save(createNewUser);
    return new Resource<>(createNewUser,
        linkTo(methodOn(UserController.class).getUserById(createNewUser.getId())).withSelfRel());
  }

  @PostMapping("/login")
  Resource<User> login(@RequestBody Map<String, String> userData) {
    if (!userData.containsKey("username") || userData.get("username").length() <= 0) {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.NULL_USERNAME.getErrorMessage());
    }
    if (!userData.containsKey("password") || userData.get("password").length() <= 0) {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.NULL_PASSWORD.getErrorMessage());
    }
    User loginUser = userRepository.findByUsername(userData.get("username"));
    if (loginUser == null) {
      throw new GenericException(HttpStatus.FORBIDDEN,
          ExceptionMessages.INVALID_CREDENTIALS.getErrorMessage());
    }

    if (!loginUser.getPassword().equals(
        Hashing.sha256().hashString(userData.get("password"), StandardCharsets.UTF_8).toString())) {
      throw new GenericException(HttpStatus.FORBIDDEN,
          ExceptionMessages.INVALID_CREDENTIALS.getErrorMessage());
    }
    return new Resource<>(loginUser,
        linkTo(methodOn(UserController.class).getUserById(loginUser.getId())).withSelfRel());
  }


  @GetMapping("/{id}")
  Resource<User> getUserById(@PathVariable Long id) {

    User user = userRepository.findById(id)
        .orElseThrow(
            () -> new GenericException(HttpStatus.NOT_FOUND, "Cannot find user with id" + id));
    return new Resource<>(user,
        linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
        linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
  }

  @PutMapping("{id}")
  Resource<User> replaceUserById(@Valid @RequestBody User newUser, @PathVariable Long id) {
    User replacedUser = userRepository.findById(id)
        .map(user -> {
          if (!newUser.getUsername().equals(user.getUsername())
              && userRepository.findByUsername(newUser.getUsername()) != null) {
            throw new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.USERNAME_IN_USE.getErrorMessage());
          }
          if (!isValidPassword(newUser.getPassword())) {
            throw new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_PASSWORD.getErrorMessage());
          }
          if (!newUser.getEmail().equals(user.getEmail())
              && userRepository.findByEmail(newUser.getEmail()) != null) {
            throw new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.EMAIL_IN_USE.getErrorMessage());
          }
          if (newUser.getSocialMediaLinks().size() > 4) {
            throw new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_SOCIAL_MEDIA_LINKS_LENGTH.getErrorMessage());
          }
          if (newUser.getPersonalDescription().length() > 160) {
            throw new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_DESCRIPTION.getErrorMessage());
          }
          user.setEmail(newUser.getEmail());
          user.setUsername(newUser.getUsername());
          user.setPassword(
              Hashing.sha256().hashString(newUser.getPassword(), StandardCharsets.UTF_8)
                  .toString());
          user.setPersonalDescription(newUser.getPersonalDescription());
          user.setSocialMediaLinks(newUser.getSocialMediaLinks());
          return userRepository.save(user);
        }).get();
    return new Resource<>(replacedUser,
        linkTo(methodOn(UserController.class).getUserById(replacedUser.getId())).withSelfRel());
  }

  @DeleteMapping("{id}")
  void deleteUserById(@PathVariable Long id) {
    userRepository.deleteById(id);
  }


  private boolean isValidPassword(String password) {
    if (password.length() < 7) {
      return false;
    }
    int[] criteria = {0, 0, 0};
    for (int i = 0; i < password.length(); ++i) {
      if (password.charAt(i) >= 'a' && password.charAt(i) <= 'w') {
        criteria[0]++;
      }
      if (password.charAt(i) >= 'A' && password.charAt(i) <= 'W') {
        criteria[1]++;
      }
      if (password.charAt(i) >= '0' && password.charAt(i) <= '9') {
        criteria[2]++;
      }
    }
    return (criteria[0] > 0 && criteria[1] > 0 && criteria[2] > 0);
  }
}
