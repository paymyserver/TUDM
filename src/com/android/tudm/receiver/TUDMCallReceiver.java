package com.android.tudm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.tudm.service.TUDMService;
import com.turnupdmood.tudm.TUDMApplication;

/**
 * This class handle all the interruption for incoming calls.
 * 
 * @author rushikesh
 * 
 */

public class TUDMCallReceiver extends BroadcastReceiver {
    TelephonyManager telManager;
    Context context;
    /**
     * Instance of {@link com.android.tudm.service.TUDMService}
     */
    TUDMService mService;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        mService = TUDMService.getInstance();
    }

    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: {
                    
                    if (mService != null && mService.isPlaying()) {
                        TUDMApplication.sIsPhoneRinging = true;
                        mService.pauseMusic();
                    }
                    break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK: {

                    break;
                }
                case TelephonyManager.CALL_STATE_IDLE: {
                    if (mService != null && mService.isPaused()
                            && TUDMApplication.sIsPhoneRinging) {
                        TUDMApplication.sIsPhoneRinging = false;
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
