package com.vegvitae.vegvitae.model;

import java.util.ArrayList;
import javax.persistence.*;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"id", "username", "email"})}
)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String username;

  @NotBlank
  private String password;

  @NotBlank
  private String email;

  @Length(max = 5, message = "The field must be shorter than 160 characters")
  private String personalDescription;

  @ElementCollection
  @Size(max = 4, message = "Maximum 4 social media links")
  private List<String> socialMediaLinks = new ArrayList<String>(
      4); //por que solo pueden haber 4 links

  User() {
  }

  public User(String username, String password, String personalDescription,
      List<String> socialMediaLinks) {
    this.username = username;
    this.password = password;
    this.personalDescription = personalDescription;
    this.socialMediaLinks = socialMediaLinks;
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

}

