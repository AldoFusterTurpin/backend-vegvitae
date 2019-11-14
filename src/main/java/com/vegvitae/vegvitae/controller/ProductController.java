package com.vegvitae.vegvitae.controller;

import static com.vegvitae.vegvitae.model.OrderTypeEnum.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.OrderTypeEnum;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.ProductAdditionalTypeEnum;
import com.vegvitae.vegvitae.model.ProductBaseTypeEnum;
import com.vegvitae.vegvitae.model.SupermarketEnum;
import com.vegvitae.vegvitae.repository.ProductRepository;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api" + ProductController.PATH)
public class ProductController {

  public static final String PATH = "/products";

  @Autowired
  private ProductRepository productRepository;

  @GetMapping
  Resources<Resource<Product>> getAllProducts(
      @RequestParam(name = "order", required = false) OrderTypeEnum orderBy) {

    Comparator<Product> comparator;
    switch (orderBy) {
      case TODAY:
        Calendar calendar = Calendar.getInstance();
        java.util.Date currentDate = calendar.getTime();
        java.sql.Date today = new java.sql.Date(currentDate.getTime());
        List<Resource<Product>> products = productRepository.findByCreationDate(today).stream()
            .map(product -> new Resource<>(product,
                linkTo(methodOn(ProductController.class).getProductByBarcode(product.getBarcode()))
                    .withSelfRel()))
            .collect(Collectors.toList());
        return new Resources<>(products,
            linkTo(methodOn(ProductController.class).getAllProducts(TODAY)).withSelfRel());
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

    List<Resource<Product>> products = productRepository.findAll().stream().sorted(comparator)
        .map(product -> new Resource<>(product,
            linkTo(methodOn(ProductController.class).getProductByBarcode(product.getBarcode()))
                .withSelfRel()))
        .collect(Collectors.toList());
    return new Resources<>(products,
        linkTo(methodOn(ProductController.class).getAllProducts(orderBy)).withSelfRel());
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
    productRepository.deleteById(id);
  }


  /**
   * private functions
   */
  Resources<Resource<Product>> getAllLinksResourcesWithAllLinks(List<Resource<Product>> p) {
    return new Resources<>(p,
        linkTo(methodOn(ProductController.class).getAllProducts(TODAY))
            .withRel("productsOrderToday"),
        linkTo(methodOn(ProductController.class).getAllProducts(HIGH_RATE))
            .withRel("productsOrderHighRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(LOW_RATE))
            .withRel("productsOrderLowRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_ASC))
            .withRel("productsOrderDateAsc"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_DESC))
            .withRel("productsOrderDateDesc"));
  }

  Resource<Product> getAllLinksResourceWithAllLinks(Product p) {
    return new Resource<>(p,
        linkTo(methodOn(ProductController.class).getProductByBarcode(p.getBarcode()))
            .withRel("product"),
        linkTo(methodOn(ProductController.class).getAllProducts(TODAY))
            .withRel("productsOrderToday"),
        linkTo(methodOn(ProductController.class).getAllProducts(HIGH_RATE))
            .withRel("productsOrderHighRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(LOW_RATE))
            .withRel("productsOrderLowRate"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_ASC))
            .withRel("productsOrderDateAsc"),
        linkTo(methodOn(ProductController.class).getAllProducts(DATE_DESC))
            .withRel("productsOrderDateDesc"));
  }
}
