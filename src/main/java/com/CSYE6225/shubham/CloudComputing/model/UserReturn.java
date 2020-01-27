package com.CSYE6225.shubham.CloudComputing.model;

import java.util.UUID;

public class UserReturn {

	private UUID id;
	private String email;
	private String first_name;
	private String last_name;
	private String account_created;
	private String account_updated;

	public UserReturn(UUID id, String email_address, String first_name, String last_name, String account_created,
			String account_updated) {
//		super();
		this.id = id;
		this.email = email_address;
		this.first_name = first_name;
		this.last_name = last_name;
		this.account_created = account_created;
		this.account_updated = account_updated;
	}
	
	public UserReturn() {
		
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email_address) {
		this.email = email_address;
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

	public void setId(UUID id) {
		this.id = id;
	}

	
	
}

