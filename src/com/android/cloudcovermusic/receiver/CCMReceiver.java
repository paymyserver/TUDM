package com.android.cloudcovermusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.cloudcovermusic.service.CCMCachingService;

public class CCMReceiver extends BroadcastReceiver {
    private static final String TAG = CCMReceiver.class.getSimpleName();

    public CCMReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        if (intent != null
                && intent.getAction().equalsIgnoreCase(
                        CCMCachingService.ACTION_DOWNLOAD_TRACK_COMPLETED)) {
            Log.d(TAG, "onReceive() Track Downloaded....");
            context.startService(new Intent(
                    CCMCachingService.ACTION_DOWNLOAD_TRACK));
        }
    }
}
