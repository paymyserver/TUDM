package com.android.cloudcovermusic.model;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongUri implements Serializable {
    @Expose
    @SerializedName("aac")
    private String mAac;
    @Expose
    @SerializedName("ogg")
    private String mOgg;
    @Expose
    @SerializedName("m4a")
    private String mM4a;
    @Expose
    @SerializedName("mp4")
    private String mMp4;

    public String getAac() {
        return mAac;
    }

    public void setAac(String aac) {
        this.mAac = aac;
    }

    public String getOgg() {
        return mOgg;
    }

    public void setOgg(String ogg) {
        this.mOgg = ogg;
    }

    public String getM4a() {
        return mM4a;
    }

    public void setM4a(String m4a) {
        this.mM4a = m4a;
    }

    public String getMp4() {
        return mMp4;
    }

    public void setMp4(String mp4) {
        this.mMp4 = mp4;
    }
}
