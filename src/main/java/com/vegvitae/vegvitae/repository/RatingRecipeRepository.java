package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.RatingRecipe;
import com.vegvitae.vegvitae.model.Recipe;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.model.UserRecipeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRecipeRepository extends JpaRepository<RatingRecipe, Long> {

  boolean existsByUserAndRecipe(User user, Recipe recipe);

  RatingRecipe findByUserAndRecipe(User user, Recipe recipe);
}
