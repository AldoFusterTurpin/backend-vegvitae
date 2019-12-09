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
public class UserProductId implements Serializable {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "product_barcode")
  private Long productBarcode;

  public UserProductId() {
  }

  public UserProductId(Long userId, Long productBarcode) {
    this.userId = userId;
    this.productBarcode = productBarcode;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getProductBarcode() {
    return productBarcode;
  }

  public void setProductBarcode(Long productBarcode) {
    this.productBarcode = productBarcode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserProductId that = (UserProductId) o;
    return userId.equals(that.userId) &&
        productBarcode.equals(that.productBarcode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, productBarcode);
  }
}
