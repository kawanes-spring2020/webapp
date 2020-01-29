package com.CSYE6225.shubham.CloudComputing.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CSYE6225.shubham.CloudComputing.model.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
	List<Bill> findByOwner(UUID owner);
	Bill findById(UUID id);
}
