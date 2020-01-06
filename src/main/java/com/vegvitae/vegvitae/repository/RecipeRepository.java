package com.vegvitae.vegvitae.repository;


import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.Recipe;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

  Set<Recipe> findRecipeByTitleContaining(String title);

  Set<Recipe> findRecipeByUsedProducts(Product p);
}
