package com.CSYE6225.shubham.CloudComputing.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CSYE6225.shubham.CloudComputing.model.Bill;
import com.CSYE6225.shubham.CloudComputing.model.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
	File findById(UUID id);
}
