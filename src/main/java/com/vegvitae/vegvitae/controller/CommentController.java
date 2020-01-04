package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Comment;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.CommentRepository;
import com.vegvitae.vegvitae.repository.ProductRepository;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    "/api" + CommentController.PRODUCT_PATH + "/{barcode}" + CommentController.COMMENT_PATH)
public class CommentController {

  static final String PRODUCT_PATH = "/products";

  static final String COMMENT_PATH = "/comments";

  @Autowired
  CommentRepository commentRepository;

  @Autowired
  ProductRepository productRepository;

  @Autowired
  UserRepository userRepository;

  @PostMapping
  Comment createComment(@PathVariable Long barcode, @Valid @RequestBody Comment comment,
      @RequestHeader("token") String token) {
    comment.setCreationDate(new Date());
    comment.setVotesUsers(new HashSet<>());
    User author = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
    comment.setAuthor(author);
    Optional<Product> productOpt = productRepository.findById(barcode);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      List<Comment> comments = product.getComments();
      Comment newComment = commentRepository.save(comment);
      comments.add(newComment);
      product.setComments(comments);
      productRepository.save(product);

      // Commenting a product earns the user a certain number of points
      author.addPoints(10);
      userRepository.save(author);

      return newComment;
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

  @GetMapping
  List<Comment> getComments(@PathVariable Long barcode) {
    Optional<Product> productOpt = productRepository.findById(barcode);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      return product.getComments();
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

  @PutMapping("/{id}")
  Comment editComment(@PathVariable Long barcode, @PathVariable Long id,
      @Valid @RequestBody Comment commentNew, @RequestHeader("token") String token) {
    Optional<Product> productOpt = productRepository.findById(barcode);
    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      Optional<Comment> commentOpt = commentRepository.findById(id);
      if (commentOpt.isPresent()) {
        List<Comment> comments = product.getComments();
        Comment comment = commentOpt.get();
        if (!comment.getAuthor().equals(user)) {
          throw new GenericException(HttpStatus.BAD_REQUEST,
              ExceptionMessages.INVALID_COMMENT_AUTHOR.getErrorMessage());
        }
        comments.remove(comment);
        comment.setText(commentNew.getText());
        commentRepository.save(comment);
        comments.add(comment);
        product.setComments(comments);
        productRepository.save(product);
        return comment;
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.COMMENT_INVALID_ID.getErrorMessage());
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  void deleteComment(@PathVariable Long barcode, @PathVariable Long id,
      @RequestHeader("token") String token) {
    Optional<Product> productOpt = productRepository.findById(barcode);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      Optional<Comment> commentOpt = commentRepository.findById(id);
      User user = userRepository.findByToken(token).orElseThrow(
          () -> new GenericException(HttpStatus.BAD_REQUEST,
              ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
      if (commentOpt.isPresent()) {
        List<Comment> comments = product.getComments();
        Comment comment = commentOpt.get();
        if (!comment.getAuthor().equals(user)) {
          throw new GenericException(HttpStatus.BAD_REQUEST,
              ExceptionMessages.INVALID_COMMENT_AUTHOR.getErrorMessage());
        }
        comments.remove(comment);
        commentRepository.delete(comment);
        product.setComments(comments);
        productRepository.save(product);
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.COMMENT_INVALID_ID.getErrorMessage());
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

  @PostMapping("/{id}/vote")
  Comment voteComment(@PathVariable Long barcode, @PathVariable Long id,
      @RequestHeader("token") String token) {
    Optional<Product> productOpt = productRepository.findById(barcode);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      Optional<Comment> commentOpt = commentRepository.findById(id);
      if (commentOpt.isPresent()) {
        List<Comment> comments = product.getComments();
        Comment comment = commentOpt.get();
        comments.remove(comment);
        Set<User> votes = comment.getVotesUsers();
        User user = userRepository.findByToken(token).orElseThrow(
            () -> new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
        if (votes.contains(user)) {
          throw new GenericException(HttpStatus.BAD_REQUEST,
              ExceptionMessages.COMMENT_ALREADY_VOTED.getErrorMessage());
        }
        votes.add(user);
        comment.setVotesUsers(votes);
        commentRepository.save(comment);
        comments.add(comment);
        product.setComments(comments);
        productRepository.save(product);
        return comment;
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.COMMENT_INVALID_ID.getErrorMessage());
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

  @DeleteMapping("/{id}/vote")
  Comment unvoteComment(@PathVariable Long barcode, @PathVariable Long id,
      @RequestHeader("token") String token) {
    Optional<Product> productOpt = productRepository.findById(barcode);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      Optional<Comment> commentOpt = commentRepository.findById(id);
      if (commentOpt.isPresent()) {
        List<Comment> comments = product.getComments();
        Comment comment = commentOpt.get();
        comments.remove(comment);
        Set<User> votes = comment.getVotesUsers();
        //Fixed user until we get authentication
        User user = userRepository.findByToken(token).orElseThrow(
            () -> new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
        if (!votes.contains(user)) {
          throw new GenericException(HttpStatus.BAD_REQUEST,
              ExceptionMessages.COMMENT_NOT_VOTED.getErrorMessage());
        }
        votes.remove(user);
        comment.setVotesUsers(votes);
        commentRepository.save(comment);
        comments.add(comment);
        product.setComments(comments);
        productRepository.save(product);
        return comment;
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.COMMENT_INVALID_ID.getErrorMessage());
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.PRODUCT_INVALID_BARCODE.getErrorMessage());
    }
  }

}
