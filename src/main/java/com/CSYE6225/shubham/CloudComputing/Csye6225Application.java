package com.CSYE6225.shubham.CloudComputing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
public class Csye6225Application {

	public static void main(String[] args) {
		SpringApplication.run(Csye6225Application.class, args);
	}

}
