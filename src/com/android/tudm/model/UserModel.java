package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserModel {
	
	@Expose
	@SerializedName("music_type")
	String userName;
	
	@Expose
	@SerializedName("message_preset")
	String messagePreset;
	
	@Expose
	@SerializedName("music_id")
	String musicId;
	
	@Expose
	@SerializedName("groupid")
	String groupId;
	
	@Expose
	@SerializedName("custid")
	String custId;

	@Expose
	@SerializedName("music_type")
	String musicType;
	
	public String getMusicType() {
		return musicType;
	}

	public void setMusicType(String musicType) {
		this.musicType = musicType;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMessagePreset() {
		return messagePreset;
	}

	public void setMessagePreset(String messagePreset) {
		this.messagePreset = messagePreset;
	}

	public String getMusicId() {
		return musicId;
	}

	public void setMusicId(String musicId) {
		this.musicId = musicId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	
	
}
