package com.vegvitae.vegvitae.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

@Entity
public class Article {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank(message = "Article title cannot be null")
  private String title;

  @NotBlank(message = "Article text cannot be null")
  private String text;

  @URL
  @NotBlank(message = "Article link cannot be null and has to be a correct URL")
  private String articleLink;

  private Date dateUpload;

  @ManyToOne
  private User uploader;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getArticleLink() {
    return articleLink;
  }

  public void setArticleLink(String articleLink) {
    this.articleLink = articleLink;
  }

  public User getUploader() {
    return uploader;
  }

  public void setUploader(User uploader) {
    this.uploader = uploader;
  }

  public Date getDateUpload() {
    return dateUpload;
  }

  public void setDateUpload(Date dateUpload) {
    this.dateUpload = dateUpload;
  }
}
