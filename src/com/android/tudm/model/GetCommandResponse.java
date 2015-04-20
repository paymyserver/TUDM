package com.android.tudm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetCommandResponse {
    public static final String COMMAND_STATUS_ACTIVE = "active";
    public static final String COMMAND_STATUS_INACTIVE = "inactive";
    public static final String COMMAND_NEXT_TRACK = "nextTrack";
    public static final String COMMAND_PAUSE_PLAYER = "pausePlayer";
    public static final String COMMAND_PLAY_PLAYER = "playPlayer";
    public static final String COMMAND_RESTART_PLAYER = "restartApp";
    public static final String COMMAND_REBOOT_PLAYER = "rebootPlayer";

    @Expose
    @SerializedName("meta")
    private MetaData mMeta;
    @Expose
    @SerializedName("status")
    private String mStatus;
    @Expose
    @SerializedName("command")
    private String mCommand;

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public String getCommand() {
        return mCommand;
    }

    public void setCommand(String command) {
        this.mCommand = command;
    }

    public MetaData getMeta() {
        return mMeta;
    }

    public void setMeta(MetaData meta) {
        this.mMeta = meta;
    }

}
