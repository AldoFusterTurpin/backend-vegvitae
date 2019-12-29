package com.vegvitae.vegvitae.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ratingsRecipe")
public class RatingRecipe {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
