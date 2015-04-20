package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ActivatePlayerResponse {
    public final static int PLAYER_STATUS_INACTIVE = 0;
    public final static int PLAYER_STATUS_ACTIVE = 1;
    @Expose
    @SerializedName("active_player")
    private int mActiveStatus;

    public int getActiveStatus() {
        return mActiveStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.mActiveStatus = activeStatus;
    }
    
}
