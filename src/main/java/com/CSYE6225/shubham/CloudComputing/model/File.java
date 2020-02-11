package com.CSYE6225.shubham.CloudComputing.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Files")
public class File {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;
	
	@Column(name = "upload_date")
	private String upload_date;
	
	@Column(name = "file_name")
	private String file_name;
	
	@Column(name = "url")
	private String url;
	
	
	
	public File(String upload_date, String file_name, String url) {
		this.upload_date = LocalDate.now().toString();
		this.file_name = file_name;
		this.url = url;
		
	}



	public File() {}



	public String getUpload_date() {
		return upload_date;
	}



	public void setUpload_date(String upload_date) {
		this.upload_date = upload_date;
	}



	public String getFile_name() {
		return file_name;
	}



	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}



	public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public UUID getId() {
		return id;
	}
	

	
	
}
