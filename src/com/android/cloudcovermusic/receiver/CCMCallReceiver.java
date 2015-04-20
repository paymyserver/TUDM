package com.android.cloudcovermusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.cloudcovermusic.service.CCMService;
import com.cloudcovermusic.ccm.CCMApplication;

/**
 * This class handle all the interruption for incoming calls.
 * 
 * @author rushikesh
 * 
 */

public class CCMCallReceiver extends BroadcastReceiver {
    TelephonyManager telManager;
    Context context;
    /**
     * Instance of {@link CCMService}
     */
    CCMService mService;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        mService = CCMService.getInstance();
    }

    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: {
                    
                    if (mService != null && mService.isPlaying()) {
                        CCMApplication.sIsPhoneRinging = true;
                        mService.pauseMusic();
                    }
                    break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK: {

                    break;
                }
                case TelephonyManager.CALL_STATE_IDLE: {
                    if (mService != null && mService.isPaused()
                            && CCMApplication.sIsPhoneRinging) {
                        CCMApplication.sIsPhoneRinging = false;
                        mService.startMusic();
                    }
                    break;
                }
                default: {
                }
                }
            } catch (Exception ex) {

            }
        }
    };
}
