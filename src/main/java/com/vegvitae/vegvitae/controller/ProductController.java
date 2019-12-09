package com.vegvitae.vegvitae.controller;


import static com.vegvitae.vegvitae.model.OrderTypeEnum.DATE_ASC;
import static com.vegvitae.vegvitae.model.OrderTypeEnum.DATE_DESC;
import static com.vegvitae.vegvitae.model.OrderTypeEnum.HIGH_RATE;
import static com.vegvitae.vegvitae.model.OrderTypeEnum.LOW_RATE;
import static com.vegvitae.vegvitae.model.OrderTypeEnum.TODAY;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.newProductDTO;
import com.vegvitae.vegvitae.model.OrderTypeEnum;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.ProductAdditionalTypeEnum;
import com.vegvitae.vegvitae.model.ProductBaseTypeEnum;
import com.vegvitae.vegvitae.model.Rating;
import com.vegvitae.vegvitae.model.SupermarketEnum;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.model.UserProductId;
import com.vegvitae.vegvitae.repository.ProductRepository;
import com.vegvitae.vegvitae.repository.RatingRepository;
import com.vegvitae.vegvitae.repository.UserRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api" + ProductController.PATH)
public class ProductController {

  static final String PATH = "/products";

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RatingRepository ratingRepository;

  @PostMapping(produces = "application/hal+json")
  public Resource<Product> newProduct(@Valid @RequestBody newProductDTO productDTO) {

    if (productRepository.existsById(productDTO.getBarcode())) {
      throw new GenericException(HttpStatus.CONFLICT,
          ExceptionMessages.PRODUCT_EXISTS.getErrorMessage());
    }

    // Creating an instance of Product with the content of the DTO
    User uploader = userRepository.getOne(productDTO.getUploaderId());
    Product product = new Product(productDTO.getBarcode(), productDTO.getName(),
        productDTO.getBaseType(), productDTO.getAdditionalTypes(),
        productDTO.getSupermarketsAvailable(), productDTO.getShop(), uploader,
        productDTO.getUploaderComment());

    productRepository.save(product);

    Link selfLink = linkTo(ProductController.class).slash(product.getBarcode()).withSelfRel();
    return new Resource<Product>(product, selfLink);
  }

  @GetMapping
  Resources<Resource<Product>> getAllProducts(
      @RequestParam(name = "order", required = false) OrderTypeEnum orderBy,
      @RequestParam(name = "user", required = false) Long userId) {

    Comparator<Product> comparator = null;
    if(orderBy != null) {
      switch (orderBy) {
        case TODAY:
          Calendar calendar = Calendar.getInstance();
          java.util.Date currentDate = calendar.getTime();
          java.sql.Date today = new java.sql.Date(currentDate.getTime());
          List<Resource<Product>> products = productRepository.findByCreationDate(today).stream()
              .map(product -> new Resource<>(product,
                  linkTo(
                      methodOn(ProductController.class).getProductByBarcode(product.getBarcode()))
                      .withSelfRel()))
              .collect(Collectors.toList());
          if (userId != null) {
            products = products.stream()
                .filter(prod -> prod.getContent().getUploader().getId().equals(userId)).collect(
                    Collectors.toList());
          }
          return new Resources<>(products,
              linkTo(methodOn(ProductController.class).getAllProducts(TODAY, userId))
                  .withSelfRel());
        case HIGH_RATE:
          comparator = Comparator.comparing((Product::getRating)).reversed();
          break;
        case LOW_RATE:
          comparator = Comparator.comparing((Product::getRating));
          break;
        case DATE_ASC:
          comparator = Comparator.comparing((Product::getCreationDate));
          break;
        case DATE_DESC:
          comparator = Comparator.comparing((Product::getCreationDate)).reversed();
          break;
        default:
          comparator = Comparator.comparing(Product::getRating);
          comparator = comparator.thenComparing(Comparator.comparing(Product::getCreationDate))
              .reversed();
          break;
      }
    }
    else {
      comparator = Comparator.comparing(Product::getRating);
    }

    List<Resource<Product>> products = productRepository.findAll().stream().sorted(comparator)
        .map(product -> new Resource<>(product,
            linkTo(methodOn(ProductController.class).getProductByBarcode(product.getBarcode()))
                .withSelfRel()))
        .collect(Collectors.toList());
    if (userId != null) {
      products = products.stream()
          .filter(prod -> prod.getContent().getUploader().getId().equals(userId)).collect(
              Collectors.toList());
    }
    return new Resources<>(products,
        linkTo(methodOn(ProductController.class).getAllProducts(orderBy, userId)).withSelfRel());
  }

