package com.vegvitae.vegvitae.model;


import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Auxiliary class that defines a composite key so we are able to limit one rating for user and
 * product
 */
@Embeddable
public class UserRecipeId implements Serializable {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "product_barcode")
  private Long recipeId;

  public UserRecipeId() {
  }

  public UserRecipeId(Long userId, Long recipeId) {
    this.userId = userId;
    this.recipeId = recipeId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getRecipeId() {
    return recipeId;
  }

  public void setRecipeId(Long recipeId) {
    this.recipeId = recipeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserRecipeId that = (UserRecipeId) o;
    return userId.equals(that.userId) &&
        recipeId.equals(that.recipeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, recipeId);
  }
}
