package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {
    @Expose
    @SerializedName("message_name")
    private String mName;
    
    @Expose
    @SerializedName("messageid")
    private int mMessageid;
    
    @Expose
    @SerializedName("uri")
    private MessageUri mMessageuri;

    public int getMessageId() {
        return mMessageid;
    }

    public void setMediaId(int messageid) {
        this.mMessageid = messageid;
    }

    public MessageUri getMessageUri() {
        return mMessageuri;
    }

    public void setMessageuri(MessageUri messageuri) {
        this.mMessageuri = messageuri;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }
}
