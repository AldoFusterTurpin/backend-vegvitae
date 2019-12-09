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
import javax.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.validator.constraints.Length;

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

  private String personalDescription;

  @ElementCollection
  private List<String> socialMediaLinks = new ArrayList<String>(4);

  @ElementCollection
  @OneToMany
  private Set<Product> uploadedProducts;
  
  @JsonIgnore
  @OneToMany(mappedBy = "user")
  private Set<Rating> productRatings;

  @JsonIgnore
  @Lob
  private byte[] image;

  @JsonIgnore
  @Column(name = "favourite_products")
  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "user_favourite_products", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "productBarcode"))
  private Set<Product> favouriteProducts;

  User() {
  }

  public User(String username, String password, String email, String personalDescription,
      List<String> socialMediaLinks) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.personalDescription = personalDescription;
    this.socialMediaLinks = socialMediaLinks;
    this.productRatings = new HashSet<Rating>();
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
  
  public Set<Rating> getProductRatings() {
    return productRatings;
  }

  public void setProductRatings(Set<Rating> productRatings) {
    this.productRatings = productRatings;
  }
}