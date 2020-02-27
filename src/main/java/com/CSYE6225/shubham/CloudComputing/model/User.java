package com.CSYE6225.shubham.CloudComputing.model;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "Users")
public class User {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@Column(name = "email_address")
	@Email
	private String email;

	@Column(name = "password")
	private String password;


	@Column(name = "first_name")
	private String first_name;

	@Column(name = "last_name")
	private String last_name;
	
	@Column(name = "account_created")
	private String account_created;
	
	@Column(name = "account_updated")
	private String account_updated;

	public User(String email_address, String password, String first_name, String last_name, String account_created,
			String account_updated) {
//		super();
		this.email = email_address;
		this.password = password;
		this.first_name = first_name;
		this.last_name = last_name;
		this.account_created = LocalDateTime.now().toString();
		this.account_updated = LocalDateTime.now().toString();
	}
	

	public User() {
//		super();
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email_address) {
		this.email = email_address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getAccount_created() {
		return account_created;
	}

	public void setAccount_created(String account_created) {
		this.account_created = account_created;
	}

	public String getAccount_updated() {
		return account_updated;
	}

	public void setAccount_updated(String account_updated) {
		this.account_updated = account_updated;
	}

	public UUID getId() {
		return id;
	}

	
	
}

