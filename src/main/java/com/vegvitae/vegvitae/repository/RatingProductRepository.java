package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.RatingProduct;
import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.model.UserProductId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RatingProductRepository extends JpaRepository<RatingProduct, UserProductId> {

  List<RatingProduct> findByUser(User u);
}
