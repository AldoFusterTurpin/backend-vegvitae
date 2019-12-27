package com.vegvitae.vegvitae.model;

import java.util.Date;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
public class RecipeComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String text;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "userCreator")
  private User author;

  private Date creationDate;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "votes", joinColumns = @JoinColumn(name = "commentId"), inverseJoinColumns = @JoinColumn(name = "userId"))
  private Set<User> votesUsers;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Set<User> getVotesUsers() {
    return votesUsers;
  }

  public void setVotesUsers(Set<User> votesUsers) {
    this.votesUsers = votesUsers;
  }
}
