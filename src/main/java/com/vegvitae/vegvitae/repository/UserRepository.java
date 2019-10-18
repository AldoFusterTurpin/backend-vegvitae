package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

}
