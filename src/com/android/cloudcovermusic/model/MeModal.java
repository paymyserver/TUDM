package com.android.cloudcovermusic.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MeModal {
	@Expose
	@SerializedName("music_type")
	String musicType;
	
	@Expose
	@SerializedName("message_preset")
	String messagePreset;
	
	@Expose
	@SerializedName("music_id")
	String musicId;
	
	@Expose
	@SerializedName("active_player")
	String activePlayer;

	@Expose
	@SerializedName("device_id")
	String deviceId;
	
//	@Expose
//	@SerializedName("user")
//	UserModel userInfo;

	
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

	public String getActivePlayer() {
		return activePlayer;
	}

	public void setActivePlayer(String activePlayer) {
		this.activePlayer = activePlayer;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

//	public UserModel getUserInfo() {
//		return userInfo;
//	}

//	public void setUserInfo(UserModel userInfo) {
//		this.userInfo = userInfo;
//	}

	public void setMusicType(String musicType) {
		this.musicType = musicType;
	}

	public String getMusicType() {
		return musicType;
	}

	public void setmMusicType(String musicType) {
		this.musicType = musicType;
	}

}
