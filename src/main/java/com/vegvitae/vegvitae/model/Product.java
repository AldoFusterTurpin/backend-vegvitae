package com.vegvitae.vegvitae.model;

import java.sql.Date;
import java.util.Calendar;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "products")
@JsonIgnoreProperties("hibernateLazyInitializer") // Removes a useless field from the JSON response
public class Product {

  @Id
  private Long barcode;

  @NotBlank(message = "Product name can't be blank.")
  private String name;

  @NotNull(message = "Product base type can't be blank.")
  private ProductBaseTypeEnum baseType;

  @ElementCollection
  private Set<ProductAdditionalTypeEnum> additionalTypes;

  @ElementCollection
  private Set<SupermarketEnum> supermarketsAvailable;

  private String shop;

  @ManyToOne
  @JoinColumn(name = "uploader_id")
  @NotNull(message = "Uploader can't be NULL.")
  private User uploader;

  @Size(max = 160, message = "The uploader comment has to be smaller than {max} characters.")
  private String uploaderComment;

  @DecimalMin(value = "0.0", message = "Rating has to be at least {value}.")
  @DecimalMax(value = "5.0", message = "Rating has to be less than or equal to {value}.")
  private double rating;

  private int numberOfRatings;

  private double totalRatings;

  @NotNull
  private Date creationDate;

  public Product() {
  }

  public Product(long barcode, String name, ProductBaseTypeEnum baseType,
      Set<ProductAdditionalTypeEnum> additionalTypes,
      Set<SupermarketEnum> supermarketsAvailable, String shop, User uploader,
      String uploaderComment) {
    this.barcode = barcode;
    this.name = name;
    this.baseType = baseType;
    this.additionalTypes = additionalTypes;
    this.supermarketsAvailable = supermarketsAvailable;
    this.shop = shop;
    this.uploader = uploader;
    this.uploaderComment = uploaderComment;
    this.numberOfRatings = 0;
    this.totalRatings = 0;
    this.creationDate = getCurrentDate();
  }

  public long getBarcode() {
    return barcode;
  }

  public void setBarcode(long barcode) {
    this.barcode = barcode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getNumberOfRatings() {
    return numberOfRatings;
  }

  public void setNumberOfRatings(int numberOfRatings) {
    this.numberOfRatings = numberOfRatings;
  }

  public double getTotalRatings() {
    return totalRatings;
  }

  public void setTotalRatings(double totalRatings) {
    this.totalRatings = totalRatings;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
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

  public void setAdditionalTypes(Set<ProductAdditionalTypeEnum> additionalTypes) {
    this.additionalTypes = additionalTypes;
  }

  public Set<SupermarketEnum> getSupermarketsAvailable() {
    return supermarketsAvailable;
  }

  public void setSupermarketsAvailable(Set<SupermarketEnum> supermarketsAvailable) {
    this.supermarketsAvailable = supermarketsAvailable;
  }

  public String getShop() {
    return shop;
  }

  public void setShop(String shop) {
    this.shop = shop;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public User getUploader() {
    return uploader;
  }

  public void setUploader(User uploader) {
    this.uploader = uploader;
  }

  public String getUploaderComment() {
    return uploaderComment;
  }

  public void setUploaderComment(String uploaderComment) {
    this.uploaderComment = uploaderComment;
  }

  private Date getCurrentDate() {
    Calendar calendar = Calendar.getInstance();
    java.util.Date currentDate = calendar.getTime();
    return new java.sql.Date(currentDate.getTime());
  }
}
