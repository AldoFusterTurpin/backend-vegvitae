package com.vegvitae.vegvitae.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Date;
import java.util.Calendar;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
  private Double rating;

  @JsonIgnore
  private long numRatings;

  @JsonIgnore
  private double sumRatings;

  @JsonIgnore
  @OneToMany(mappedBy = "product")
  private Set<RatingProduct> ratingProducts;

  @NotNull
  private Date creationDate;

  @JsonIgnore
  @Lob
  private byte[] image;

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
    this.rating = 0.0;
    this.numRatings = 0;
    this.sumRatings = 0;
    this.ratingProducts = new HashSet<RatingProduct>();
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

  public Set<RatingProduct> getRatingProducts() {
    return ratingProducts;
  }

  public void setRatingProducts(Set<RatingProduct> ratingProducts) {
    this.ratingProducts = ratingProducts;
  }

  public long getNumRatings() {
    return numRatings;
  }

  public void setNumRatings(long numRatings) {
    this.numRatings = numRatings;
  }

  public double getSumRatings() {
    return sumRatings;
  }

  public void setSumRatings(double sumRatings) {
    this.sumRatings = sumRatings;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  public Double getRating(){
    return rating;
  }

  public void addUserRating(Double value) {
    numRatings++;
    sumRatings += value;
    rating = sumRatings / numRatings;
  }

  public void changeUserRating(Double oldValue, Double newValue){
    sumRatings += newValue - oldValue;
    rating = sumRatings / numRatings;
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

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  private Date getCurrentDate() {
    Calendar calendar = Calendar.getInstance();
    java.util.Date currentDate = calendar.getTime();
    return new java.sql.Date(currentDate.getTime());
  }
}
