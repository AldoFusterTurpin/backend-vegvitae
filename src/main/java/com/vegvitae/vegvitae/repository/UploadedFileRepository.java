package com.vegvitae.vegvitae.repository;

import com.vegvitae.vegvitae.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long>
{

}
