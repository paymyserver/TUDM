package com.android.cloudcovermusic.service;

import java.io.File;
import java.io.FileInputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.cloudcovermusic.model.Album;
import com.android.cloudcovermusic.model.Message;
import com.android.cloudcovermusic.model.Song;
import com.android.cloudcovermusic.model.Track;
import com.android.cloudcovermusic.utils.CCMException;
import com.android.cloudcovermusic.utils.CCMUtils;
import com.android.cloudcovermusic.utils.Constants;
import com.cloudcovermusic.ccm.CCMApplication;
import com.cloudcovermusic.ccm.CCMPlayer;
import com.cloudcovermusic.ccm.R;

/**
 * The main component of the application and is responsible to play the actual
 * {@link Song} or {@link Message}.<br>
 * Starts usually by {@link CCMPlayer} activity once the {@link Song} or
 * {@link Message} is received from server.<br>
 * It needs to be stopped once track ended. And starts again for the next track.<br>
 * {@link CCMService} keeps running in the background even though user moves out
 * of the application.<br>
 * Uses Android {@link MediaPlayer} to play the track. Also display's the
 * notification in the notification bar for the track its playing.
 *
 * @author Mahesh Chauhan
 *
 */
public class CCMService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = CCMService.class.getSimpleName();
    // Singleton instance of the CCMService.
    private static CCMService mInstance = null;
    // Media Player instance which is required to play any media.
    private MediaPlayer mMediaPlayer = null;
    // Stores the Buffer position.
    private int mBufferPosition;
    // Callback instance to send the callback back to CCMPlayer activity.
    private CCMListener mListener;
    // Notification Manager to update the notification.
    NotificationManager mNotificationManager;
    // Notification builder to create custom notification.
    private NotificationCompat.Builder mBuilder;
    int mNotificationId = Constants.NOTIFICATION_ID;
    // The actual notification.
    Notification mNotification = null;
    // Stores the song which needs to be played.
    private Track mTrack;
    // Stores the url of the song to be stream.
    private String mMediaUrl;
    // Notification Id for the notification to be displayed.
    public static final int NOTIFICATION_ID = 1;
    // BroadcastReceiver to receive the remote broadcast from Notificaiont.
    private RemoteActionReceiver mRemoteActionReceiver;
    public static final int NOTIFICATION_TYPE_PLAY = 0;
    public static final int NOTIFICATION_TYPE_PAUSE = 1;
    private static final String INTENT_ACTION_REMOTE_ACTIONS = "remoteActions";
    private static final String INTENT_ACTION_REMOTE_ACTIONS_CANCEL = "remoteActionsCancel";
    private static final String INTENT_ACTION_REMOTE_ACTIONS_LAUNCH = "remoteActionsLaunchActivity";
    static final String INTENT_ACTION_FINISH_FORM_SERVICE = "closeActions";
    private Bitmap mNotiDrawable;
    // indicates the state of CCMService.
    enum State {
        Retrieving, Stopped, Preparing, Prepared, Playing, Paused, Error
    };
    

    State mState = State.Retrieving;

    boolean audioFocusGranted;

    AudioManager audioManager;
    
   
    OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    
    @Override
    public void onCreate() {
        mInstance = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mRemoteActionReceiver = new RemoteActionReceiver();
        // Register receiver to listen the remote actions from notificaiton.
        registerReceiver(mRemoteActionReceiver, new IntentFilter(INTENT_ACTION_REMOTE_ACTIONS));
        registerReceiver(mRemoteActionReceiver, new IntentFilter(INTENT_ACTION_REMOTE_ACTIONS_CANCEL));
        registerReceiver(mRemoteActionReceiver, new IntentFilter(INTENT_ACTION_REMOTE_ACTIONS_LAUNCH));
        
        audioManager = (AudioManager) getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        getAudioFocus();
          mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener(){

            @Override
            public void onAudioFocusChange(int focusChange) {

                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    startMusic();
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                    stopMusic();

                }
            }
        };
        
        
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(Constants.ACTION_PLAY_TRACK)) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                initMediaPlayer();
            }
        }
        return START_STICKY;
    }

    private void initMediaPlayer() {
        try {
            if (mTrack!=null && !TextUtils.isEmpty(mTrack.getSongPath())) {
                Log.d(TAG, "initMediaPlayer() Trying to play from cache");
                // create temp file that will hold byte array
                File tempFile = null;
                tempFile = new File(mTrack.getSongPath());
                Log.d(TAG, "initMediaPlayer() tempFile : " + tempFile);
                // Tried passing path directly, but kept getting
                // "Prepare failed.: status=0x1"
                // so using file descriptor instead
                FileInputStream fis = new FileInputStream(tempFile);
                mMediaPlayer.setDataSource("http://tudm003.appspot.com/1.mp3");
                mMediaPlayer.prepareAsync();
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "initMediaPlayer() Exception preparing");
            // release the player.
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            // Also cancel the notification if any.
            cancelNotification(getInstance(), mNotificationId);
            // If its an exception, lets throw error.

            if (mListener != null) {
                mListener.onPlayerError();
            }
        } finally {
            // Now we can safetly delete the cache Bytes of the song.
            CCMApplication.removeCachedTrack();
        }
        mState = State.Preparing;
    }

    public void restartMusic() {
        // Restart music
    }

    /**
     * Sets the buffer position of the player.
     *
     * @param progress
     */
    protected void setBufferPosition(int progress) {
        mBufferPosition = progress;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        // Begin playing music
        Log.d(TAG, "onPrepared()");
        mState = State.Prepared;
        startMusic();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError() what : " + what + " extra : " + extra);
        deleteTrack();
        if (mNotiDrawable != null) {
			mNotiDrawable = null;
		}
        // Also cancel the notification if any.
        cancelNotification(getInstance(), mNotificationId);
        // Stop the player.
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mListener != null) {
            mListener.onPlayerError();
        }
        mState = State.Error;
        return false;
    }

    @Override
    public void onDestroy() {
        deleteTrack();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if(mRemoteActionReceiver != null) {
            unregisterReceiver(mRemoteActionReceiver);
        }
        cancelNotification(getInstance(), mNotificationId);
        mState = State.Retrieving;
        mInstance = null;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * Pause the track which is getting played.
     */
    public void pauseMusic() {
        if (mState.equals(State.Playing)) {
            mMediaPlayer.pause();
            mState = State.Paused;
            updateNotification(NOTIFICATION_TYPE_PAUSE,mNotiDrawable);
            if (mListener != null) {
                mListener.onTrackPaused();
            }
        }
    }

    /**
     * Stops playing track.
     */
    public void stopMusic() {
        if (mState.equals(State.Playing)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
            mState = State.Stopped;
            if (mListener != null) {
                mListener.onTrackStopped();
            }
            
        }
    }

    /**
     * Start playing track as mentioned by  or .
     */
    public void startMusic() {
        Log.d(TAG, "startMusic()");
        /*if (!mState.equals(State.Retrieving) && (mState.equals(State.Prepared) || mState
                        .equals(State.Paused))) {
          */  if (getAudioFocus()) {
               mMediaPlayer.start();
                State oldState = mState;
                mState = State.Playing;
                updateNotification(NOTIFICATION_TYPE_PLAY, mNotiDrawable);
                Log.d(TAG, "startMusic() mListener : " + mListener);
                if (mListener != null) {
                    Log.d(TAG, "startMusic() mState : " + mState);
                    if (oldState.equals(State.Paused)) {
                        mListener.onTrackResumed();
                    } else {
                        mListener.onTrackStarted();
                    }
                }

          //  }

        }
    }

    /**
     * Seek the player track to mentioned value.
     */
    public void seekTo(int seekTo) {
        if (mState.equals(State.Playing) || mState.equals(State.Paused)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(seekTo);
            }
        }
    }

    /**
     * Returns whether {@link #mMediaPlayer} is still playing the track or not.
     *
     * @return
     */
    public boolean isPlaying() {
        if (mState.equals(State.Playing)) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether {@link #mMediaPlayer} is in paused state.
     * @return
     */
    public boolean isPaused() {
        if (mState.equals(State.Paused)) {
            return true;
        }
        return false;
    }

    /**
     * Sets the {@link CCMListener} callback. {@link CCMPlayer} needs to listen
     * to this callback.
     *
     * @param listener
     */
    public void setCCMListener(CCMListener listener) {
        mListener = listener;
    }

    /**
     * Removes {@link CCMListener} callback. Called by {@link CCMPlayer}.
     */
    public void removeCCMListener() {
        mListener = null;
    }

    /**
     * Gets the music duration. Its a total duration of the track which is
     * getting played currently.
     *
     * @return
     */
    public int getMusicDuration() {
        if (!mState.equals(State.Error) && !mState.equals(State.Preparing)
                && !mState.equals(State.Retrieving) && !mState.equals(State.Stopped)) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    /**
     * Gets the current position of the track which is getting played.
     *
     * @return
     */
    public int getCurrentPosition() {
        if (!mState.equals(State.Error)) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    /**
     * Gets the Buffer percentage.
     * @return
     */
    public int getBufferPercentage() {
        return mBufferPosition;
    }

    public static CCMService getInstance() {
        return mInstance;
    }


    /**
     * Sets the {@link Song} to be played from the cache.. Called by {@link CCMPlayer} when a
     * track needs to be played from the cache.
     *
     * @param track Track to be played from the cache.
     */
    public void setSong(Track track) {
        mTrack = track;
    }

    /**
     * Gets the current {@link Song} or {@link Message} name.
     * @return
     */
    public String getSongName() {
        if (mTrack.isMessage()) {
            return mTrack.getTitle();
        } else {
            return mTrack.getTitle() + "(" + mTrack.getAlbum().getName() + ")";
        }
    }

    /**
     * Gets the album art url form the {@link Album}.
     * @return
     */
    public String getAlbumArtUrl() {
        return mTrack.getAlbum() != null ? mTrack.getAlbum().getThumbnail() : "";
    }

    public String getArtistName() {
        return mTrack.getArtist() != null ? mTrack.getArtist().getName() : "";
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * getMusicDuration() / 100);
    }

    /** Updates the notification. */
    void updateNotification(int type) {
        // Notify NotificationManager of new intent
        setUpAsForeground(type, null);
    }

    void updateNotification(int type, Bitmap albumart) {
        setUpAsForeground(type, albumart);
    }
    /**
     * Configures service as a foreground service. A foreground service is a
     * service that's doing something the user is actively aware of (such as
     * playing music), and must appear to the user as a notification. That's why
     * we create the notification here.
     */
    void setUpAsForeground(int type, Bitmap albumart) {
        mNotification = getNotification(type, albumart);
        startForeground(NOTIFICATION_ID, mNotification);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopForeground(true);
        deleteTrack();
        mState = State.Stopped;
		if (mNotiDrawable != null) {
			mNotiDrawable = null;
		}
        if (mListener != null) {
            mListener.onTrackCompleted();
        }
    }

    /**
     * Creates a notification bar that displays pause and play button to control
     * the player from notification bar
     * 
     * @param albumart
     */
    public Notification getNotification(int type, Bitmap albumart) {
        // Using RemoteViews to bind custom layouts into Notification
        final RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.ccm_custom_notification_layout);

        Intent activityIntent = new Intent(this,CCMPlayer.class);
        //Resumes the activity When it goes in background
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Locate and set the Text into customnotificationtext.xml TextViews
        remoteViews.setTextViewText(R.id.ccmTitle, getSongName());
        remoteViews.setTextViewText(R.id.ccmArtist, getArtistName());
        // Locate album art view and set album art drawable.
        if (albumart != null) {
            remoteViews.setImageViewBitmap(R.id.ccmAlbumArt, albumart);
        } else {
            remoteViews.setImageViewResource(R.id.ccmAlbumArt,
                    R.drawable.ccm_no_album_art);
        }
        // Show the play/pause action icon.
        if (type == NOTIFICATION_TYPE_PAUSE) {
            remoteViews.setViewVisibility(R.id.play, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.pause, View.GONE);
        } else if (type == NOTIFICATION_TYPE_PLAY) {
            remoteViews.setViewVisibility(R.id.play, View.GONE);
            remoteViews.setViewVisibility(R.id.pause, View.VISIBLE);
        }

        // Setup the PendingIntent for the actions.
        Intent actionIntent = new Intent(INTENT_ACTION_REMOTE_ACTIONS);
        PendingIntent pendingAction = PendingIntent.getBroadcast(this, 0,
                actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.play, pendingAction);
        remoteViews.setOnClickPendingIntent(R.id.pause, pendingAction);

        Intent cancelIntent = new Intent(
                INTENT_ACTION_REMOTE_ACTIONS_CANCEL);
        PendingIntent pendingActionCancel = PendingIntent.getBroadcast(this, 0,
                cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.cancel, pendingActionCancel);

        /*Intent launchIntent = new Intent(INTENT_ACTION_REMOTE_ACTIONS_LAUNCH);
        PendingIntent pendingActionLaunch = PendingIntent.getBroadcast(this, 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.ccmAlbumArt,
                pendingActionLaunch);
        remoteViews.setOnClickPendingIntent(R.id.ccmTrackInfoNoti,
                pendingActionLaunch);*/
        
        mBuilder = new NotificationCompat.Builder(this)
        // Set Icon 
                .setSmallIcon(R.drawable.ic_launcher)
                // Set Ticker Message
                .setTicker(getSongName())
                // Dismiss Notification
                .setAutoCancel(false)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Set RemoteViews into Notification
                .setContent(remoteViews);
        return mBuilder.build();
    }

    public void cancelNotification(Context ctx, int notifyId) {
        try{
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx
                .getSystemService(ns);
        nMgr.cancel(notifyId);
        }catch(Exception e){
            Log.d(TAG, ""+e.getMessage());
        }
    }

    private class RemoteActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "RemoteActionReceiver onReceive()");
            if (intent.getAction().equals(INTENT_ACTION_REMOTE_ACTIONS)) {
                if (mState == State.Playing) {
                    pauseMusic();
                } else if (mState == State.Paused) {
                    startMusic();
                }
            } else if (intent.getAction().equals(
                    INTENT_ACTION_REMOTE_ACTIONS_CANCEL)) {
                // Tell activity to get finish.
                if (mListener != null) {
                    mListener.onPlayerStopped();
                }
                // sendBroadcast(new Intent(INTENT_ACTION_FINISH_FORM_SERVICE));
                // Stop the Music first.
                stopMusic();
                // Stop the service.
                stopSelf();
                // Lets dismiss the notification.
                cancelNotification(context, mNotificationId);

            }
        }
    }

    public void setAlbumArt(Bitmap drawable) {
    	if(drawable!=null){
            mNotiDrawable = drawable;
    	}else{
    		BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            mNotiDrawable = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ccm_no_album_art, options);
    	}
        updateNotification(mState == State.Playing ? NOTIFICATION_TYPE_PLAY
                : NOTIFICATION_TYPE_PAUSE, mNotiDrawable);
    }

    private void deleteTrack() {
        if (mTrack != null && !TextUtils.isEmpty(mTrack.getSongPath())) {
            File file = new File(mTrack.getSongPath());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        
        switch (focusChange) {
        case AudioManager.AUDIOFOCUS_GAIN:
            // resume playback
        	
        	try{
            if (mMediaPlayer == null) {
                initMediaPlayer();
            } else if (mState!=State.Error&&mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                startMusic();
            }
            mMediaPlayer.setVolume(1.0f, 1.0f);
        	}catch(Exception ae){
        		Log.d(TAG, ""+ae.getMessage());
        	}
            break;

        case AudioManager.AUDIOFOCUS_LOSS:
            // Lost focus for an unbounded amount of time: stop playback and
            // release media player
            try{
                if (mMediaPlayer != null&& mMediaPlayer.isPlaying()){
                    pauseMusic();
                }
            }catch(IllegalStateException e){
                Log.d(TAG,"Handled runtime "+e.getStackTrace());
            }catch(Exception ee){
                Log.d(TAG,"Handled runtime "+ee.getStackTrace());
            }
         
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            // Lost focus for a short time, but we have to stop
            // playback. We don't release the media player because playback
            // is likely to resume
        	
        	try{
        		if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                    pauseMusic();
        		
        	}catch(Exception ae){
        		Log.d(TAG, ""+ae.getMessage());
        	}
              
           
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            // Lost focus for a short time, but it's ok to keep playing
            // at an attenuated level
        	try{
            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                mMediaPlayer.setVolume(0.1f, 0.1f);
        	}catch(Exception ae){
        		System.out.println("Illegal state to set sound");
        	}
            break;
        default:

        }
        
    }
    public boolean getAudioFocus(){
        boolean isFocusGained=false;
      
        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            isFocusGained=true;
        }  
        return isFocusGained;
    }
    
}
