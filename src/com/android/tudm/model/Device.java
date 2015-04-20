package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {
    @Expose
    @SerializedName("uuid")
    private String mUuid;

    @Expose
    @SerializedName("deviceid")
    private int mDeviceId;

    @Expose
    @SerializedName("custid")
    private int mCustId;

    @Expose
    @SerializedName("userid")
    private int mUserId;

    @Expose
    @SerializedName("name")
    private String mName;

    @Expose
    @SerializedName("description")
    private String mDescription;

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        this.mUuid = uuid;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(int deviceId) {
        this.mDeviceId = deviceId;
    }

    public int getCustId() {
        return mCustId;
    }

    public void setCustId(int custId) {
        this.mCustId = custId;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }
}
