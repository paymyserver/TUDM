package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Song {
    @Expose
    @SerializedName("name")
    private String mName;
    
    @Expose
    @SerializedName("mediaid")
    private int mMediaId;
    
    @Expose
    @SerializedName("uri")
    private SongUri mSonguri;

    public int getMediaId() {
        return mMediaId;
    }

    public void setMediaId(int mediaId) {
        this.mMediaId = mediaId;
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

    public void setName(String name) {
        this.mName = name;
    }
}
