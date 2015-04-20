package com.android.tudm.service;

import android.media.MediaPlayer;

public interface TUDMListener {
    void onCompletion(MediaPlayer mp);

    void onTrackStarted();
    void onTrackCompleted();
    void onTrackPaused();
    void onTrackResumed();
    void onTrackStopped();
    void onPlayerError();
    // Will be called when user stops the application from notification bar.
    void onPlayerStopped();
}
