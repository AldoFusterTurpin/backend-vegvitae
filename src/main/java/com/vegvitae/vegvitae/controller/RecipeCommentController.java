package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Recipe;
import com.vegvitae.vegvitae.model.RecipeComment;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.RecipeCommentRepository;
import com.vegvitae.vegvitae.repository.RecipeRepository;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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
    "/api" + RecipeCommentController.RECIPE_PATH + "/{recipeId}"
        + RecipeCommentController.COMMENT_PATH)
public class RecipeCommentController {

  static final String RECIPE_PATH = "/recipes";

  static final String COMMENT_PATH = "/comments";

  @Autowired
  UserRepository userRepository;

  @Autowired
  RecipeRepository recipeRepository;

  @Autowired
  RecipeCommentRepository recipeCommentRepository;

  @Transactional
  @PostMapping
  RecipeComment createRecipeComment(@PathVariable Long recipeId,
      @Valid @RequestBody RecipeComment comment, @RequestHeader("token") String token) {
    comment.setCreationDate(new Date());
    comment.setVotesUsers(new HashSet<>());
    User author = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
    comment.setAuthor(author);
    Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
    if (recipeOpt.isPresent()) {
      Recipe recipe = recipeOpt.get();
      List<RecipeComment> comments = recipe.getComments();
      RecipeComment newRecipeComment = recipeCommentRepository.save(comment);
      comments.add(newRecipeComment);
      recipe.setComments(comments);
      recipeRepository.save(recipe);

      // Commenting a recipe earns the user a certain number of points
      author.addPoints(10);
      userRepository.save(author);

      return newRecipeComment;
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage());
    }
  }

  @Transactional
  @GetMapping
  List<RecipeComment> getRecipeComments(@PathVariable Long recipeId) {
    Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
    if (recipeOpt.isPresent()) {
      Recipe recipe = recipeOpt.get();
      return recipe.getComments();
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage());
    }
  }

  @Transactional
  @PutMapping("/{id}")
  RecipeComment editRecipeComment(@PathVariable Long recipeId, @PathVariable Long id,
      @Valid @RequestBody RecipeComment commentNew, @RequestHeader("token") String token) {
    Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
    if (recipeOpt.isPresent()) {
      Recipe recipe = recipeOpt.get();
      Optional<RecipeComment> commentOpt = recipeCommentRepository.findById(id);
      if (commentOpt.isPresent()) {
        List<RecipeComment> comments = recipe.getComments();
        RecipeComment comment = commentOpt.get();
        User user = userRepository.findByToken(token).orElseThrow(
            () -> new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
        if(!user.equals(comment.getAuthor())) {
          throw new GenericException(HttpStatus.BAD_REQUEST, ExceptionMessages.INVALID_COMMENT_AUTHOR.getErrorMessage());
        }
        comments.remove(comment);
        comment.setText(commentNew.getText());
        recipeCommentRepository.save(comment);
        comments.add(comment);
        recipe.setComments(comments);
        recipeRepository.save(recipe);
        return comment;
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            "Cannot found a comment with the specified id");
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage());
    }
  }

  @Transactional
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  void deleteRecipeComment(@PathVariable Long recipeId, @PathVariable Long id, @RequestHeader("token") String token) {
    Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
    if (recipeOpt.isPresent()) {
      Recipe recipe = recipeOpt.get();
      Optional<RecipeComment> commentOpt = recipeCommentRepository.findById(id);
      if (commentOpt.isPresent()) {
        List<RecipeComment> comments = recipe.getComments();
        RecipeComment comment = commentOpt.get();
        User user = userRepository.findByToken(token).orElseThrow(
            () -> new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
        if(!user.equals(comment.getAuthor())) {
          throw new GenericException(HttpStatus.BAD_REQUEST, ExceptionMessages.INVALID_COMMENT_AUTHOR.getErrorMessage());
        }
        comments.remove(comment);
        recipeCommentRepository.delete(comment);
        recipe.setComments(comments);
        recipeRepository.save(recipe);
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            "Cannot found a comment with the specified id");
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage());
    }
  }

  @Transactional
  @PostMapping("/{id}/vote")
  RecipeComment voteRecipeComment(@PathVariable Long recipeId, @PathVariable Long id,
      @RequestHeader("token") String token) {
    Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
    if (recipeOpt.isPresent()) {
      Recipe recipe = recipeOpt.get();
      Optional<RecipeComment> commentOpt = recipeCommentRepository.findById(id);
      if (commentOpt.isPresent()) {
        List<RecipeComment> comments = recipe.getComments();
        RecipeComment comment = commentOpt.get();
        comments.remove(comment);
        Set<User> votes = comment.getVotesUsers();
        User user = userRepository.findByToken(token).orElseThrow(
            () -> new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
        if (votes.contains(user)) {
          throw new GenericException(HttpStatus.BAD_REQUEST,
              "Comment already voted");
        }
        votes.add(user);
        comment.setVotesUsers(votes);
        recipeCommentRepository.save(comment);
        comments.add(comment);
        recipe.setComments(comments);
        recipeRepository.save(recipe);
        return comment;
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            "Invalid comment ID");
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage());
    }
  }

  @Transactional
  @DeleteMapping("/{id}/vote")
  RecipeComment unvoteRecipeComment(@PathVariable Long recipeId, @PathVariable Long id,
      @RequestHeader("token") String token) {
    Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
    if (recipeOpt.isPresent()) {
      Recipe recipe = recipeOpt.get();
      Optional<RecipeComment> commentOpt = recipeCommentRepository.findById(id);
      if (commentOpt.isPresent()) {
        List<RecipeComment> comments = recipe.getComments();
        RecipeComment comment = commentOpt.get();
        comments.remove(comment);
        Set<User> votes = comment.getVotesUsers();
        //Fixed user until we get authentication
        User user = userRepository.findByToken(token).orElseThrow(
            () -> new GenericException(HttpStatus.BAD_REQUEST,
                ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
        if (!votes.contains(user)) {
          throw new GenericException(HttpStatus.BAD_REQUEST,
              "Comment not voted");
        }
        votes.remove(user);
        comment.setVotesUsers(votes);
        recipeCommentRepository.save(comment);
        comments.add(comment);
        recipe.setComments(comments);
        recipeRepository.save(recipe);
        return comment;
      } else {
        throw new GenericException(HttpStatus.BAD_REQUEST,
            "Invalid comment ID");
      }
    } else {
      throw new GenericException(HttpStatus.BAD_REQUEST,
          ExceptionMessages.RECIPE_NOT_FOUND.getErrorMessage());
    }
  }


}
