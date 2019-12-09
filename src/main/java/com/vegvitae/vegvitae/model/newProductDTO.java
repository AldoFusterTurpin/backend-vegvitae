package com.vegvitae.vegvitae.model;

import java.util.Set;

public class newProductDTO {

  private Long barcode;

  private String name;

  private ProductBaseTypeEnum baseType;

  private Set<ProductAdditionalTypeEnum> additionalTypes;

  private Set<SupermarketEnum> supermarketsAvailable;

  private String shop;

  private Long uploaderId;

  private String uploaderComment;


  public newProductDTO() {

  }

  public newProductDTO(Long barcode, String name,
      ProductBaseTypeEnum baseType,
      Set<ProductAdditionalTypeEnum> additionalTypes,
      Set<SupermarketEnum> supermarketsAvailable, String shop, Long uploaderId,
      String uploaderComment) {
    this.barcode = barcode;
    this.name = name;
    this.baseType = baseType;
    this.additionalTypes = additionalTypes;
    this.supermarketsAvailable = supermarketsAvailable;
    this.shop = shop;
    this.uploaderId = uploaderId;
    this.uploaderComment = uploaderComment;
  }

  public Long getBarcode() {
    return barcode;
  }

  public void setBarcode(Long barcode) {
    this.barcode = barcode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ProductBaseTypeEnum getBaseType() {
    return baseType;
  }

  public void setBaseType(ProductBaseTypeEnum baseType) {
    this.baseType = baseType;
  }

  public Set<ProductAdditionalTypeEnum> getAdditionalTypes() {
    return additionalTypes;
  }

  public void setAdditionalTypes(
      Set<ProductAdditionalTypeEnum> additionalTypes) {
    this.additionalTypes = additionalTypes;
  }

  public Set<SupermarketEnum> getSupermarketsAvailable() {
    return supermarketsAvailable;
  }

  public void setSupermarketsAvailable(
      Set<SupermarketEnum> supermarketsAvailable) {
    this.supermarketsAvailable = supermarketsAvailable;
  }

  public String getShop() {
    return shop;
  }

  public void setShop(String shop) {
    this.shop = shop;
  }

  public Long getUploaderId() {
    return uploaderId;
  }

  public void setUploaderId(Long uploaderId) {
    this.uploaderId = uploaderId;
  }

  public String getUploaderComment() {
    return uploaderComment;
  }

  public void setUploaderComment(String uploaderComment) {
    this.uploaderComment = uploaderComment;
  }
}
