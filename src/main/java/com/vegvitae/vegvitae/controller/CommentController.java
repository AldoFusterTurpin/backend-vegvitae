package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Comment;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.repository.CommentRepository;
import com.vegvitae.vegvitae.repository.ProductRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api" + CommentController.PRODUCT_PATH + "/{id}" + CommentController.COMMENT_PATH)
public class CommentController {

  static final String PRODUCT_PATH = "/products";

  static final String COMMENT_PATH = "/comments";

  @Autowired
  CommentRepository commentRepository;

  @Autowired
  ProductRepository productRepository;

  @PostMapping
  Comment createComment(@PathVariable Long id, @Valid @RequestBody Comment comment) {
    comment.setCreationDate(new Date());
    Optional<Product> productOpt = productRepository.findById(id);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      List<Comment> comments = product.getComments();
      Comment newComment = commentRepository.save(comment);
      comments.add(newComment);
      return newComment;
    }
    else {
      throw new GenericException(HttpStatus.BAD_REQUEST, ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

  @GetMapping
  List<Comment> getComments(@PathVariable Long id) {
    Optional<Product> productOpt = productRepository.findById(id);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      return product.getComments();
    }
    else {
      throw new GenericException(HttpStatus.BAD_REQUEST, ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

}
