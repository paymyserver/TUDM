package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetTrackResponse {
    public static final String COMMAND_STATUS_INACTIVE = "InActivePlayer";
    public static final String TRACK_ERROR_NO_MUSIC = "nomusic";

    @Expose
    @SerializedName("meta")
    private MetaData mMeta;
    @Expose
    @SerializedName("song")
    private Song mSong;

    @Expose
    @SerializedName("message")
    private Message mMessage;

    @Expose
    @SerializedName("artist")
    private Artist mArtist;

    @Expose
    @SerializedName("album")
    private Album mAlbum;

    public Song getSong() {
        return mSong;
    }

    public void setSong(Song song) {
        this.mSong = song;
    }

    public Message getMessage() {
        return mMessage;
    }

    public void setMessage(Message message) {
        this.mMessage = message;
    }

    public Artist getArtist() {
        return mArtist;
    }

    public void setArtist(Artist artist) {
        this.mArtist = artist;
    }

    public Album getAlbum() {
        return mAlbum;
    }

    public void setAlbum(Album album) {
        this.mAlbum = album;
    }

    public MetaData getMeta() {
        return mMeta;
    }

    public void setMeta(MetaData meta) {
        this.mMeta = meta;
    }

}
