package com.vegvitae.vegvitae.controller;

import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.exceptions.GenericException;
import com.vegvitae.vegvitae.model.Article;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.ArticleRepository;
import com.vegvitae.vegvitae.repository.UserRepository;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import org.checkerframework.common.util.report.qual.ReportUnqualified;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.ClassGeneratingEntityInstantiator;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api" + ArticleController.ARTICLE_PATH)
public class ArticleController {

  static final String ARTICLE_PATH = "/articles";

  @Autowired
  ArticleRepository articleRepository;

  @Autowired
  UserRepository userRepository;

  @Transactional
  @PostMapping
  Article createArticle(@Valid @RequestBody Article article, @RequestHeader("token") String token) {
    article.setUploader(userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage())));
    article.setDateUpload(new Date());
    return articleRepository.save(article);
  }

  @Transactional
  @GetMapping
  List<Article> getAllArticles() {
    return articleRepository.findAll();
  }

  @Transactional
  @PutMapping("/{id}")
  Article editArticle(@Valid @RequestBody Article newArticle, @PathVariable Long id,
      @RequestHeader("token") String token) {
    Article article = articleRepository.findById(id).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_ARTICLE_ID.getErrorMessage()));
    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
    if (!article.getUploader().equals(user)) {
      throw new GenericException(HttpStatus.FORBIDDEN,
          ExceptionMessages.INVALID_TOKEN.getErrorMessage());
    }
    article.setText(newArticle.getText());
    article.setTitle(newArticle.getTitle());
    article.setArticleLink(newArticle.getArticleLink());
    return articleRepository.save(article);
  }

  @Transactional
  @DeleteMapping("/{id}")
  void deleteArticle(@PathVariable Long id, @RequestHeader("token") String token) {
    Article article = articleRepository.findById(id).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_ARTICLE_ID.getErrorMessage()));
    User user = userRepository.findByToken(token).orElseThrow(
        () -> new GenericException(HttpStatus.BAD_REQUEST,
            ExceptionMessages.INVALID_TOKEN.getErrorMessage()));
    if (!article.getUploader().equals(user)) {
      throw new GenericException(HttpStatus.FORBIDDEN,
          ExceptionMessages.INVALID_TOKEN.getErrorMessage());
    }
    articleRepository.deleteById(id);
  }
}
