package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api" + ProductController.PATH)
public class ProductController {

  public static final String PATH = "/products";

  @Autowired
  private ProductRepository productRepository;

  @GetMapping
  String test() {
    return "HELLO ITS WORKING";
  }

}
