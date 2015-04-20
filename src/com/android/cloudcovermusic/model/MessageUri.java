package com.android.cloudcovermusic.model;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageUri implements Serializable {
    @Expose
    @SerializedName("mp3")
    private String mMp3;

    public String getMp3() {
        return mMp3;
    }

    public void setMp3(String mp3) {
        this.mMp3 = mp3;
    }
}
