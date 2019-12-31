package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.RatingProduct;
import com.vegvitae.vegvitae.model.UserProductId;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingProductRepository extends JpaRepository<RatingProduct, UserProductId> {

  List<RatingProduct> findByProduct(Product p);

  List<RatingProduct> findByUser(User user);

  RatingProduct findByUserAndProduct(User u, Product p);
}
