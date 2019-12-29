package com.vegvitae.vegvitae.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

/** Join table for product ratings **/
@Entity
@Table(name = "ratingsProduct")
public class RatingProduct {

  @EmbeddedId
  private UserProductId id;

  @ManyToOne
  @MapsId("userId")
  private User user;

  @ManyToOne
  @MapsId("productBarcode")
  private Product product;

  @Column(name = "rating")
  @DecimalMin(value = "0.0", message = "Rating has to be at least {value}.")
  @DecimalMax(value = "5.0", message = "Rating has to be less than or equal to {value}.")
  private Double rating;

  public RatingProduct() {
  }

  public RatingProduct(User user, Product product, Double rating) {
    this.id = new UserProductId(user.getId(), product.getBarcode());
    this.user = user;
    this.product = product;
    this.rating = rating;
  }

  public UserProductId getId() {
    return id;
  }

  public void setId(UserProductId id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
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
    RatingProduct ratingProduct = (RatingProduct) o;
    return Double.compare(ratingProduct.rating, this.rating) == 0 &&
        id.equals(ratingProduct.id) &&
        user.equals(ratingProduct.user) &&
        product.equals(ratingProduct.product);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, user, product, rating);
  }
}
