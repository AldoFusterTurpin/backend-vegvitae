package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.ProductAdditionalTypeEnum;
import com.vegvitae.vegvitae.model.ProductBaseTypeEnum;
import com.vegvitae.vegvitae.model.SupermarketEnum;
import java.sql.Date;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ProductRepository extends JpaRepository<Product, Long> {

  Set<Product> findByShopContaining(String shop);

  Set<Product> findByNameContaining(String name);

  List<Product> findByBaseTypeIn(List<ProductBaseTypeEnum> baseTypes);

  List<Product> findByAdditionalTypesIn(List<ProductAdditionalTypeEnum> additionalTypes);

  List<Product> findBySupermarketsAvailable(List<SupermarketEnum> supermarkets);

  List<Product> findByCreationDate(Date today);

  boolean existsByBarcode(Long id);

  void deleteByBarcode(Long id);
}
