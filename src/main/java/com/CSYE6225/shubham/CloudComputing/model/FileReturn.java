package com.CSYE6225.shubham.CloudComputing.model;


import java.util.UUID;


public class FileReturn {

	private UUID id;
	private String upload_date;
	private String file_name;
	private String url;
	private String md5;
	private int size;
	
	
	
	public FileReturn(UUID id, String upload_date, String file_name, String url, String md5, int size) {
		this.id = id;
		this.upload_date = upload_date;
		this.file_name = file_name;
		this.url = url;
		this.md5 = md5;
		this.size = size;
	}



	public FileReturn() {}

	

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
