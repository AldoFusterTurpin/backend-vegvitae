package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
