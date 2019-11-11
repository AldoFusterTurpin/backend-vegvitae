package com.vegvitae.vegvitae.model;

import java.sql.Date;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(
    name = "products"
)
public class Product {

  @Id
  private Long barcode;

  @NotBlank(message = "Product name can't be blank.")
  private String name;

  @NotBlank(message = "Product base type can't be blank.")
  private ProductBaseTypeEnum baseType;

  @ElementCollection
  private Set<ProductAdditionalTypeEnum> additionalTypes;

  @ElementCollection
  private Set<SupermarketEnum> supermarketsAvailable;

  private String shop;

  @NotBlank
  private Date creationDate;

  @DecimalMin(value = "0.0", message = "Price has to be at least {value}.")
  private Long price;

  @DecimalMin(value = "0.0", message = "Rating has to be at least {value}.")
  @DecimalMax(value = "5.0", message = "Rating has to be less than or equal to {value}.")
  private double rating;

  private int numberOfRatings;

  {
    numberOfRatings = 0;
  }

  private double totalRatings;

  {
    totalRatings = 0;
  }

  @ManyToOne
  @JoinColumn(name = "uploader_id")
  @NotBlank(message = "Uploader id can't be blank.")
  private User uploader;

  @Size(max = 160, message = "The uploader comment has to be smaller than {max} characters.")
  private String uploaderComment;

  public Product() {
  }

  public Product(long barcode, String name, ProductBaseTypeEnum baseType,
      Set<ProductAdditionalTypeEnum> additionalTypes,
      Set<SupermarketEnum> supermarketsAvailable, String shop, User uploader,
      String uploaderComment, Long price,
      int numberOfRatings, Date creationDate, double rating) {
    this.barcode = barcode;
    this.name = name;
    this.baseType = baseType;
    this.additionalTypes = additionalTypes;
    this.supermarketsAvailable = supermarketsAvailable;
    this.shop = shop;
    this.uploader = uploader;
    this.uploaderComment = uploaderComment;
    this.price = price;
    this.creationDate = creationDate;
    this.numberOfRatings = numberOfRatings;

    if (this.numberOfRatings > 0) {
      ++this.numberOfRatings;
      this.rating = (this.totalRatings + rating) / this.numberOfRatings;
      this.totalRatings += rating;
    } else {
      if (rating >= 0) {
        this.rating = rating;
        this.totalRatings = rating;
        this.numberOfRatings = 1;
      }
    }
  }

  public long getBarcode() {
    return barcode;
  }

  public void setBarcode(long barcode) {
    this.barcode = barcode;
  }

  public Long getPrice() {
    return price;
  }

  public void setPrice(Long price) {
    this.price = price;
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
}
