package com.vegvitae.vegvitae.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.google.common.hash.Hashing;
import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.RatingProduct;
import com.vegvitae.vegvitae.model.Recipe;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.ProductRepository;
import com.vegvitae.vegvitae.repository.RatingProductRepository;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api" + UserController.PATH)
class UserController {

  public final static String PATH = "/users";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RatingProductRepository ratingProductRepository;

  @Autowired
  private ProductRepository productRepository;

  @GetMapping()
  Resources<Resource<User>> getAllUsers() {

    List<Resource<User>> users = userRepository.findAll().stream()
        .map(user -> {
          Resource<User> resource = new Resource<>(user,
              linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),
              linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
          if (user.getImage() != null) {
            resource.add(
                linkTo(methodOn(UserController.class).getUserImage(user.getId())).withRel("image"));
          }
          return resource;
        })
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
    createNewUser.setToken(Hashing.sha256().hashString(
        createNewUser.getPassword() + createNewUser.getEmail() + createNewUser.getUsername(),
        StandardCharsets.UTF_8)
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
    Resource<User> resource = new Resource<>(loginUser,
        linkTo(methodOn(UserController.class).getUserById(loginUser.getId())).withSelfRel());
    if (loginUser.getImage() != null) {
      resource.add(
          linkTo(methodOn(UserController.class).getUserImage(loginUser.getId())).withRel("image"));
    }
    return resource;
  }


  @GetMapping("/{id}")
  Resource<User> getUserById(@PathVariable Long id) {

    User user = userRepository.findById(id)
        .orElseThrow(
            () -> new GenericException(HttpStatus.NOT_FOUND, "Cannot find user with id " + id));
    Resource<User> resource = new Resource<>(user,
        linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
        linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
    if (user.getImage() != null) {
      resource.add(
          linkTo(methodOn(UserController.class).getUserImage(user.getId())).withRel("image"));
    }
    return resource;
  }

  @PutMapping("{id}/image")
  void uploadUserImage(@PathVariable Long id,
      @RequestParam("image") MultipartFile image) throws IOException {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new GenericException(HttpStatus.BAD_REQUEST, "Couldn't find the user"));
    user.setImage(image.getBytes());
    userRepository.save(user);
  }

  @GetMapping("{id}/image")
  ResponseEntity<byte[]> getUserImage(@PathVariable Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new GenericException(HttpStatus.BAD_REQUEST, "Couldn't find the user"));
    if (user.getImage() != null) {
      byte[] imageBytes = user.getImage();
      return new ResponseEntity<>(Base64.getEncoder().encode(imageBytes), HttpStatus.OK);
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST, "User has no image");
    }
  }

  @PutMapping("{id}")
  Resource<User> replaceUserById(@Valid @RequestBody User newUser, @PathVariable Long id,
      @RequestHeader("token") String token) {
    User replacedUser = userRepository.findById(id)
        .map(user -> {
          if (!user.getToken().equals(token)) {
            throw new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_TOKEN.getErrorMessage());
          }
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
          user.setToken(Hashing.sha256().hashString(
              user.getPassword() + user.getEmail() + user.getUsername(),
              StandardCharsets.UTF_8)
              .toString());
          return userRepository.save(user);
        }).get();
    Resource resource = new Resource<>(replacedUser,
        linkTo(methodOn(UserController.class).getUserById(replacedUser.getId())).withSelfRel());
    if (replacedUser.getImage() != null) {
      resource.add(linkTo(methodOn(UserController.class).getUserImage(replacedUser.getId()))
          .withRel("image"));
    }
    return resource;
  }

  @DeleteMapping("{id}")
  void deleteUserById(@PathVariable Long id) {
    userRepository.deleteById(id);
  }

  @GetMapping("/{id}/favourites")
  Resources<Set<Product>> getUserFavouriteProducts(@PathVariable Long id) {
    User actual_user = userRepository.findById(id).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST, "Cannot find user with id " + id));

    Resources resource = new Resources<>(actual_user.getFavouriteProducts());

    for (Product p : actual_user.getFavouriteProducts()) {
      resource.add(linkTo(methodOn(ProductController.class).getProductByBarcode(p.getBarcode()))
          .withSelfRel());
    }
    return resource;
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

  @GetMapping("/ratedProducts")
  public Resources<Resource<Product>> getRatedProducts(@RequestHeader("token") String token){
    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));

    List<RatingProduct> productsRated = ratingProductRepository.findByUser(user);
    List<Resource<Product>> productsRatedResource = new ArrayList<>();

    for (RatingProduct next : productsRated){
      productsRatedResource.add(new Resource<>(next.getProduct(),
          linkTo(methodOn(ProductController.class).getProductByBarcode(next.getProduct().getBarcode())).withSelfRel()));
    }
    return new Resources<>(productsRatedResource);
  }

  @GetMapping("/favouriteRecipes")
  public Resources<Resource<Recipe>> getFavouriteRecipes(@RequestHeader("token") String token) {
    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));

    List<Resource<Recipe>> favRecipesResource = new ArrayList<>();
    Set<Recipe> favRecipes = user.getFavouriteRecipes();

    for (Recipe next : favRecipes){
      favRecipesResource.add(new Resource<>(next,
          linkTo(methodOn(RecipeController.class).getRecipeById(next.getId())).withSelfRel()));
    }

    return new Resources<>(favRecipesResource);
  }

}
