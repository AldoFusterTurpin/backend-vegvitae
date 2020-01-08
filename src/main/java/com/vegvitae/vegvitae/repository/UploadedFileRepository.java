package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long>
{

}
