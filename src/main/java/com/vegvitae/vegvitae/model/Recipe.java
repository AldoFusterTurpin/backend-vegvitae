package com.vegvitae.vegvitae.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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

  private Double rating;

  @JsonIgnore
  private long numRatings;

  @JsonIgnore
  private double sumRatings;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(name = "recipeComments", joinColumns = @JoinColumn(name = "recipeId"), inverseJoinColumns = @JoinColumn(name = "recipeCommentId"))
  List<RecipeComment> comments;

  @JsonIgnore
  @ManyToMany
  @JoinTable(name = "reportsRecipe", joinColumns = @JoinColumn(name = "idRecipe"), inverseJoinColumns = @JoinColumn(name = "userId"))
  Set<User> userReports;

  @JsonIgnore
  @OneToMany(mappedBy = "recipe")
  private Set<RatingRecipe> ratingRecipes;

  private Date creationDate;

  @NotNull
  private String process;

  @ManyToMany
  @JoinColumn(name = "product_id")
  private Set<Product> usedProducts;

  @JsonIgnore
  @Lob
  private byte[] recipeImage;

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
    this.ratingRecipes = new HashSet<>();
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

  public List<RecipeComment> getComments() {
    return comments;
  }

  public void setComments(List<RecipeComment> comments) {
    this.comments = comments;
  }

  public Set<User> getUserReports() {
    return userReports;
  }

  public void setUserReports(Set<User> userReports) {
    this.userReports = userReports;
  }

  public void removeProduct(Product productToRemove) {
    usedProducts.remove(productToRemove);
  }

  public Set<RatingRecipe> getRatingRecipes() {
    return ratingRecipes;
  }

  public void setRatingRecipes(Set<RatingRecipe> ratingRecipes) {
    this.ratingRecipes = ratingRecipes;
  }

  public void addRatingRecipe(User user, double rating) {
    RatingRecipe newRating = new RatingRecipe(user, this, rating);
    this.ratingRecipes.add(newRating);
  }

  public void deleteRating(Double value) {
    --this.numRatings;
    this.sumRatings = this.sumRatings - value;
    this.rating = this.sumRatings / this.numRatings;
  }

  public boolean existsRatingRecipeById(Long user_id) {

    for (RatingRecipe next : this.ratingRecipes) {
      if (next.getRecipe().getId() == this.id && next.getUser().getId() == user_id) {
        return true;
      }
    }
    return false;
  }

  public void deleteRatingById(Long id_user) {
    if (ratingRecipes.size() == 1) {
      this.numRatings = 0;
      this.sumRatings = 0;
      this.rating = 0.0;
      ratingRecipes = new HashSet<>();
    } else {
      for (RatingRecipe next : this.ratingRecipes) {
        if (next.getRecipe().getId() == this.id && next.getUser().getId() == id_user) {
          --this.numRatings;
          this.sumRatings -= next.getRating();
          this.rating = this.sumRatings / this.numRatings;
          ratingRecipes.remove(next);
        }
      }
    }
  }
}

