package com.vegvitae.vegvitae.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "recipe")
public class Recipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  private String title;

  @ManyToOne
  @JoinColumn(name = "creator_id")
  private User creator;

  @DecimalMin(value = "0.0", message = "Rating has to be at least {value}.")
  @DecimalMax(value = "5.0", message = "Rating has to be less than or equal to {value}.")
  private Double rating;

  @JsonIgnore
  private long numRatings;

  @JsonIgnore
  private double sumRatings;

  /*
  @JsonIgnore
  @OneToMany(mappedBy = "recipe")
  private Set<RatingRecipe> ratingRecipe;
  */

  private Date creationDate;

  @NotNull
  private String process;

  @ElementCollection
  @ManyToMany
  private Set<Product> usedProducts;

  /*
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "recipe")
    private Set<UploadedFile> attachments;
     */
  @JsonIgnore
  @Lob
  byte[] recipeImage;


  public Recipe() {
  }

  public Recipe(Long id, String title, User creator, String process, Set<Product> usedProducts) {
    this.id = id;
    this.title = title;
    this.creator = creator;
    this.process = process;
    if (usedProducts != null) {
      this.usedProducts = usedProducts;
    } else {
      this.usedProducts = null;
    }
    this.rating = 0.0;
    this.numRatings = 0;
    this.sumRatings = 0;
    //this.ratingRecipe = new HashSet<RatingRecipe>();
    this.creationDate = getCurrentDate();
  }

  private Date getCurrentDate() {
    Calendar calendar = Calendar.getInstance();
    java.util.Date currentDate = calendar.getTime();
    return new java.sql.Date(currentDate.getTime());
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public User getCreator() {
    return creator;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }

  public Double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
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

  /*
  public Set<RatingRecipe> getRatingRecipe() {
    return ratingRecipe;
  }

  public void setRatingRecipe(Set<RatingRecipe> ratingRecipe) {
    this.ratingRecipe = ratingRecipe;
  }
   */

  public String getProcess() {
    return process;
  }

  public void setProcess(String process) {
    this.process = process;
  }

  public Set<Product> getUsedProducts() {
    return usedProducts;
  }

  public void setUsedProduct(Set<Product> usedProducts) {
    this.usedProducts = usedProducts;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public Long getId() {
    return id;
  }

  public void addUsedProduct(Product productToAdd) {
    this.usedProducts.add(productToAdd);
  }

  public byte[] getRecipeImage() {
    return recipeImage;
  }

  public void setRecipeImage(byte[] recipeImage) {
    this.recipeImage = recipeImage;
  }

  /*
  public Set<UploadedFile> getAttachments() {
    return attachments;
  }

  public void setAttachment(UploadedFile newImage) {
    attachments.add(newImage);
  }
   */

}
