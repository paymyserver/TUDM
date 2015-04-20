package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongRemovalModal {
    @Expose
    @SerializedName("name")
    String mName;
    
    @Expose
    @SerializedName("mediaid")
    String mediaId;
   
    @Expose
    @SerializedName("album")
    private Album mAlbum;
    
    @Expose
    @SerializedName("artist")
    private Artist mArtist;
    
    @Expose
    @SerializedName("uuid")
    private String uuid;
    
    @Expose
    @SerializedName("uri")
    private SongUri mSonguri;
    
    private boolean isItemClicked;
    private boolean isPlayedClicked;
    
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Album getAlbum() {
        return mAlbum;
    }

    public void setAlbum(Album album) {
        this.mAlbum = album;
    }
    
    public Artist getArtist() {
        return mArtist;
    }

    public void setArtist(Artist artist) {
        this.mArtist = artist;
    }
    
    public SongUri getSonguri() {
        return mSonguri;
    }

    public void setSonguri(SongUri songuri) {
        this.mSonguri = songuri;
    }
    
    public String getName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
    
    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public boolean isItemClicked() {
        return isItemClicked;
    }

    public void setItemClicked(boolean isItemClicked) {
        this.isItemClicked = isItemClicked;
    }
    
    public boolean isPlayedClicked() {
        return isPlayedClicked;
    }

    public void isPlayedClicked(boolean isPlayedClicked) {
        this.isPlayedClicked = isPlayedClicked;
    }
}
