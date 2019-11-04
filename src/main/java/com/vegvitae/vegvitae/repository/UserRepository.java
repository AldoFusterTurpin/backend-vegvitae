package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.User;
import org.hibernate.annotations.NamedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  User findByUsername(String username);

  User findByEmail(String email);

}
