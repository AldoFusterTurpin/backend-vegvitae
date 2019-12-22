package com.vegvitae.vegvitae.exceptions;

public enum ExceptionMessages {

  INVALID_PASSWORD(
      "The password must have a minimum of 7 characters and should showcase upper case, lower case and number"),
  NULL_USERNAME("Username cannot be null nor empty"),
  NULL_PASSWORD("Password cannot be null nor empty"),
  EMAIL_IN_USE("This email addres is already in use"),
  USERNAME_IN_USE("This username is already in use"),
  INVALID_DESCRIPTION("The personal description must be shorter than 160 characters"),
  INVALID_SOCIAL_MEDIA_LINKS_LENGTH("Social media links limit exceeded. Maximum is 4"),
  INVALID_CREDENTIALS("Credentials are not correct. Try again"),
  PRODUCT_EXISTS("There is already a product with this barcode:"),
  USER_NOT_FOUND("There is no username with that id: "),
  RECIPE_NOT_FOUND("There is no Recipe with that id: "),
  PRODUCT_NOT_FOUND("There is no product with that id: "),
  PRODUCT_INVALID_BARCODE("Product with the specified barcode doesn't exist"),
  COMMENT_INVALID_ID("Comment with the specified id doesn't exist");

  private String message;

  ExceptionMessages(String message) {
    this.message = message;
  }

  public String getErrorMessage() {
    return message;
  }
}
