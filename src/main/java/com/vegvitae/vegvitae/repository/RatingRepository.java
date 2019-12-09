package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.Rating;
import com.vegvitae.vegvitae.model.UserProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UserProductId> {
}
