package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.ProductAdditionalTypeEnum;
import com.vegvitae.vegvitae.model.ProductBaseTypeEnum;
import com.vegvitae.vegvitae.model.SupermarketEnum;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByNameIn(List<String> name);

  Set<Product> findByNameContaining(String name);

  List<Product> findByBaseTypeIn(List<ProductBaseTypeEnum> baseTypes);

  List<Product> findByAdditionalTypesIn(List<ProductAdditionalTypeEnum> additionalTypes);

  List<Product> findBySupermarketsAvailable(List<SupermarketEnum> supermarkets);

}
