package com.vegvitae.vegvitae.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.Recipe;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.ProductRepository;
import com.vegvitae.vegvitae.repository.RecipeRepository;
import com.vegvitae.vegvitae.repository.UploadedFileRepository;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api" + RecipeController.PATH)
public class RecipeController {

  static final String PATH = "/recipes";

  @Autowired
  private RecipeRepository recipeRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private UploadedFileRepository uploadedFileRepository;

  @Transactional
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Resource<Recipe> newRecipe(@Valid @RequestBody Recipe newRecipe,
      @RequestHeader("token") String token) {
    User creator = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
    newRecipe.setCreator(creator);
    if (!userRepository.findById(newRecipe.getCreator().getId()).isPresent()) {
      throw new GenericException(HttpStatus.NOT_FOUND,
          ExceptionMessages.USER_NOT_FOUND.getErrorMessage() + newRecipe.getId());
    }

    recipeRepository.save(newRecipe);

    // Uploading a recipe earns the user a certain number of points
    creator.addPoints(25);
    userRepository.save(creator);

    return new Resource<>(newRecipe,
        linkTo(methodOn(RecipeController.class).getRecipeById(newRecipe.getId())).withSelfRel());

  }

  @Transactional
  @GetMapping("{id}")
  public Resource<Recipe> getRecipeById(@PathVariable Long id) {
    Recipe recipe = recipeRepository.findById(id).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage() + id));
    return new Resource<>(recipe,
        linkTo(methodOn(RecipeController.class).getRecipeImages(id)).withSelfRel());
  }

  @Transactional
  @GetMapping("{id}/pictures")
  public ResponseEntity<byte[]> getRecipeImages(@PathVariable Long id) {
    Optional<Recipe> recipeOpt = recipeRepository.findById(id);
    if (recipeOpt.isPresent()) {
      Recipe actualRecipe = recipeOpt.get();
      if (actualRecipe.getRecipeImage() != null) {
        byte[] imageBytes = actualRecipe.getRecipeImage();
        return new ResponseEntity<>(Base64.getEncoder().encode(imageBytes), HttpStatus.OK);
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST, "Recipe has no image");
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage() + id);
    }
  }

  @Transactional
  @PutMapping("{id}/pictures")
  public void addRecipeImages(@PathVariable Long id, @RequestParam("files") MultipartFile file)
      throws IOException {

    Recipe actualRecipe = recipeRepository.findById(id).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage() + id));

    if (file != null) {
      actualRecipe.setRecipeImage(file.getBytes());
    } else {
      throw new GenericException(HttpStatus.NO_CONTENT,
          "You cannot upload an empty file");
    }
    recipeRepository.save(actualRecipe);
  }

  @Transactional
  @PutMapping("{id_recipe}/addProduct/{id_product}")
  public void addProductToARecipe(@PathVariable Long id_recipe, @PathVariable Long id_product) {
    Product productToAdd = productRepository.findById(id_product).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.PRODUCT_NOT_FOUND.getErrorMessage() + id_product));

    Recipe actualRecipe = recipeRepository.findById(id_recipe).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage() + id_recipe));

    if (actualRecipe.getUsedProducts().contains(productToAdd)) {
      throw new GenericException(HttpStatus.IM_USED,
          ExceptionMessages.PRODUCT_EXISTS.getErrorMessage() + id_product);
    }

    actualRecipe.addUsedProduct(productToAdd);
    recipeRepository.save(actualRecipe);
  }

  @Transactional
  @GetMapping("{id_recipe}/products")
  public Resources<Resource<Product>> getRecipeProducts(@PathVariable Long id_recipe) {
    Recipe recipe = recipeRepository.findById(id_recipe).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.PRODUCT_NOT_FOUND.getErrorMessage() + id_recipe));

    Set<Product> productsUsed = recipe.getUsedProducts();
    if (productsUsed.isEmpty()) {
      throw new GenericException(HttpStatus.NOT_FOUND, "This Recipe has no products associated");
    }
    List<Resource<Product>> products = new ArrayList<>();
    for (Product next : productsUsed) {
      products.add(new Resource<>(next,
          linkTo(methodOn(ProductController.class).getProductByBarcode(next.getBarcode()))
              .withSelfRel()));
    }
    return new Resources<>(products,
        linkTo(methodOn(RecipeController.class).getRecipeImages(id_recipe)).withSelfRel());
  }

  @Transactional
  @PostMapping("{recipeId}/report")
  void reportProduct(@RequestHeader("token") String token, @PathVariable Long recipeId) {
    Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
    if (recipeOpt.isPresent()) {
      Recipe recipe = recipeOpt.get();
      User user = userRepository.findByToken(token).orElseThrow(
          () -> new GenericException(HttpStatus.BAD_REQUEST,
              ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
      Set<User> usersReports = recipe.getUserReports();
      if (usersReports.contains(user)) {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            "This user already reported this product");
      }
      usersReports.add(user);
      recipe.setUserReports(usersReports);
      if (usersReports.size() >= 2) {
        recipeRepository.deleteById(recipeId);
        return;
      }
      recipeRepository.save(recipe);
      return;
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST, "The specified product doesn't exist");
    }
  }

  @Transactional
  @PutMapping("{id}/favourites")
  @ResponseStatus(HttpStatus.OK)
  void addRecipeToFavourite(@RequestHeader("token") String token, @PathVariable Long id) {
    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));

    Recipe recipe = recipeRepository.findById(id).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage()));

    if (!user.getFavouriteRecipes().contains(recipe)) {
      user.setFavouriteRecipe(recipe);
      userRepository.save(user);
    }
    else throw new GenericException(HttpStatus.IM_USED,
        "You have already added this recipe to favourites.");
  }

  @Transactional
  @DeleteMapping("{id}/favourites")
  @ResponseStatus(HttpStatus.OK)
  void removeRecipeFromFavourite(@RequestHeader("token") String token, @PathVariable Long id) {
    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));

    Recipe recipe = recipeRepository.findById(id).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage()));

    if (user.getFavouriteRecipes().contains(recipe)) {
      user.deleteFavouriteRecipe(recipe);
      userRepository.save(user);
    } else {
      throw new GenericException(HttpStatus.NOT_FOUND,
          "You can not remove a recipe from favourites if its not yet in favourites.");
    }

  }

  @Transactional
  @GetMapping()
  public Resources<Resource<Recipe>> getAllRecipes() {
    List<Recipe> allRecipes = recipeRepository.findAll();
    List<Resource<Recipe>> recipesResources = new ArrayList<>();

    for (Recipe next : allRecipes) {
      recipesResources.add(new Resource<>(next,
          linkTo(methodOn(RecipeController.class).getRecipeById(next.getId())).withSelfRel()));
    }

    return new Resources<>(recipesResources);
  }

  @Transactional
  @GetMapping("/search")
  public Resources<Resource<Recipe>> searchRecipes(
      @RequestParam(name = "title", required = false) String title,
      @RequestParam(name = "idProduct", required = false) Long idProduct) {

    List<Resource<Recipe>> recipeResources = new ArrayList<>();
    Set<Recipe> recipes = new HashSet<>();
    if (title != null && idProduct != null){
      recipes = recipeRepository.findRecipeByTitleIgnoreCaseContaining(title);
      Product actualProduct = productRepository.findById(idProduct).orElseThrow(
          () -> new GenericException(HttpStatus.NOT_FOUND,
              ExceptionMessages.PRODUCT_NOT_FOUND.getErrorMessage()));

      Set<Recipe> aux = recipeRepository.findRecipeByUsedProducts(actualProduct);
      recipes.retainAll(aux);
    }
    if (idProduct != null){
      Product actualProduct = productRepository.findById(idProduct).orElseThrow(
          () -> new GenericException(HttpStatus.NOT_FOUND,
              ExceptionMessages.PRODUCT_NOT_FOUND.getErrorMessage()));

      recipes = recipeRepository.findRecipeByUsedProducts(actualProduct);
    }
    if (title != null){
      recipes = recipeRepository.findRecipeByTitleIgnoreCaseContaining(title);
    }

    for (Recipe next : recipes){
      recipeResources.add(new Resource<>(next,
          linkTo(methodOn(RecipeController.class).getRecipeById(next.getId())).withSelfRel()));
    }

    return new Resources<>(recipeResources);
  }
}