  @GetMapping("/{id}")
  Resource<Product> getProductByBarcode(@PathVariable Long id) {

    Product product = productRepository.findById(id)
        .orElseThrow(
            () -> new GenericException(HttpStatus.NOT_FOUND,
                "Cannot find product with barcode " + id));

    return getAllLinksResourceWithAllLinks(product);

  }

  @GetMapping("/search")
  Resources<Resource<Product>> getProductsByTags(
      @RequestParam(name = "base", required = false) List<ProductBaseTypeEnum> baseTags,
      @RequestParam(name = "additional", required = false) List<ProductAdditionalTypeEnum> additionalTags,
      @RequestParam(name = "supermarkets", required = false) List<SupermarketEnum> supermarkets,
      @RequestParam(name = "name", required = false) List<String> name,
      @RequestParam(name = "shop", required = false) List<String> shop) {

    List<Resource<Product>> resourceProducts = new ArrayList<>();
    List<Product> products = new ArrayList<>();
    if (name != null) {
      Set<Product> setProducts = new HashSet<>();
      for (String n : name) {
        setProducts.addAll(productRepository.findByNameContaining(n));
      }
      products.addAll(setProducts);
    }
    if (supermarkets != null) {
      if (products.isEmpty()) {
        products = productRepository.findBySupermarketsAvailable(supermarkets);
      } else {
        products.retainAll(productRepository.findBySupermarketsAvailable(supermarkets));
      }
    }
    if (additionalTags != null) {
      if (products.isEmpty()) {
        products = productRepository.findByAdditionalTypesIn(additionalTags);
      } else {
        products.retainAll(productRepository.findByAdditionalTypesIn(additionalTags));
      }
    }
    if (baseTags != null) {
      if (products.isEmpty()) {
        products = productRepository.findByBaseTypeIn(baseTags);
      } else {
        products.retainAll(productRepository.findByBaseTypeIn(baseTags));
      }
    }
    if (shop != null) {
      Set<Product> setProducts = new HashSet<>();
      for (String n : shop) {
        setProducts.addAll(productRepository.findByShopContaining(n));
      }
      if (products.isEmpty()) {
        products.addAll(setProducts);
      } else {
        products.retainAll(setProducts);
      }
    }

    for (Product prod : products) {
      resourceProducts.add(new Resource<>(prod,
          linkTo(methodOn(ProductController.class).getProductByBarcode(prod.getBarcode()))
              .withSelfRel()));
    }
    return new Resources<>(getAllLinksResourcesWithAllLinks(resourceProducts));
  }

  @GetMapping("/additionalEnum")
  ProductAdditionalTypeEnum[] getAdditionalEnums() {
    return ProductAdditionalTypeEnum.values();
  }

  @GetMapping("/baseEnum")
  ProductBaseTypeEnum[] getBaseEnums() {
    return ProductBaseTypeEnum.values();
  }

  @GetMapping("/supermarketEnum")
  SupermarketEnum[] getSupermarketEnums() {
    return SupermarketEnum.values();
  }

  @GetMapping("/orderEnum")
  OrderTypeEnum[] getOrderEnums() {
    return OrderTypeEnum.values();
  }

  @DeleteMapping("{id}")
  void deleteProductById(@PathVariable Long id) {
    //no s'implementa
  }

