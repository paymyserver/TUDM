package com.android.cloudcovermusic.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegistrationResponse {
    @Expose
    @SerializedName("meta")
    private MetaData mMeta;
    @Expose
    @SerializedName("device")
    private Device mDevice;

    public MetaData getMeta() {
        return mMeta;
    }

    public void setMeta(MetaData meta) {
        this.mMeta = meta;
    }

    public Device getDevice() {
        return mDevice;
    }

    public void setDevice(Device device) {
        this.mDevice = device;
    }
}
