package com.android.tudm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.tudm.service.TUDMCachingService;

public class TUDMReceiver extends BroadcastReceiver {
    private static final String TAG = TUDMReceiver.class.getSimpleName();

    public TUDMReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        if (intent != null
                && intent.getAction().equalsIgnoreCase(
                        TUDMCachingService.ACTION_DOWNLOAD_TRACK_COMPLETED)) {
            Log.d(TAG, "onReceive() Track Downloaded....");
            context.startService(new Intent(
                    TUDMCachingService.ACTION_DOWNLOAD_TRACK));
        }
    }
}
