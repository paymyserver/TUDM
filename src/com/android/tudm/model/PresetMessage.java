package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class PresetMessage {

	@Expose
    @SerializedName("userid")
	String userID;
	@Expose
    @SerializedName("name")
	String name;
	@Expose
    @SerializedName("presetid")
	String presetId;
	@Expose
    @SerializedName("custid")
	String custId;
	@Expose
    @SerializedName("isAcive")
	boolean active;
	@Expose
    @SerializedName("isDeleted")
	boolean deleted;
	
	@Expose
    @SerializedName("messages")
	String messages;
	
	public String getMessages() {
		return messages;
	}
	public void setMessages(String messages) {
		this.messages = messages;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPreserId() {
		return presetId;
	}
	public void setPreserId(String preserId) {
		this.presetId = preserId;
	}
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
}
