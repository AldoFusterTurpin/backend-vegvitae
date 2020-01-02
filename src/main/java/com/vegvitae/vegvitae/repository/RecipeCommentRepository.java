package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.RecipeComment;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface RecipeCommentRepository extends JpaRepository<RecipeComment, Long> {

}