  @PutMapping("/{id_product}/favourites/user/{id_user}")
  void addProductToFavourites(@PathVariable Long id_product, @PathVariable Long id_user) {
    Product favouriteProd = productRepository.findById(id_product).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            "Cannot find product with barcode " + id_product));

    User user = userRepository.findById(id_user).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            "Cannot find user with barcode " + id_user));

    user.setFavouriteProduct(favouriteProd);
    userRepository.save(user);
  }

  @DeleteMapping("/{id_product}/favourites/user/{id_user}")
  void deleteProductFromFavourites(@PathVariable Long id_product, @PathVariable Long id_user) {
    Product favouriteProd = productRepository.findById(id_product).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            "Cannot find product with barcode " + id_product));

    User user = userRepository.findById(id_user).orElseThrow(
        () -> new GenericException(HttpStatus.NOT_FOUND,
            "Cannot find user with id " + id_user));
    user.deleteFavouriteProduct(favouriteProd);
    userRepository.save(user);
  }

  private Resources<Resource<Product>> getAllLinksResourcesWithAllLinks(List<Resource<Product>> p) {
    return new Resources<>(p,
        linkTo(methodOn(ProductController.class).getAllProducts(TODAY, null))
            .withRel("productsOrderToday"),
        linkTo(methodOn(ProductController.class).getAllProducts(HIGH_RATE, null))
            .withRel("productsOrderHighRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(LOW_RATE, null))
            .withRel("productsOrderLowRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_ASC, null))
            .withRel("productsOrderDateAsc"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_DESC, null))
            .withRel("productsOrderDateDesc"));
  }

  private Resource<Product> getAllLinksResourceWithAllLinks(Product p) {
    return new Resource<>(p,
        linkTo(methodOn(ProductController.class).getProductByBarcode(p.getBarcode()))
            .withRel("product"),
        linkTo(methodOn(ProductController.class).getAllProducts(TODAY, null))
            .withRel("productsOrderToday"),
        linkTo(methodOn(ProductController.class).getAllProducts(HIGH_RATE, null))
            .withRel("productsOrderHighRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(LOW_RATE, null))
            .withRel("productsOrderLowRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_ASC, null))
            .withRel("productsOrderDateAsc"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_DESC, null))
            .withRel("productsOrderDateDesc"));
  }

  @GetMapping("/{productId}/rating")
  public Resource<Map<String, Double>> getProductRating(@PathVariable Long productId) {
    if (!productRepository.existsById(productId)) {
      throw new GenericException(HttpStatus.NOT_FOUND,
          "Product with id " + productId + " doesn't exist");
    }

    Map<String, Double> body = new HashMap<String, Double>();
    body.put("rating", productRepository.getOne(productId).getRating());

    Link selfLink = linkTo(methodOn(ProductController.class).getProductRating(productId))
        .withSelfRel();
    return new Resource<Map<String, Double>>(body, selfLink);
  }

  @GetMapping("/{productId}/users/{userId}/rating")
  public Resource<Map<String, Double>> getUserRating(@PathVariable Long productId, @PathVariable Long userId) {
    if (!productRepository.existsById(productId)) {
      throw new GenericException(HttpStatus.NOT_FOUND,
          "Product with id " + productId + " doesn't exist");
    }
    else if (!userRepository.existsById(userId)) {
      throw new GenericException(HttpStatus.NOT_FOUND,
          "User with id " + userId + " doesn't exist");
    }
    else if (!ratingRepository.existsById(new UserProductId(userId, productId))) {
      throw new GenericException(HttpStatus.NOT_FOUND,
          "The user hasn’t rated this product");
    }

    Map<String, Double> body = new HashMap<String, Double>();
    body.put("rating", ratingRepository.getOne(new UserProductId(userId, productId)).getRating());

    Link selfLink = linkTo(methodOn(ProductController.class).getUserRating(productId, userId))
        .withSelfRel();
    return new Resource<Map<String, Double>>(body, selfLink);
  }

  @PutMapping("/{productId}/users/{userId}/rating")
  public Resource<Product> addRating(@PathVariable Long productId, @PathVariable Long userId,
      @RequestBody Map<String, Double> body) {
    Double value = body.get("value");
    User user = userRepository.getOne(userId);
    Product product = productRepository.getOne(productId);
    Rating newRating = new Rating(user, product, value);

    // Make the associations between the join table Rating, User and Product
    Set<Rating> userRatings = user.getProductRatings();
    Set<Rating> productRatings = product.getRatings();
    if (ratingRepository.existsById(new UserProductId(userId, productId))) {
      Rating oldRating = ratingRepository.getOne(new UserProductId(userId, productId));
      // Change the user product rating
      product.changeUserRating(oldRating.getRating(), value);
      userRatings.remove(oldRating);
      productRatings.remove(oldRating);
      ratingRepository.delete(oldRating);
    }
    else {
      // Add a new user rating to Product
      product.addUserRating(value);
    }
    userRatings.add(newRating);
    productRatings.add(newRating);

    // Save the entry into the join table Rating
    ratingRepository.save(newRating);

    // Update the state of the Product entity
    productRepository.save(product);

    // Update the state of the User entity
    userRepository.save(user);

    Link selfLink = linkTo(methodOn(ProductController.class).addRating(productId, userId, new HashMap<String, Double>()))
        .withSelfRel();
    return new Resource<Product>(product, selfLink);
  }
}
