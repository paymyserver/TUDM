package com.android.tudm.service;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.android.tudm.json.JsonParser;
import com.android.tudm.model.GetTrackResponse;
import com.android.tudm.model.Message;
import com.android.tudm.model.MessageUri;
import com.android.tudm.model.Song;
import com.android.tudm.model.SongUri;
import com.android.tudm.model.Track;
import com.android.tudm.utils.TUDMException;
import com.android.tudm.utils.TUDMUtils;
import com.android.tudm.utils.Constants;
import com.android.tudm.utils.ServerUtilities;
import com.android.tudm.utils.ServerUtilities.ResponseData;
import com.turnupdmood.tudm.TUDMApplication;
import com.google.gson.reflect.TypeToken;

public class TUDMCachingService extends IntentService {
    private static final String TAG = TUDMCachingService.class.getSimpleName();
    // Action to download the track to cache in the application.
    public static final String ACTION_DOWNLOAD_TRACK = "com.android.cloudcovermusic.DOWNLOAD_TRACK";
    public static final String ACTION_DOWNLOAD_TRACK_COMPLETED = "com.android.cloudcovermusic.DOWNLOAD_TRACK_COMPLETED";
    SharedPreferences mPref;
    private String mToken;

    public TUDMCachingService() {
        super("CCMCachingService");
        Log.d(TAG, "CCMCachingService()");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase(ACTION_DOWNLOAD_TRACK)) {
            mPref = getApplicationContext().getSharedPreferences(
                    Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
            if (mPref != null) {
                mToken = mPref.getString(Constants.PARAM_DEVICE_AUTH_TOKEN, "");
            }
            Log.d(TAG, "onHandleIntent() action : " + ACTION_DOWNLOAD_TRACK);
            // First get the track details from the server.
            try {
                ResponseData response = getNextTrack();
                if (response != null) {
                    JSONObject result = response.getResponseJson();
                    // Check if its success or failure.
                    if (response.isSuccess()) {
                        Log.d(TAG, "onHandleIntent() response.isSuccess() : "
                                + response.isSuccess());
                        // Parse the json response.
                        GetTrackResponse trackResponse = (GetTrackResponse) JsonParser
                                .parseJsonToType(getApplicationContext(),
                                        result,
                                        new TypeToken<GetTrackResponse>() {
                                        }.getType());
                        Track track = getTrack(trackResponse);
                        if(track != null) {
                            Log.d(TAG, "onHandleIntent() track : "
                                    + track);
                            // Now download the track.
                            String filePath = downloadTrackRequest(track
                                    .isMessage() ? track
                                    .getMessageUri().getMp3() : track
                                    .getSongUri().getOgg(), TUDMUtils.removeSpecialCharacters(track.getTitle()));
                            if(!TextUtils.isEmpty(filePath)) {
                                track.setSongPath(filePath);
                                Log.d(TAG, "onHandleIntent() Sending broadcast...");
                                if (track != null) {
                                    // Lets cache it.
                                    if (TUDMApplication.cacheTrack(track)) {
                                        // Cache limit not reached, lets
                                        // download next track.
                                        sendBroadcast();
                                    } else {
                                        Log.d(TAG,
                                                "onHandleIntent() Reached cacheLimit. Stop downloading...");
                                    }
                                }
                            }else{
                                sendBroadcast();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(ACTION_DOWNLOAD_TRACK_COMPLETED);
        getApplicationContext().sendBroadcast(intent);
    }

    /**
     * Sends a request to download the track.
     * 
     * @return {@link ByteArrayBuffer}
     * @throws com.android.tudm.utils.TUDMException
     */
    protected String downloadTrackRequest(String url, String fileName)
            throws TUDMException {
        Log.d(TAG, "Into downloadTrackRequest method");
        Context context = getApplicationContext();
        return ServerUtilities.getInstance(context).downloadResource(context,
                url + Constants.TOKEN_SUFFIX + mToken, fileName);
    }

    private Track getTrack(GetTrackResponse trackResponse) {
        Track track = null;
        if (trackResponse != null) {
            Song song = trackResponse.getSong();
            Message message = trackResponse.getMessage();
            if (song != null) {
                SongUri songUri = song.getSonguri();
                if (songUri != null) {
                    String oggTrack = songUri.getOgg();
                    if (!TextUtils.isEmpty(oggTrack)) {
                        // create a track.
                        track = new Track();
                        track.setTitle(song.getName());
                        track.setSongUri(songUri);
                        track.setAlbum(trackResponse.getAlbum());
                        track.setArtist(trackResponse.getArtist());
                    }
                }
            } else if (message != null) {
                // Its not a song, but a recorded message in mp3 format.
                MessageUri messageUri = message.getMessageUri();
                if (messageUri != null) {
                    String mp3Track = messageUri.getMp3();
                    if (!TextUtils.isEmpty(mp3Track)) {
                        track = new Track();
                        track.setTitle(message.getName());
                        track.setMessage(true);
                        track.setMessageUri(messageUri);
                    }
                }
            }
        }
        return track;
    }

    protected ResponseData getNextTrack() throws TUDMException {
        Log.d(TAG, "Into getNextTrack() method");
        return ServerUtilities.getInstance(getApplicationContext()).sendGetRequest(
                Constants.NEXT_TRACK_API + mToken);
    }
}
