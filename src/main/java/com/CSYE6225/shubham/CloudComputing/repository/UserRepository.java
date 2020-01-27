package com.CSYE6225.shubham.CloudComputing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CSYE6225.shubham.CloudComputing.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Boolean existsByEmail(String email);
	User findByEmail(String email);
}
