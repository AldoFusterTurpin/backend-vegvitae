package com.vegvitae.vegvitae.model;

import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "products")
public class Product {

  @Id
  private Long barcode;

  @NotBlank
  private String name;

  private String description;

  private int rating;

  @ElementCollection
  private List<String> supermarketsAvailable;


}
