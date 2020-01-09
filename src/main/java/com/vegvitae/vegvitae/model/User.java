package com.vegvitae.vegvitae.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"id", "username", "email"})}
)
@JsonIgnoreProperties("hibernateLazyInitializer") // Removes a useless field from the JSON response
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String username;

  @NotBlank
  private String password;

  @NotBlank
  @Email
  private String email;

  private String token;

  private int points;

  private boolean pointsSecondaryApp = false;

  private String personalDescription;

  @ElementCollection
  private List<String> socialMediaLinks = new ArrayList<String>(4);

  @ElementCollection
  @OneToMany
  private Set<Product> uploadedProducts;

  @JsonIgnore
  @OneToMany(mappedBy = "user")
  private Set<RatingProduct> productRatings;

  @JsonIgnore
  @Lob
  private byte[] image;

  @JsonIgnore
  @Column(name = "favourite_products")
  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "user_favourite_products", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "productBarcode"))
  private Set<Product> favouriteProducts;

  @JsonIgnore
  @Column(name = "favourite_recipe")
  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "user_favourite_recipe", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "recipeId"))
  private Set<Recipe> favouriteRecipes;

  public User() {
  }

  public User(String username, String password, String email, String personalDescription,
      List<String> socialMediaLinks) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.points = 0;
    this.personalDescription = personalDescription;
    this.socialMediaLinks = socialMediaLinks;
    this.productRatings = new HashSet<RatingProduct>();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPersonalDescription() {
    return personalDescription;
  }

  public void setPersonalDescription(String personalDescription) {
    this.personalDescription = personalDescription;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<String> getSocialMediaLinks() {
    return socialMediaLinks;
  }

  public void setSocialMediaLinks(List<String> socialMediaLinks) {
    this.socialMediaLinks = socialMediaLinks;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public void addPoints(int pointsToAdd) {
    this.points += pointsToAdd;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  public Set<Product> getFavouriteProducts() {
    return favouriteProducts;
  }

  public void setFavouriteProducts(Set<Product> favouriteProducts) {
    this.favouriteProducts = favouriteProducts;
  }

  public void setFavouriteProduct(Product favouriteProduct) {
    this.favouriteProducts.add(favouriteProduct);
  }

  public void deleteFavouriteProduct(Product productDeleted) {
    this.favouriteProducts.remove(productDeleted);
  }

  public Set<RatingProduct> getProductRatings() {
    return productRatings;
  }

  public void setProductRatings(Set<RatingProduct> productRatings) {
    this.productRatings = productRatings;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Set<Product> getUploadedProducts() {
    return uploadedProducts;
  }

  public void setUploadedProducts(Set<Product> uploadedProducts) {
    this.uploadedProducts = uploadedProducts;
  }

  public Set<Recipe> getFavouriteRecipes() {
    return favouriteRecipes;
  }

  public void setFavouriteRecipe(Recipe favouriteRecipe) {
    this.favouriteRecipes.add(favouriteRecipe);
  }

  public void deleteFavouriteRecipe(Recipe deletedRecipe){
    this.favouriteRecipes.remove(deletedRecipe);
  }

  public boolean isPointsSecondaryApp() {
    return pointsSecondaryApp;
  }

  public void setPointsSecondaryApp(boolean pointsSecondaryApp) {
    this.pointsSecondaryApp = pointsSecondaryApp;
  }
}