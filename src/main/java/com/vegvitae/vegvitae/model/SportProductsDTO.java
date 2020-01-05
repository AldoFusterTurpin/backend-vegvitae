package com.vegvitae.vegvitae.model;

import java.util.Set;

public class SportProductsDTO {

  private String productName;

  private String baseType;

  private Set<String> additionalTypes;


  public SportProductsDTO(String productName, String baseType,
      Set<String> additionalTypes) {
    this.productName = productName;
    this.baseType = baseType;
    this.additionalTypes = additionalTypes;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public String getBaseType() {
    return baseType;
  }

  public void setBaseType(String baseType) {
    this.baseType = baseType;
  }

  public Set<String> getAdditionalTypes() {
    return additionalTypes;
  }

  public void setAdditionalTypes(
      Set<String> additionalTypes) {
    this.additionalTypes = additionalTypes;
  }
}
