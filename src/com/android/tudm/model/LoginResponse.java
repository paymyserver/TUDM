package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @Expose
    @SerializedName("meta")
    private MetaData mMeta;
    @Expose
    @SerializedName("token")
    private String mToken;
    @Expose
    @SerializedName("userid")   
    private int mUserId;
    @Expose
    @SerializedName("custid")
    private int mCustId;

    public MetaData getMeta() {
        return mMeta;
    }

    public void setMeta(MetaData meta) {
        this.mMeta = meta;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public int getCustId() {
        return mCustId;
    }

    public void setCustId(int custId) {
        this.mCustId = custId;
    }
}
