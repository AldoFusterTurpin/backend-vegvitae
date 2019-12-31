package com.vegvitae.vegvitae.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

/**
 * Join table for product ratings
 **/
@Entity
@Table(name = "ratingsRecipe")
public class RatingRecipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "rater_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;

  @Column(name = "rating")
  @DecimalMin(value = "0.0", message = "Rating has to be at least {value}.")
  @DecimalMax(value = "5.0", message = "Rating has to be less than or equal to {value}.")
  private Double rating;

  public RatingRecipe() {
  }

  public RatingRecipe(User user, Recipe recipe, Double rating) {
    this.user = user;
    this.recipe = recipe;
    this.rating = rating;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public void setRecipe(Recipe recipe) {
    this.recipe = recipe;
  }

  public Double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RatingRecipe ratingRecipe = (RatingRecipe) o;
    return Double.compare(ratingRecipe.rating, this.rating) == 0 &&
        id.equals(ratingRecipe.id) &&
        user.equals(ratingRecipe.user) &&
        recipe.equals(ratingRecipe.recipe);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, user, recipe, rating);
  }
}
