package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.RatingProduct;
import com.vegvitae.vegvitae.model.UserProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingProductRepository extends JpaRepository<RatingProduct, UserProductId> {
}
