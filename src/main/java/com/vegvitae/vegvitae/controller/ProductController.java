package com.vegvitae.vegvitae.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.ProductAdditionalTypeEnum;
import com.vegvitae.vegvitae.model.ProductBaseTypeEnum;
import com.vegvitae.vegvitae.model.SupermarketEnum;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.ProductRepository;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
  Resources<Resource<Product>> getAllProducts() {

    Comparator<Product> comparator = Comparator.comparing(Product::getRating);
    comparator = comparator.thenComparing(Comparator.comparing(Product::getCreationDate));

    List<Resource<Product>> products = productRepository.findAll().stream().sorted(comparator)
        .map(product -> new Resource<>(product,
            linkTo(methodOn(ProductController.class).getProductById(product.getBarcode()))
                .withSelfRel(),
            linkTo(methodOn(ProductController.class).getAllProducts()).withRel("products")))
        .collect(Collectors.toList());
    return new Resources<>(products,
        linkTo(methodOn(ProductController.class).getAllProducts()).withSelfRel());
  }

  @GetMapping("/{id}")
  Resource<Product> getProductById(@PathVariable Long id) {

    Product product = productRepository.findById(id)
        .orElseThrow(
            () -> new GenericException(HttpStatus.NOT_FOUND, "Cannot find user with id" + id));
    return new Resource<>(product,
        linkTo(methodOn(ProductController.class).getProductById(id)).withSelfRel(),
        linkTo(methodOn(ProductController.class).getAllProducts()).withRel("products"));
  }

  @GetMapping("/search")
  Resources<Resource<Product>> getProductsByTags(
      @RequestParam(name = "base", required = false) List<ProductBaseTypeEnum> baseTags,
      @RequestParam(name = "aditional", required = false) List<ProductAdditionalTypeEnum> additionalTags,
      @RequestParam(name = "supermarket", required = false) List<SupermarketEnum> supermarkets,
      @RequestParam(name = "name", required = false) List<String> name) {

    //comprovar que tags se pasan y buscarlos en la BD
    List<Resource<Product>> resourceProducts = new ArrayList<>();
    List<Product> products = new ArrayList<>();
    if (name != null) { //se ha de decidir que forma usamos!!!!!!!!!!!!!!!!!!!!!!!
      //find by name normal
      //products = productRepository.findByNameIn(name);

      //find by containing part of the name
      /*
      Set<Product> setProducts = new HashSet<>();
      for(String n : name) {
        setProducts.addAll(productRepository.findByNameContaining(n));
      }
      products.addAll(setProducts);
       */
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

    for (Product prod : products) {
      resourceProducts.add(new Resource<>(prod,
          linkTo(methodOn(ProductController.class).getProductById(prod.getBarcode())).withSelfRel(),
          linkTo(methodOn(ProductController.class).getAllProducts()).withRel("products")));
    }
    return new Resources<>(resourceProducts,
        linkTo(methodOn(ProductController.class).getAllProducts()).withSelfRel());
  }

}
