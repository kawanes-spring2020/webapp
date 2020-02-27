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
import org.hibernate.annotations.Type;

@Entity
@Table(name = "Files")
public class File {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;
	
	@Column(name = "upload_date")
	private String upload_date;
	
	@Column(name = "file_name")
	private String file_name;
	
	@Column(name = "url")
	private String url;
	
	@Column(name = "md5")
	private String md5;
	
	@Column(name = "size")
	private int size;
	
	public File(String upload_date, String file_name, String url, String md5, int size) {
		this.upload_date = LocalDate.now().toString();
		this.file_name = file_name;
		this.url = url;
		this.md5 = md5;
		this.size = size;
		
	}



	public File() {}

	

	public String getMd5() {
		return md5;
	}



	public void setMd5(String md5) {
		this.md5 = md5;
	}



	public int getSize() {
		return size;
	}



	public void setSize(int size) {
		this.size = size;
	}



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
