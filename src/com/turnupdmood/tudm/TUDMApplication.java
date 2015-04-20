package com.turnupdmood.tudm;

import android.app.Application;
import android.util.Log;

import com.android.tudm.model.CCMLinkedList;
import com.android.tudm.model.Track;
import com.android.tudm.utils.Constants;
import com.appblade.framework.AppBlade;

/**
 * Custom android {@link Application} class.<br>
 * Can be used to hold any data at the application global level. Also this is
 * the place to initialize the components before the application starts.
 * 
 * @author Mahesh Chauhan
 * 
 */
public class TUDMApplication extends Application {

    private static final String TAG = TUDMApplication.class.getSimpleName();
    public static final int MAX_TRACKS_TO_CACHE = 4;
    public static CCMLinkedList<Track> sCachedTracks = new CCMLinkedList<Track>();
    /**
     * Static boolean to keep track whether the user session is ongoing or not.
     * Once user logs in, session starts and keeps there till application
     * doesn't exist. Once session ends, user needs to re-authenticate by
     * putting the password on login screen.
     */
    public static boolean sIsSessionOngoing = false;
    /**
     * Static counter to maintain the track skip doing in a session. The maximum
     * limit of skipping track is defined {@link Constants#CCM_MAX_SKIP_LIMIT}
     */
    public static int sSkipCount = 0;
    /**
     * Static boolean to keep track whether call is in Ringing state.
     */
    public static boolean sIsPhoneRinging = false;
    public static boolean sIsError = false;

    public static boolean isSkipMessageShown=true;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        // Register AppBlade.
       // registerToAppBlade();
    }

    /**
     * Registers to AppBlade http://appblade.com server so that we can check for
     * the updates and can use other features of AppBlade Framework.
     */
    private void registerToAppBlade() {
        // Define the Keys.
        String uuid = "82154fe1-66aa-417b-bf24-b25ea0af172a";
        String token = "c50d9b8d77f0cb0a718f1a32c88351ce";
        String secret = "37aefa631661a28c74d883f648286794";
        String issuance = "1406820918";
        // Lets register now.
        AppBlade.register(this, token, secret, uuid, issuance,
                "https://appblade.com");
    }

    public static boolean cacheTrack(Track track) {
        if (sCachedTracks == null) {
            sCachedTracks = new CCMLinkedList<Track>();
        }
        synchronized (sCachedTracks) {
            int size = sCachedTracks.size();
            if (size < MAX_TRACKS_TO_CACHE) {
                // Cache only if the size not yet received to max allowed.
                sCachedTracks.add(track);
            }
        }
        // Print the cache.
        Log.d(TAG, "sCachedTracks : " + sCachedTracks.toString());
        return sCachedTracks.size() >= MAX_TRACKS_TO_CACHE ? false : true;
    }

    public static Track getCachedTrack() {
        Track track = null;
        if (sCachedTracks != null) {
            synchronized (sCachedTracks) {
                if (sCachedTracks.size() > 0) {
                    track = sCachedTracks.get(1);
                }
            }
        }
        return track;
    }

    public static void removeCachedTrack() {
        if (sCachedTracks != null) {
            synchronized (sCachedTracks) {
                if (sCachedTracks.size() > 0) {
                    sCachedTracks.remove(1);
                }
            }
        }
    }

    public static void clearCachedTracks() {
        if (sCachedTracks != null) {
            synchronized (sCachedTracks) {
                sCachedTracks = null;
            }
        }
    }
    
    public static boolean isTrackCached(String trackName){
        if(sCachedTracks!=null){
            for (int i = 1; i <= sCachedTracks.size(); i++) {
                Track track=sCachedTracks.get(i);
                String songName=sCachedTracks.get(i).getTitle().toString();
                //boolean isEquals=songName.equals(trackName);
                if(songName!=null&&songName.equals(trackName)){
                   return true;
                }
            }
        }
        return false;
    }
}
