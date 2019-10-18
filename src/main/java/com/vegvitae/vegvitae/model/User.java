package com.vegvitae.vegvitae.model;

import javax.persistence.*;
import java.util.List;
import javax.validation.constraints.NotBlank;


@Entity
@Table(name = "users")
public
class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String username;

  @NotBlank
  private String password;

  @NotBlank
  private String email;

  private String personalDescription;

  @ElementCollection
  private List<String> socialMediaLinks;

  User() {
  }

  public User(String name, String password, String personalDescription,
      List<String> socialMediaLinks) {
    this.name = name;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

