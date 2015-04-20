package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MetaData {
    @Expose
    @SerializedName("code")
    private int mCode;

    @Expose
    @SerializedName("page")
    private int mPage;

    @Expose
    @SerializedName("limit")
    private int mLimit;

    @Expose
    @SerializedName("error")
    private String mError;

    @Expose
    @SerializedName("responsetime")
    private int mResponseTime;

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        this.mPage = page;
    }

    public int getLimit() {
        return mLimit;
    }

    public void setLimit(int limit) {
        this.mLimit = limit;
    }

    public String getError() {
        return mError;
    }

    public void setError(String error) {
        this.mError = error;
    }

    public int getResponseTime() {
        return mResponseTime;
    }

    public void setResponseTime(int responseTime) {
        this.mResponseTime = responseTime;
    }
}
