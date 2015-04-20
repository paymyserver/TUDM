package com.android.tudm.model;

import java.io.Serializable;

public class Track implements Serializable {
    private String mTitle;
    private SongUri mSongUri;
    private MessageUri mMessageUri;
    private Album mAlbum;
    private Artist mArtist;
    private boolean mIsMessage;
    private String mSongPath;

    public String getSongPath() {
        return mSongPath;
    }

    public void setSongPath(String songPath) {
        this.mSongPath = songPath;
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public SongUri getSongUri() {
        return mSongUri;
    }

    public void setSongUri(SongUri songUri) {
        this.mSongUri = songUri;
    }

    public MessageUri getMessageUri() {
        return mMessageUri;
    }

    public void setMessageUri(MessageUri messageUri) {
        this.mMessageUri = messageUri;
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

    public boolean isMessage() {
        return mIsMessage;
    }

    public void setMessage(boolean isMessage) {
        this.mIsMessage = isMessage;
    }

    @Override
    public String toString() {
        String str = mTitle;
        if (mAlbum != null) {
            str = str + mAlbum.getName();
        }
        return str;
    }
}
