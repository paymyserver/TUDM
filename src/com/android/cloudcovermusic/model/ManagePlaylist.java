package com.android.cloudcovermusic.model;

public class ManagePlaylist {
	
	int id;
	int playlistid;
	int daypartingid;
	int mixid;
	String name;
	String subname;
	String description;
	boolean isHeader;
	String musicType;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubname() {
		return subname;
	}
	public void setSubname(String subname) {
		this.subname = subname;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isHeader() {
		return isHeader;
	}
	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}
	public int getPlaylistid() {
		return playlistid;
	}
	public void setPlaylistid(int playlistid) {
		this.playlistid = playlistid;
	}
	public int getDaypartingid() {
		return daypartingid;
	}
	public void setDaypartingid(int daypartingid) {
		this.daypartingid = daypartingid;
	}
	public int getMixid() {
		return mixid;
	}
	public void setMixid(int mixid) {
		this.mixid = mixid;
	}
	public String getMusicType() {
		return musicType;
	}
	public void setMusicType(String musicType) {
		this.musicType = musicType;
	}

}
