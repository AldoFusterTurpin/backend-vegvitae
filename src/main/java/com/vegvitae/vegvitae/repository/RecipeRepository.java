package com.vegvitae.vegvitae.repository;


import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.Recipe;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

  Set<Recipe> findRecipeByTitleContaining(String title);

  Set<Recipe> findRecipeByUsedProducts(Product p);
}
