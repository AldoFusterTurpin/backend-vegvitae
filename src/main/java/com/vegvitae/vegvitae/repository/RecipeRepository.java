package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vegvitae.vegvitae.model.Product;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

  @Transactional
  List<Recipe> findRecipeByTitleContaining(String title);

  List<Recipe> findByUsedProducts(Product product);
}
