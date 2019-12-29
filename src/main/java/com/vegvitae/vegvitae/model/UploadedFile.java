package com.vegvitae.vegvitae.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;

@Entity
public class UploadedFile
{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  private String name;

  private String contentType;

  @JsonIgnore
  @Lob
  //@Type(type = "org.hibernate.type.ImageType")
  private byte[] file;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public byte[] getFile() {
    return file;
  }

  public void setFile(byte[] file) {
    this.file = file;
  }
}