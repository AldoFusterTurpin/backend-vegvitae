package com.vegvitae.vegvitae.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.Recipe;
import com.vegvitae.vegvitae.repository.ProductRepository;
import com.vegvitae.vegvitae.model.RatingRecipe;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.RatingRecipeRepository;
import com.vegvitae.vegvitae.repository.RecipeRepository;
import com.vegvitae.vegvitae.repository.UploadedFileRepository;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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

  @Autowired
  private RatingRecipeRepository ratingRecipeRepository;

  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public Resource<Recipe> newRecipe(@Valid @RequestBody Recipe newRecipe,
      @RequestHeader("token") String token) {
    newRecipe.setCreator(userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage())));

    if (!userRepository.findById(newRecipe.getCreator().getId()).isPresent()) {
      throw new GenericException(HttpStatus.NOT_FOUND,
          ExceptionMessages.USER_NOT_FOUND.getErrorMessage() + newRecipe.getId());
    }

    recipeRepository.save(newRecipe);
    return new Resource<>(newRecipe,
        linkTo(methodOn(RecipeController.class).getRecipeById(newRecipe.getId())).withSelfRel());

  }

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

  @DeleteMapping("{id_recipe}/deleteProduct/{id_product}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteProductToARecipe(@PathVariable Long id_recipe, @PathVariable Long id_product) {
    Recipe actual_recipe = recipeRepository.findById(id_recipe).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage() + id_recipe));

    Product productToRemove = productRepository.findById(id_product).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.PRODUCT_NOT_FOUND.getErrorMessage() + id_product));

    if (actual_recipe.getUsedProducts().contains(productToRemove)) {
      actual_recipe.removeProduct(productToRemove);
      recipeRepository.save(actual_recipe);
    }
  }

  @PutMapping("{id_recipe}/rate")
  public Resource<Recipe> rateRecipe(@PathVariable Long id_recipe, @RequestHeader("token") String token,
      @RequestBody Map<String, Double> body) {
    Double value = body.get("value");
    Recipe recipe = recipeRepository.findById(id_recipe).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage() + id_recipe));

    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));

    RatingRecipe newRating = null;
    if (ratingRecipeRepository.existsByUserAndRecipe(user, recipe)) {
      recipe.deleteRating(ratingRecipeRepository.findByUserAndRecipe(user, recipe).getRating());
      recipeRepository.save(recipe);
      newRating = ratingRecipeRepository.findByUserAndRecipe(user, recipe);
      newRating.setRating(value);
      //ratingRecipeRepository.deleteById(ratingRecipeRepository.findByUser(user).getId());
    }
    recipe.setNumRatings(recipe.getNumRatings() + 1);
    recipe.setSumRatings(recipe.getSumRatings() + value);
    recipe.setRating(recipe.getSumRatings() / recipe.getNumRatings());
    if (newRating == null) {
      newRating = new RatingRecipe(user, recipe, value);
    }
    ratingRecipeRepository.save(newRating);
    Set<RatingRecipe> newRatingRecipe = recipe.getRatingRecipes();
    newRatingRecipe.add(newRating);
    recipe.setRatingRecipes(newRatingRecipe);
    recipeRepository.save(recipe);

    return new Resource<>(recipe,
        linkTo(methodOn(RecipeController.class).getRecipeById(id_recipe)).withSelfRel());
  }

  @DeleteMapping("{id_recipe}/rate")
  @ResponseStatus(HttpStatus.OK)
  public void deleteRateRecipe(@PathVariable Long id_recipe, @RequestHeader("token") String token) {
    Recipe recipe = recipeRepository.findById(id_recipe).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage() + id_recipe));

    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));

    if (recipe.existsRatingRecipeById(user.getId())) {
      recipe.deleteRatingById(user.getId());
      recipeRepository.save(recipe);
      ratingRecipeRepository
          .deleteById(ratingRecipeRepository.findByUserAndRecipe(user, recipe).getId());
    }
  }

  @PutMapping("/{id_recipe}/favourites/user")
  void addProductToFavourites(@PathVariable Long id_recipe, @RequestHeader("token") String token) {
    Recipe favouriteRecipe = recipeRepository.findById(id_recipe).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            "Cannot find a recipe with barcode " + id_recipe));

    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));

    user.setFavouriteRecipe(favouriteRecipe);
    userRepository.save(user);
  }

  @DeleteMapping("/{id_recipe}/favourites/user")
  void deleteProductFromFavourites(@PathVariable Long id_recipe, @RequestHeader("token") String token) {
    Recipe favouriteRecipe = recipeRepository.findById(id_recipe).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            "Cannot find a recipe with barcode " + id_recipe));

    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
    user.deleteFavouriteRecipe(favouriteRecipe);
    userRepository.save(user);
  }

  @GetMapping("search/byProduct/{id_product}")
  public Resources<Resource<Recipe>> searchRecipesByProduct(@PathVariable Long id_product) {
    Product product = productRepository.findById(id_product).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            "Cannot find a product with barcode " + id_product));

    List<Recipe> recipes = recipeRepository.findByUsedProducts(product);
    List<Resource<Recipe>> recipeResources = new ArrayList<>();
    for (Recipe next : recipes) {
      recipeResources.add(new Resource<>(next,
          linkTo(methodOn(RecipeController.class).getRecipeById(next.getId())).withSelfRel()));
    }
    return new Resources<>(recipeResources);
  }

  @GetMapping("search/byName")
  public Resources<Resource<Recipe>> searchRecipesByName(
      @RequestParam(name = "title") String recipeTitle) {

    List<Recipe> recipes = recipeRepository.findRecipeByTitleContaining(recipeTitle);
    List<Resource<Recipe>> recipeResources = new ArrayList<>();
    for (Recipe next : recipes) {
      recipeResources.add(new Resource<>(next,
          linkTo(methodOn(RecipeController.class).getRecipeById(next.getId())).withSelfRel()));
    }
    return new Resources<>(recipeResources);
  }
}
