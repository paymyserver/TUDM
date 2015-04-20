package com.android.cloudcovermusic.service;

public interface CCMListener {
    void onTrackStarted();
    void onTrackCompleted();
    void onTrackPaused();
    void onTrackResumed();
    void onTrackStopped();
    void onPlayerError();
    // Will be called when user stops the application from notification bar.
    void onPlayerStopped();
}
