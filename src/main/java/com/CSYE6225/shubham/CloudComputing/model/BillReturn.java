package com.CSYE6225.shubham.CloudComputing.model;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.json.JSONObject;

public class BillReturn {

	private UUID id;
	private String created_ts;
	private String updated_ts;
	private UUID owner;
	private String vendor;
	private String bill_date;
	private String due_date;
	private Double amount_due;
	private PaymentStatus payment_status;
	private String[] categories;
	private File attachment;

	public BillReturn(UUID id,String created_ts, String updated_ts, UUID owner_id, String vendor, String bill_date, String due_date,
			Double amount_due, PaymentStatus payment_status, String[] categories, File attachment) {
//		super();
		this.id = id;
		this.created_ts = created_ts;
		this.updated_ts = updated_ts;
		this.owner = owner_id;
		this.vendor = vendor;
		this.bill_date = bill_date;
		this.due_date = due_date;
		this.amount_due = amount_due;
		this.payment_status = payment_status;
		this.categories = categories;
		this.attachment = attachment;
	}
	
	public BillReturn() {}

	
	public File getAttachment() {
		return attachment;
	}

	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}

	public String getCreated_ts() {
		return created_ts;
	}

	public void setCreated_ts(String created_ts) {
		this.created_ts = created_ts;
	}

	public String getUpdated_ts() {
		return updated_ts;
	}

	public void setUpdated_ts(String updated_ts) {
		this.updated_ts = updated_ts;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner_id) {
		this.owner = owner_id;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getBill_date() {
		return bill_date;
	}

	public void setBill_date(String bill_date) {
		this.bill_date = bill_date;
	}

	public String getDue_date() {
		return due_date;
	}

	public void setDue_date(String due_date) {
		this.due_date = due_date;
	}

	public Double getAmount_due() {
		return amount_due;
	}

	public void setAmount_due(Double amount_due) {
		this.amount_due = amount_due;
	}

	public PaymentStatus getPayment_status() {
		return payment_status;
	}

	public void setPayment_status(PaymentStatus payment_status) {
		this.payment_status = payment_status;
	}

	public String[] getCategories() {
		return categories;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	public UUID getId() {
		return id;
	}
	
	
	
	
}
