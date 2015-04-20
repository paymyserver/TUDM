package com.cloudcovermusic.ccm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.cloudcovermusic.json.JsonParser;
import com.android.cloudcovermusic.model.ActivatePlayerResponse;
import com.android.cloudcovermusic.model.Album;
import com.android.cloudcovermusic.model.Artist;
import com.android.cloudcovermusic.model.ErrorResponse;
import com.android.cloudcovermusic.model.GetCommandResponse;
import com.android.cloudcovermusic.model.GetTrackResponse;
import com.android.cloudcovermusic.model.Message;
import com.android.cloudcovermusic.model.MessageUri;
import com.android.cloudcovermusic.model.MetaData;
import com.android.cloudcovermusic.model.Song;
import com.android.cloudcovermusic.model.SongUri;
import com.android.cloudcovermusic.model.Track;
import com.android.cloudcovermusic.service.CCMCachingService;
import com.android.cloudcovermusic.service.CCMListener;
import com.android.cloudcovermusic.service.CCMService;
import com.android.cloudcovermusic.utils.CCMErrorHandler;
import com.android.cloudcovermusic.utils.CCMException;
import com.android.cloudcovermusic.utils.CCMUtils;
import com.android.cloudcovermusic.utils.Constants;
import com.android.cloudcovermusic.utils.ServerUtilities;
import com.android.cloudcovermusic.utils.ServerUtilities.ResponseData;
import com.google.gson.reflect.TypeToken;

/**
 * {@link CCMPlayer} is the main screen of the application which basically plays
 * the track and let user to interact with the application.<br>
 * This activity is launched by {@link CCMLogin} once the user successfully
 * logged in and registers the device.<br>
 * This activity is responsible for below mentioned tasks:<br>
 * - First checks the status of the device, whether the device is activated or
 * still in inactive condition.<br>
 * - If inActive, lets user to activate the player or activate automatically for
 * the first time after login.<br>
 * - After activating the player, starts playing the {@link Song} or
 * {@link Message}.<br>
 * - Lets user to control the tracks.<br>
 * - User can Play, Pause, Stop, Next the track.<br>
 *
 *
 * @author Mahesh Chauhan
 *
 */
public class CCMPlayer extends Activity implements OnSeekBarChangeListener,
        OnSeekCompleteListener, CCMListener, DrawerListener,MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = CCMPlayer.class.getSimpleName();
    // Seekbar to show the track progress.
    private SeekBar mTrackSeek;
    // Button to let user playing the track.
    private LinearLayout mPlayTrack;
    // Button to let user pause the track.
    private LinearLayout mPauseTrack;
    // Button to let user remove song
    private LinearLayout mRemoveSong;
    // Button to let user remove song
    private ImageView mRemoveSongView;
    //this shows three option i.e. remove, play and pause song
    private LinearLayout mTrackActionPanel;
    //this shows three option i.e. inappropriate, wrong station and dislike
    private LinearLayout mRemovSongAction;
    // Track seek layout
    private RelativeLayout mTrackSeekLayout;
    // Button to let user move to the next track.
    private LinearLayout mNextTrack;
    // Button to let user skip the track.
    private ImageView mSkipTrackView;
    // Button to let user play the track.
    private ImageView mPlayTrackView;
    // Service responsible for playing the tracks.
    private CCMService mService;
    // To keep track whether a track is finished or not.
    private boolean mIsTrackFinished = true;
    // Displays the track's current duration.
    private TextView mTrackCurrentDuration;
    // Displays the track's total duration.
    private TextView mTrackTotalDuration;
    // Displays track's extra info.
    private TextView mTrackInfo;
    // Displays track's artist info.
    private TextView mTrackArtist;
    private LinearLayout mAlbumArt;
    // Drawer Layout to show the CCM Player options.
    private DrawerLayout mDrawerLayout;
    // ImageButton to toggle the drawer.
    private LinearLayout mDrawerToggleImg;
    private ProgressDialog mProgressDialog;
    private LinearLayout mSignoutPanel;
    // Async task to get the next track.
    private GetNextTrackTaskAsync mNextTrackAsync;
    // Async task to the get the player command.
    private GetCommandTaskAsync mGetCommandAsync;
    // Async task to get the album art.
    private GetAlbumArtTask mGetAlbumArtAsync;
    // Async task to activate the player/device.
    private ActivatePlayerTaskAsync mActivatePlayerAsync;
    // Async task to logout the device from the server.
    private LogOutTaskAsync mLogoutAsync;
    private static final int SERVICE_POLL_TIME_IN_MILLIS = 1000;
    /**
     * A timer which will keep polling the {@link CCMService} for the status of
     * track being played. This is to update the seekbar properly and also the
     * player UI controls. This timer will be scheduled to run in the interval
     * defined by SERVICE_POLL_TIME_IN_MILLIS.
     */
    private Timer mServicePollTimer;
    /**
     * To keep track whether the player is launched first time after login or
     * its already been logged in by user. This is to differentiate the behavior
     * when the player is inActive. If its value is "true" then we will activate
     * the player automatically else will prompt user to activate the player.
     */
    private boolean mIsFirstTime = true;
    /**
     * Boolean to be passed with intent from {@link CCMLogin} that this user has
     * been registering first time on this device and device has to be activated
     * automatically.
     */
    private boolean mForceActivate = false;

    /**
     * Handle to get next track
     */
    final Handler mHandler = new Handler();
    /**
     * Timer to get next track after every 30 sec if error occurred
     */
    private Timer mRetryingTimer;
    private int mRetryInterval = RETRY_NEXT_TRACK_STARTING_INTERVAL;
    private static final int RETRY_NEXT_TRACK_STARTING_INTERVAL = 30000;
    // Layout which helps to show album art when some kind of error occurs and
    // the song is not playing
    private ViewFlipper mBannerArt;

    /**
     * To keep track whether the activity is active or not.
     */
    private boolean mIsActive;
    // add custom font for text view
    Typeface mRobotoRegular, mRobotoBold;
    private TextView mPlayText,mSkipText;
    //TextView that are shown in drawer layout
    private TextView mDrawerMenuText;
    private TextView mDrawerPolicyText;
    private TextView mDrawerHelpText;
    private TextView mDrawerTermsText;
    private TextView mDrawerSignOutText;
    private TextView mAboutTitle;
    private TextView mAboutAppVersion;
    private TextView mAboutUserLoggedin;
    // media id is used while removing song
    private int mediaID;

    private boolean mIsPlayingSong=false;
    Track mTrack;
    AudioManager audioManager;
    // Show dialog
    private Dialog mDialog;

    private boolean isUserSignOut;
    private  RelativeLayout mDrawerOptions;

    private LinearLayout mAboutActionPannel;

    TextView mAboutActionBackText;

    /**
     * Reason for removing song
     */
    String mReason;
    boolean isDeviceActivateDialogShown=false;

    /**
     * This boolean value is added to control the multiple click of skip button 
     */
    boolean mSkipBtnControl=true;

    //Addded by Sushil
    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Into onCreate()");
        super.onCreate(savedInstanceState);
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ccm_player_screen_layout);
        /*CCMApplication.sIsError = false;
        // Stop the retry timer if its ongoing.
        mIsActive = true;
        // hide keyboard when user switch from login to player
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            mForceActivate = extras != null
                    && extras.getBoolean(Constants.INTENT_EXTRA_FORCE_ACTIVATE,
                            false) ? true : false;
        }
        SharedPreferences mPref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        isUserSignOut= mPref.getBoolean(Constants.PREF_KEY_IS_USER_SIGNOUT, false);
        
        // Initialise the views and instances.
        */
        init();
        // Setup the listeners.
        setListeners();
        // First of all get the service to check if the service is still going
        // on and playing the music.
        //  mService = CCMService.getInstance();
        //  if (mService == null) {
        // CCMService hasn't started yet. So lets check the command first
        // and then we will play the track (in case device is already
        // activated).
        //     getCommand(false);
        // Start the service. We are doing that in advance so that Service
        // will
        // be instantiated in prior to actually using a service for playing
        // the
        // media.
        //    startServiceExplicitly();
        // } else {
        // This is the case when user came out of application when the music
        // was playing and again coming back to application. CCMService is
        // already running, no need to do anything. If there
        // is no track playing, lets try loading next track.
         /*   if (!mService.isPlaying() && !mService.isPaused()) {
                getCommand(false);
            } else {
                mIsTrackFinished = false;
                startPollingTimer();
                mService.setCCMListener(this);
                reinitialiseControls();
            }
        }*/

    }

    /**
     * Starts the {@link CCMService}.
     */
    void startServiceExplicitly() {
        startService(new Intent(CCMPlayer.this, CCMService.class));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Into onDestroy()");
        super.onDestroy();
        mIsActive = false;
        stopPollingTimer();
    }

    /**
     * Initialise the views and instances.
     */


    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ccmTrackSeek){
            /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
            if(mediaPlayer.isPlaying()){
                SeekBar sb = (SeekBar)v;
                int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMillisecconds);
            }
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
        mPlayTrack.setEnabled(true);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        /** Method which updates the SeekBar secondary progress by current song loading from URL position*/
        mTrackSeek.setSecondaryProgress(percent);
    }

    private void init() {
        Log.d(TAG, "Into init()");
        mProgressDialog = new ProgressDialog(CCMPlayer.this);
        mProgressDialog.setCancelable(false);
        mTrackSeek = (SeekBar) findViewById(R.id.ccmTrackSeek);
        Drawable seekBarThumb = this.getResources().getDrawable(
                R.drawable.ccm_bar_tracker);
        mTrackSeek.setThumb(seekBarThumb);
        mPlayTrack = (LinearLayout) findViewById(R.id.ccmPlayTrack);
        //mPlayTrack.setEnabled(true);
        mPauseTrack = (LinearLayout) findViewById(R.id.ccmPauseTrack);
        // mPauseTrack.setEnabled(false);
        mPlayTrack.setVisibility(View.VISIBLE);
        mPauseTrack.setVisibility(View.GONE);
        mNextTrack = (LinearLayout) findViewById(R.id.ccmNextTrack);
        mSkipTrackView = (ImageView) findViewById(R.id.skipTrack);
        mPlayTrackView = (ImageView) findViewById(R.id.playTrack);
        mTrackCurrentDuration = (TextView) findViewById(R.id.ccmTrackCurrentTime);
        mTrackTotalDuration = (TextView) findViewById(R.id.ccmTrackTotalTime);
        mTrackInfo = (TextView) findViewById(R.id.ccmSongInfo);
        mTrackArtist = (TextView) findViewById(R.id.ccmArtistInfo);
        // mAlbumArt = (LinearLayout) findViewById(R.id.ccmAlbumArt);
        mDrawerToggleImg = (LinearLayout) findViewById(R.id.ccmDrawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mSignoutPanel = (LinearLayout) findViewById(R.id.signoutPanel);
        mTrackSeekLayout = (RelativeLayout) findViewById(R.id.trackSeekLayout);
        mPlayText = (TextView) findViewById(R.id.ccmPlayText);
        mSkipText = (TextView) findViewById(R.id.ccmSkipText);
        mDrawerMenuText = (TextView) findViewById(R.id.ccmDrawerMenuText);
        mDrawerHelpText = (TextView) findViewById(R.id.ccmDrawerHelpText);
        mDrawerPolicyText =null;// (TextView) findViewById(R.id.ccmDrawerPolicyText);
        mDrawerTermsText = null;//(TextView) findViewById(R.id.ccmDrawerTermsText);
        mDrawerSignOutText = (TextView) findViewById(R.id.ccmDrawerSignOutText);

        mTrackActionPanel=(LinearLayout) findViewById(R.id.ccmTrackActionPanel);
        mRemovSongAction=(LinearLayout) findViewById(R.id.ccmRemovSongAction);
        mRemoveSongView=(ImageView) findViewById(R.id.removeSong);
        if (CCMApplication.sSkipCount >= Constants.CCM_MAX_SKIP_LIMIT) {
            updateSkipActionState(false);
        } else {
            updateSkipActionState(true);
        }
        mBannerArt = (ViewFlipper) findViewById(R.id.viewflipper);
//        updateAlbumArt(false);

        SharedPreferences mPref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        mIsFirstTime = mPref.getBoolean(Constants.FIRST_INSTALL, true);
        // Check if the user as installed the app for the first time
        if (mIsFirstTime) {
            mPref.edit().putBoolean(Constants.FIRST_INSTALL, false).commit();
        }
        mRobotoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Regular.ttf");
        mRobotoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Bold.ttf");
        mTrackCurrentDuration.setTypeface(mRobotoRegular);
        mTrackTotalDuration.setTypeface(mRobotoRegular);
        mTrackInfo.setTypeface(mRobotoBold);
        mTrackArtist.setTypeface(mRobotoRegular);
        mPlayText.setTypeface(mRobotoRegular);
        mSkipText.setTypeface(mRobotoRegular);
        mDrawerMenuText.setTypeface(mRobotoRegular);
        mDrawerHelpText.setTypeface(mRobotoRegular);
//        mDrawerPolicyText.setTypeface(mRobotoRegular);
//        mDrawerTermsText.setTypeface(mRobotoRegular);
        mDrawerSignOutText.setTypeface(mRobotoRegular);

        audioManager = (AudioManager) getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);

        mDialog = new Dialog(CCMPlayer.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.ccm_activateuser_dialog_layout);

        mAboutTitle=(TextView) findViewById(R.id.ccmPlayerAboutActionTitle);
        mAboutTitle.setTypeface(mRobotoRegular);
        mAboutUserLoggedin=(TextView) findViewById(R.id.ccmPlayerAboutActionUserLoggedIn);
        mAboutUserLoggedin.setTypeface(mRobotoRegular);
        mAboutUserLoggedin.setText(getUserName()+" "+"Sushil");//getResources().getString(R.string.ccm_player_about_user_logedin));
        mAboutAppVersion=(TextView) findViewById(R.id.ccmPlayerAboutAppVersion);
        mAboutAppVersion.setTypeface(mRobotoRegular);
        mAboutAppVersion.setText(getResources().getString(R.string.ccm_player_about_app_version)+" "+getApplicationVesrion());

        mDrawerOptions=(RelativeLayout) findViewById(R.id.ccm_drawer_options);
        mAboutActionPannel=(LinearLayout) findViewById(R.id.ccmAboutActionPannel);

        mAboutActionBackText=(TextView) findViewById(R.id.ccmPlayerAboutBackText);
        mAboutActionBackText.setTypeface(mRobotoRegular);
    }

    /**
     * Sets up the listeners such as seekbar listener, onClickListener etc.
     */

    private void primarySeekBarProgressUpdater() {
        mTrackSeek.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };
            handler.postDelayed(notification,1000);
        }
    }
   /* private String songName="";
    public void playSong(View view){
        initView();
            if(mediaPlayer.isPlaying())    //Stop the mediaplayer if it's already playing
                mediaPlayer.stop();
        try {
        switch(view.getId())          //Choose the clip to be played
            {
                case R.id.track1:
                    mediaPlayer.setDataSource(getApplicationContext(),"http://tudm003.appspot.com/track01.mp3");
                    break;
                case R.id.track02:
                    mediaPlayer.setDataSource("http://tudm003.appspot.com/track02.mp3");
                    break;
                case R.id.track03:
                    mediaPlayer.setDataSource("http://tudm003.appspot.com/track03.mp3");
                    break;
                case R.id.track04:
                    mediaPlayer.setDataSource("http://tudm003.appspot.com/track04.mp3");
                    break;
            }


            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();           //Start the mediaplayer
        }
*/



    private void initView() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mTrackSeek.setMax(99); // It means 100% .0-99
        // mTrackSeek.setOnTouchListener(this);
    }

    private void setListeners() {
        Log.d(TAG, "Into setListeners()");
        mTrackSeek.setOnSeekBarChangeListener(this);
        mPlayTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                initView();
                try {
                    mediaPlayer.setDataSource("http://tudm003.appspot.com/track01.mp3"); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
                    mediaPlayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL

                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    mPlayTrack.setVisibility(View.GONE);
                    mPauseTrack.setVisibility(View.VISIBLE);

                }else {
                    mediaPlayer.pause();
                    mPlayTrack.setVisibility(View.VISIBLE);
                    mPauseTrack.setVisibility(View.GONE);
                }

                primarySeekBarProgressUpdater();
               /* if (mService != null && mService.isPaused()) {
                    mService.startMusic();
                } else {
                    if (mService != null) {
                        mService.startMusic();
                    } else {
                        getCommand(false);
                    }
                }*/
            }
        });

        mPauseTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                mPlayTrack.setVisibility(View.VISIBLE);

                mPauseTrack.setVisibility(View.GONE);/* if (mService != null && mService.isPlaying()) {
                    mService.pauseMusic();
                } else {
                    if (mService != null) {
                        mService.pauseMusic();
                    } else {
                        getCommand(false);
                    }
                }*/
            }
        });

        mNextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CCMApplication.sSkipCount >= Constants.CCM_MAX_SKIP_LIMIT) {
                    updateSkipActionState(false);

                    //setDefaultAlbumArt();
                } else {
                    //setDefaultAlbumArt();
                    if (mService != null) {
                        mService.removeCCMListener();
                    }
                    if(mSkipBtnControl)
                    {
                        stopMusic();
                        // restart service
                        startServiceExplicitly();

                        getCommand(true);
                        mSkipBtnControl=false;
                    }
                }

            }
        });

        // Set the drawer listener.
        mDrawerLayout.setDrawerListener(this);
        mDrawerToggleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout != null) {
                    if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                    } else {
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                    }
                }
            }
        });

        mSignoutPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ServerUtilities.isNetworkAvailable(getApplicationContext())) {
                    showToast(getResources().getString(R.string.error_network_unavailable));
                    return ;
                }else{
                    logout();
                }
            }
        });

        LinearLayout needHelp = (LinearLayout) findViewById(R.id.needHelpPanel);
        needHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchHelp = new Intent(getApplicationContext(),
                        CCMHelp.class);
                launchHelp.putExtra(Constants.INTENT_EXTRA_NEED_HELP_TYPE,
                        CCMHelp.NEED_HELP_TYPE_PLAYER);
                startActivity(launchHelp);
                //CloseDrawer();
            }
        });

        LinearLayout songRemove = (LinearLayout) findViewById(R.id.ccmDrawerSongRemoval);
        songRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent songRemoval = new Intent(
                        getApplicationContext(), CCMSongRemoval.class);
                startActivity(songRemoval);
                //CloseDrawer();
            }
        });

       /* LinearLayout privacyPolicy = (LinearLayout) findViewById(R.id.ccmPrivacyPanel);
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchprivacyPolicy = new Intent(
                        getApplicationContext(), CCMPolicyTerms.class);
                launchprivacyPolicy.putExtra("value", Constants.CCM_ANDROID_PRIVACY);
                startActivity(launchprivacyPolicy);
                //CloseDrawer();
            }
        });

        LinearLayout showTerms = (LinearLayout) findViewById(R.id.ccmTermsPanel);
        showTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchshowTerms = new Intent(getApplicationContext(),
                        CCMPolicyTerms.class);
                launchshowTerms.putExtra("value", Constants.CCM_ANDROID_TERMS);
                startActivity(launchshowTerms);
                //CloseDrawer();
            }
        });
*/

        /*LinearLayout devicePanel = (LinearLayout) findViewById(R.id.manageDevicePanel);
        devicePanel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent launchdevicePanel = new Intent(getApplicationContext(),
                        CCMDeviceList.class);
                startActivity(launchdevicePanel);
                // CloseDrawer();
            }
        });
*/
        LinearLayout playlistPanel = (LinearLayout) findViewById(R.id.ccmDrawerManagePlaylist);
        playlistPanel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent launchdevicePanel = new Intent(getApplicationContext(),
                        CCMPlayListManager.class);
                startActivity(launchdevicePanel);
                //CloseDrawer();
            }
        });

        mRemoveSong = (LinearLayout) findViewById(R.id.ccmRemoveSong);

        mRemoveSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateActionPanel(false);
            }
        });

        LinearLayout mCancel = (LinearLayout) findViewById(R.id.ccmCancel);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateActionPanel(true);
            }
        });

        LinearLayout mInappropriate = (LinearLayout) findViewById(R.id.ccmInapppropriate);
        mInappropriate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSong(Constants.PARAM_REMOVE_SONG_REASON_INAPPOPRIATE);
            }
        });

        LinearLayout mWrongStation = (LinearLayout) findViewById(R.id.ccmWrongstation);

        mWrongStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSong(Constants.PARAM_REMOVE_SONG_REASON_WRONG_STATION);
            }
        });

        LinearLayout mDisLike = (LinearLayout) findViewById(R.id.ccmDislike);
        mDisLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSong(Constants.PARAM_REMOVE_SONG_REASON_DISLIKE);
            }
        });

        LinearLayout mAboutPannel=(LinearLayout) findViewById(R.id.aboutPanel);
        mAboutPannel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDrawerOptions.setVisibility(View.GONE);
                mAboutActionPannel.setVisibility(View.VISIBLE);
            }
        });

        LinearLayout mAboutBack= (LinearLayout) findViewById(R.id.cmmPlayerAboutBack);
        mAboutBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAboutActionPannel.setVisibility(View.GONE);
                mDrawerOptions.setVisibility(View.VISIBLE);
            }
        });

       /* LinearLayout mManageMessage=(LinearLayout) findViewById(R.id.ccmDrawerManageMessage);
        mManageMessage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent manageMessage = new Intent(
                        getApplicationContext(), CCMManageMessage.class);
                startActivity(manageMessage);
                //CloseDrawer();
            }
        });
        */
/*        LinearLayout mManagePlayList=(LinearLayout) findViewById(R.id.ccmDrawerManagePlaylist);
        mManagePlayList.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent managePlalist = new Intent(
                        getApplicationContext(), CCMManagePlayList.class);
                startActivity(managePlalist);
            }
        });*/
    }

    public void CloseDrawer()
    {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        }
    }

    /**
     * this method call remove song api to remove song from server play list
     * reason will say why user want to remove song
     */

    private void removeSong(String reason) {
        mReason=reason;
        //showToast(reason);
        RemoveAsynctask mRemoveAsynctask=new RemoveAsynctask();
        mRemoveAsynctask.execute();
    }
    /**
     * Starts the polling to update the Player UI liked seekbar and current
     * duration.
     */
    synchronized void startPollingTimer() {
        Log.d(TAG, "Into startPollingTimer()");
        mServicePollTimer = new Timer();
        mServicePollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int currentPosition = 0;
                while (!mIsTrackFinished) {
                    if (mService == null) {
                        return;
                    }
                    int total;
                    try {
                        currentPosition = mService.getCurrentPosition();
                        total = mService.getMusicDuration();
                    } catch (IllegalStateException e) {
                        Log.d(TAG, "Song ended, just stop getting duration");
                        cancel();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        cancel();
                        return;
                    }

                    NumberFormat curSecondsFormat = new DecimalFormat("00");
                    int seconds=currentPosition/1000;
                    int songSeconds=seconds%60;
                    int songMinutes=seconds/60;

                    final String curTime =curSecondsFormat.format(songMinutes)+":"+curSecondsFormat.format(songSeconds);

                    int totalSeconds=total/1000;
                    int totalSongSeconds=totalSeconds%60;
                    int totalSongMinutes=totalSeconds/60;
                    final String totalDuration=curSecondsFormat.format(totalSongMinutes)+":"+curSecondsFormat.format(totalSongSeconds);

                    mTrackSeek.setMax(total); // song duration
                    mTrackSeek.setProgress(currentPosition); // for current
                    // song
                    // progress
                    mTrackSeek.setSecondaryProgress(mService
                            .getBufferPercentage()); // for
                    // buffer
                    // progress
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTrackCurrentDuration.setVisibility(View.VISIBLE);
                            mTrackTotalDuration.setVisibility(View.VISIBLE);
                            mTrackTotalDuration.setText(totalDuration);
                            mTrackCurrentDuration.setText(curTime);
                        }
                    });
                }
                stopPollingTimer();
            }
        }, 0, SERVICE_POLL_TIME_IN_MILLIS);
    }

    /**
     * Stops the Polling timer once track is stopped.
     */
    synchronized void stopPollingTimer() {
        Log.d(TAG, "Into stopPollingTimer()");
        if (mServicePollTimer != null) {
            Log.d(TAG, "Into stopPollingTimer() cancelled");
            mServicePollTimer.cancel();
            mServicePollTimer = null;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (fromUser) {
            // Lets move forward or backward if user updates the seekbar
            // position.
            int secondaryPosition = mTrackSeek.getSecondaryProgress();
            Log.d(TAG, "onProgressChanged fromuser : " + fromUser + " mService : " + mService);
            if (mService != null) {
                Log.d(TAG, "onProgressChanged progress : " + progress
                        + " secondaryPosition : " + secondaryPosition);
                mService.seekTo(progress < secondaryPosition ? progress
                        : secondaryPosition == 0 ? progress : secondaryPosition);
                mTrackSeek
                        .setProgress(progress < secondaryPosition ? progress
                                : secondaryPosition == 0 ? progress
                                : secondaryPosition);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "Into onStartTrackingTouch()");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "Into onStopTrackingTouch()");
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.d(TAG, "Into onSeekComplete()");
    }

    /**
     * {@link CCMListener} callback. {@link CCMService} calls this callback API
     * when track starts playing. This is useful for initialising/updating UI
     * Components accordingly.
     */
    @Override
    public void onTrackStarted() {
        Log.d(TAG, "onTrackStarted");
        mIsTrackFinished = false;
        // Start the timer to update the seekbar.
        initMusicControls();
        startPollingTimer();
        dismissProgressDialog();

        // Start downloading tracks in background.
        // Implicit intents with startService are not safe:
        // Warning is showing while playing song so change it this code
        Intent intent = new Intent(CCMPlayer.this, CCMCachingService.class);
        intent.setAction(CCMCachingService.ACTION_DOWNLOAD_TRACK);
        startService(intent);

    }

    /**
     * Dismisses the {@link #mProgressDialog}
     */
    void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    /**
     * {@link CCMListener} callback. {@link CCMService} calls this callback API
     * when track playing gets completed. This is useful for
     * initialising/updating UI Components accordingly.
     */
    @Override
    public void onTrackCompleted() {
        Log.d(TAG, "onTrackCompleted");
        //This will cancel the image downloading part if any error occurs
        if (mGetAlbumArtAsync != null) {
            mGetAlbumArtAsync.cancel(true);
        }
        mIsTrackFinished = true;
        // remove the listener.
        if (mService != null) {
            mService.removeCCMListener();
        }
        //setDefaultAlbumArt();
        stopPollingTimer();
        resetMusicControls();
        // Try loading nextTrack.
        getCommand(false);
    }

    /**
     * Called from {@link CCMListener#onTrackStarted()} callback API to update
     * the Media controls in {@link CCMPlayer} screen.
     */
    void initMusicControls() {
        Log.d(TAG, "Into initControls()");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mService != null) {
                    mTrackInfo.setText(mService.getSongName());
                    mTrackArtist.setText(mService.getArtistName());
                }
                mPlayTrack.setVisibility(View.GONE);
                mPauseTrack.setVisibility(View.VISIBLE);
                mTrackSeek.setProgress(0);
            }
        });
    }

    /**
     * Called from {@link CCMListener#onTrackCompleted()} callback API to update
     * the Media controls in {@link CCMPlayer} screen.
     */
    void resetMusicControls() {
        Log.d(TAG, "Into resetMusicControls()");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackCurrentDuration.setVisibility(View.INVISIBLE);
                mTrackTotalDuration.setVisibility(View.INVISIBLE);
                mPlayTrack.setVisibility(View.VISIBLE);
                mPauseTrack.setVisibility(View.GONE);
                mTrackSeek.setProgress(0);
            }
        });
    }

    /**
     * Called from {@link CCMListener#onTrackPaused()} callback API to update
     * the Media controls in {@link CCMPlayer} screen.
     */
    void pauseMusicControls() {
        Log.d(TAG, "Into pauseControls()");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayTrack.setVisibility(View.VISIBLE);
                mPlayTrack.setEnabled(true);
                mPauseTrack.setVisibility(View.GONE);
            }
        });
    }

    /**
     * {@link CCMListener} callback. {@link CCMService} calls this callback API
     * when track playing get paused. This is useful for initialising/updating
     * UI Components accordingly.
     */
    @Override
    public void onTrackPaused() {
        Log.d(TAG, "Into onTrackPaused()");
        if (mServicePollTimer != null) {
            mServicePollTimer.cancel();
        }
        pauseMusicControls();
    }

    /**
     * {@link CCMListener} callback. {@link CCMService} calls this callback API
     * when track playing get resumed from pause state.This is useful to update the UI accordingly.
     */
    @Override
    public void onTrackResumed() {
        Log.d(TAG, "onTrackResumed");
        // Start the timer to update the seekbar.
        startPollingTimer();
        dismissProgressDialog();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayTrack.setVisibility(View.GONE);
                mPauseTrack.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Async Task to get the command from the CCM Server. If the device is
     * inactive, it starts the activation of the registered device.
     *
     * @author Mahesh Chauhan
     *
     */
    class GetCommandTaskAsync extends
            AsyncTask<Void, String, GetCommandResponse> {
        boolean mForced = false;
        ProgressDialog mAsyncDialog;
        public GetCommandTaskAsync(boolean forced) {
            mForced = forced;
            mAsyncDialog = new ProgressDialog(CCMPlayer.this);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Into GetCommandTaskAsync onPreExecute() mIsActive : " + mIsActive + " isFinishing : " + isFinishing());
            mAsyncDialog
                    .setMessage(getString(R.string.ccm_player_get_command_progress_message));
            mAsyncDialog.setCancelable(false);
            if (mIsActive) {
                mAsyncDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected GetCommandResponse doInBackground(Void... params) {
            Log.d(TAG, "Into GetCommandTaskAsync doInBackground()");
            GetCommandResponse commandResponse = null;
            if (!ServerUtilities.isNetworkAvailable(getApplicationContext())) {
                return commandResponse;
            }
           /* try {
                ResponseData response = sendNextCommandRequest();
                if (response != null) {
                    // Check if its success or failure.
                    if (response.isSuccess()) {
                        JSONObject result = response.getResponseJson();
                        // Parse the json response.
                        commandResponse = (GetCommandResponse) JsonParser
                                .parseJsonToType(getApplicationContext(),
                                        result,
                                        new TypeToken<GetCommandResponse>() {
                                        }.getType());
                    }
                }
                return commandResponse;
            } catch (Exception e) {
                e.printStackTrace();
                return commandResponse;
            }*/
            return null;
        }

        @Override
        protected void onPostExecute(GetCommandResponse response) {

            if (mAsyncDialog!=null)
            {
                mAsyncDialog.dismiss();

            }
            String errorString = null;
            Log.d(TAG, "Into GetCommandTaskAsync onPostExecute()");
            if (response != null) {
                String status = response.getStatus();
                if (!TextUtils.isEmpty(status)) {
                    if (GetCommandResponse.COMMAND_STATUS_ACTIVE
                            .equalsIgnoreCase(status)) {
                        // Check the command now.
                        String command = response.getCommand();
                        if (!TextUtils.isEmpty(command)) {
                            if (GetCommandResponse.COMMAND_NEXT_TRACK
                                    .equalsIgnoreCase(command)) {
                                // Move to the next track or the first track
                                // only for the first time. Lets not interrupt
                                // the player in between playing track.
                                getNextTrack(mForced);
                            } else {
                                // Unknown command from server.
                                errorString = getString(R.string.ccm_player_player_undefined_message);
                            }
                        } else {
                            // Even though we didn't get any command, let try to
                            // get the track.
                            getNextTrack(mForced);
                        }
                    } else if (GetCommandResponse.COMMAND_STATUS_INACTIVE
                            .equalsIgnoreCase(status)) {

                        if (mIsFirstTime || mForceActivate) {
                            //If the player is launched first time after login,
                            //lets activate the player.
                            // activatePlayer();
                            showDialog();
                        }
                        else {
                            // Stop the CCMService if its ongoing as device has
                            // been
                            // deactivated from server.
                            stopService(new Intent(Constants.ACTION_PLAY_TRACK));
                            errorString =
                                    getString(R.string.ccm_player_player_inactive_message);
                        }
                    } else {
                        // Undefined status
                        errorString = getString(R.string.ccm_player_player_undefined_message);
                    }
                } else {
                    // Error
                    errorString = getString(R.string.ccm_player_player_undefined_message);
                }
            } else {
                // Error
                if (!ServerUtilities
                        .isNetworkAvailable(getApplicationContext())) {
                    errorString = CCMErrorHandler.getErrorMessage(
                            getApplicationContext(),
                            CCMErrorHandler.ERROR_NO_NETWORK);
                } else {
                    errorString = getString(R.string.ccm_player_player_undefined_message);
                }
            }
            if (errorString != null) {
//                showErrorMessage(errorString);
            } else {
                mRetryInterval = RETRY_NEXT_TRACK_STARTING_INTERVAL;
            }
        }
    }

    /**
     * Async Task to get the next track. Once a response is received and it
     * contains valid {@link Song} or {@link Message}, {@link CCMService} will
     * be started to play the track.<br>
     * Apart from playing track, if there is any album art url,
     * {@link GetAlbumArtTask} is started to downloaded the image. Once
     * downloaded, it will be displayed on the UI.
     *
     * @author Mahesh Chauhan
     *
     */

    class GetNextTrackTaskAsync extends AsyncTask<Void, Void, Void> {
        ErrorResponse mErrorResponse;
        boolean mForced = false;
        boolean mIsPlaylistEmpty = false;
        ProgressDialog mAsyncDialog;

        public GetNextTrackTaskAsync(boolean forced) {
            mForced = forced;
            mAsyncDialog = new ProgressDialog(CCMPlayer.this);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Into GetNextTrackTaskAsync onPreExecute() mIsActive : " + mIsActive + " isFinishing : " + isFinishing());
            // when screen is locked that time it is giving error
            // show method is called from back ground thread for that purpose
            // check isFinishing
            if (!isFinishing()) {
                mAsyncDialog
                        .setMessage(getString(R.string.ccm_player_get_next_track_progress_message));
                mAsyncDialog.setCancelable(false);
                if (mIsActive) {
                    mAsyncDialog.show();
                }
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Into GetNextTrackTaskAsync doInBackground()");
            if (!ServerUtilities.isNetworkAvailable(getApplicationContext())) {
                return null;
            }
            /*try {
                // First of all check, if we have some cache Song in memory.
                mTrack = CCMApplication.getCachedTrack();
                if (mTrack == null) {
                   // ResponseData response = sendNextTrackRequest();
                    if (response != null) {
                        JSONObject result = response.getResponseJson();
                        // Check if its success or failure.
                        if (response.isSuccess()) {
                            // Lets check if there is any error like no song in
                            // playlist. CHeck the meta data first.
                            MetaData meta = (MetaData) JsonParser
                                    .parseJsonToType(getApplicationContext(),
                                            result,
                                            new TypeToken<MetaData>() {
                                            }.getType());
                            if (!TextUtils.isEmpty(meta.getError())
                                    && GetTrackResponse.TRACK_ERROR_NO_MUSIC
                                            .equalsIgnoreCase(meta.getError())) {
                                mIsPlaylistEmpty = true;
                                return null;
                            } else {
                                // Parse the json response.
                                GetTrackResponse trackResponse = (GetTrackResponse) JsonParser
                                        .parseJsonToType(
                                                getApplicationContext(),
                                                result,
                                                new TypeToken<GetTrackResponse>() {
                                                }.getType());
                                if (trackResponse != null) {
                                    Song song = trackResponse.getSong();
                                    Message message = trackResponse
                                            .getMessage();
                                    if (song != null) {
                                        // Its a song.
                                        setMediaID(song.getMediaId());
                                        mTrack = new Track();
                                        mTrack.setTitle(song.getName());
                                        SongUri songUri = song.getSonguri();
                                        if (songUri != null) {
                                            mTrack.setSongUri(songUri);
                                            String oggTrack = songUri.getOgg();
                                            if (!TextUtils.isEmpty(oggTrack)) {
                                                // Also load the art album.
                                                Album album = trackResponse
                                                        .getAlbum();
                                                mTrack.setAlbum(album);
                                                Artist artist = trackResponse
                                                        .getArtist();
                                                mTrack.setArtist(artist);
                                            }
                                        }
                                    } else if (message != null) {
                                        // Its a message preset.
                                        mTrack = new Track();
                                        mTrack.setTitle(message.getName());
                                        mTrack.setMessage(true);
                                        // Its not a song, but a recorded
                                        // message in mp3 format.
                                        MessageUri messageUri = message
                                                .getMessageUri();
                                        if (messageUri != null) {
                                            mTrack.setMessageUri(messageUri);
                                        }
                                    }
                                }
                                if(mTrack != null) {
                                    // download the track.
                                    String filePath="http://tudm003.appspot.com/1.mp3";
                                  *//*  String filePath = downloadTrackRequest(mTrack
                                            .isMessage() ? mTrack
                                            .getMessageUri().getMp3() : mTrack
                                            .getSongUri().getOgg(), CCMUtils.removeSpecialCharacters(mTrack.getTitle()));
                                  *//*  if (!TextUtils.isEmpty(filePath)) {
                                        mTrack.setSongPath(filePath);
                                    } else {
                                        mTrack = null;
                                    }
                                }
                            }
                        } else {
                            mErrorResponse = (ErrorResponse) JsonParser
                                    .parseJsonToType(getApplicationContext(),
                                            result,
                                            new TypeToken<ErrorResponse>() {
                                            }.getType());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mTrack = null;
            }*/
            return null;
        }

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(Void response) {
            if (mIsActive && mAsyncDialog != null && mAsyncDialog.isShowing()) {
                mAsyncDialog.dismiss();
                mSkipBtnControl=true;
            }
            Log.d(TAG, "Into GetNextTrackTaskAsync onPostExecute()");
            if (mTrack != null) {
                if (!mTrack.isMessage() && mForced) {
                    ++CCMApplication.sSkipCount;
                }
                mRetryInterval = RETRY_NEXT_TRACK_STARTING_INTERVAL;
                mService = CCMService.getInstance();
                if (mService != null) {
                    // Check if we were able to successfully download the track.
                    if (!TextUtils.isEmpty(mTrack.getSongPath())) {
                        mService.setSong(mTrack);
                        startMusic();
                        if (mTrack.getAlbum() != null) {
                            loadAlbumArt(mTrack.getAlbum().getThumbnail());
                            mTrackInfo.setText(mTrack.getTitle() + "("
                                    + mTrack.getAlbum().getName() + ")");
                            if (mTrack.getArtist() != null) {
                                mTrackArtist.setText(mTrack.getArtist()
                                        .getName());
                            }
                        }
                    } else {
                        // Lets have a next track.
                        getCommand(false);
                        return;
                    }
                } else {
                    // If service is not started yet, lets start the
                    // service again and get the track again.
                    startService(new Intent(CCMPlayer.this, CCMService.class));
                    getCommand(false);
                    return;
                }
            } else if (mErrorResponse != null) {
                if (GetTrackResponse.COMMAND_STATUS_INACTIVE
                        .equalsIgnoreCase(mErrorResponse.getCode())) {
                    showErrorMessage(getString(R.string.ccm_player_player_inactive_message));
                } else if(Constants.NOT_FOUND_ERROR.equalsIgnoreCase(mErrorResponse.getCode()))
                {
                    // show error message when day part is not active on the day
                    showErrorMessage(getResources().getString(R.string.ccm_player_set_playlist));

                }else{
                    // Undefined error, show error.
                    showErrorMessage(getString(R.string.ccm_player_player_undefined_message));
                }
            } else {
                String error = getString(R.string.ccm_player_player_undefined_message);
                if (mIsPlaylistEmpty) {
                    error = getString(R.string.ccm_player_player_empty_playlist_message);
                    showErrorMessage(error);
                } else {
                    if (!ServerUtilities
                            .isNetworkAvailable(getApplicationContext())) {
                        error = CCMErrorHandler.getErrorMessage(
                                getApplicationContext(),
                                CCMErrorHandler.ERROR_NO_NETWORK);
                    }
                    showErrorMessage(error);
                }
            }
            if (mAsyncDialog != null && mAsyncDialog.isShowing()) {
                mAsyncDialog.dismiss();
                if (CCMApplication.sSkipCount >= Constants.CCM_MAX_SKIP_LIMIT) {
                    mNextTrack.setClickable(false);
                    mSkipTrackView.setEnabled(false);
                    if(CCMApplication.isSkipMessageShown){
                        CCMApplication.isSkipMessageShown=false;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                showToast(getResources().getString(R.string.ccm_reached_max_limit_for_skip_track));
                            }
                        }, 2000);
                        //showToast(getResources().getString(R.string.ccm_reached_max_limit_for_skip_track));
                    }

                }
            }
        }
    }

    /**
     * Async Task to download the Album Art.
     *
     * @author Mahesh Chauhan
     *
     */
    private class GetAlbumArtTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Log.d(TAG, "GetAlbumArtTask doInBackground() urldisplay : "
                    + urldisplay);
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bitmap == null) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeResource(getResources(),
                            R.drawable.ccm_no_album_art, options);
                } catch (Exception ae) {
                    System.gc();
                    System.out.println("Out of memory in bitmap");

                }
            }
            return bitmap;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(Bitmap result) {
        /*    Log.d(TAG, "GetAlbumArtTask onPostExecute() result : " + result);


            BitmapDrawable drawable = new BitmapDrawable(getResources(), result);
            CCMUtils.setViewBackgroundDrawable(mAlbumArt, drawable);
            // Also give it to service so that it can be updated to Notification
            // as well.
            if (mService != null) {
            	//updateAlbumArt(false);
                mService.setAlbumArt(result);
            }
        */
        }
    }

    /**
     * Sends a {@link HttpGet} request to get the next track.
     *
     * @return {@link ResponseData}
     * @throws CCMException
     */
    /*protected ResponseData sendNextTrackRequest() throws CCMException {
        Log.d(TAG, "Into sendNextTrackRequest() method");
        Context context = getApplicationContext();
        return ServerUtilities.getInstance(context).sendGetRequest(
                Constants.NEXT_TRACK_API + getToken());
    }*/


    /**
     * Sends a {@link HttpGet} request to get the next command.
     *
     * @return {@link ResponseData}
     * @throws CCMException
     */
    /*protected ResponseData sendNextCommandRequest() throws CCMException {
        Log.d(TAG, "Into sendNextCommandRequest() method");
        Context context = getApplicationContext();
        return ServerUtilities.getInstance(context).sendGetRequest(
                Constants.NEXT_COMMAND_API + getToken());
    }*/

    /**
     * Sends a {@link HttpPut} request to activate the registered device.
     *
     * @return {@link ResponseData}
     * @throws CCMException
     */
    protected ResponseData sendActivatePlayerRequest() throws CCMException {
        Log.d(TAG, "Into sendActivatePlayerRequest() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(
                Constants.PARAM_DEVICE_AUTH_TOKEN, getToken()));
        return ServerUtilities.getInstance(context).sendPutRequest(
                Constants.ACTIVATE_PLAYER_API, nameValuePairs);
    }

    /**
     * Sends a {@link HttpPost} request to signout the user.
     *
     * @return {@link ResponseData}
     * @throws CCMException
     */
    protected ResponseData sendSignOutRequest() throws CCMException {
        Log.d(TAG, "Into sendSignOutRequest() method");
        Context context = getApplicationContext();
        SharedPreferences mPref = getSharedPreferences(Constants.CLOUD_COVER_PLAYER_PREFERENCE,
                Context.MODE_PRIVATE);
        String username = mPref.getString(Constants.PARAM_USERNAME, "");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_UUID,
                CCMUtils.getUUID(getApplicationContext(), username).toString()));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_DEVICE_AUTH_TOKEN,
                getToken()));
        return ServerUtilities.getInstance(context).sendPostRequest(
                Constants.DEACTIVATE_USER_API, nameValuePairs);
    }

    protected ResponseData sendRemoveSongRequest() throws CCMException{

        Log.d(TAG, "Into sendRemoveSongRequest() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(
                Constants.PARAM_DEVICE_AUTH_TOKEN, getToken()));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_CUSTID,
                getCustomerId() + ""));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_MEDIA_ID,
                gettMediaID() + ""));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_REASON,
                mReason));

        return ServerUtilities.getInstance(context).sendPostRequest(
                Constants.REMOVE_SONG_API, nameValuePairs);
    }

    /**
     * Sends a request to download the track.
     *
     * @return {@link ByteArrayBuffer}
     * @throws CCMException
     */
    /*protected String downloadTrackRequest(String url, String fileName)
            throws CCMException {
        Log.d(TAG, "Into downloadTrackRequest method");
        Context context = getApplicationContext();
        return ServerUtilities.getInstance(context).downloadResource(context,
                url + Constants.TOKEN_SUFFIX + getToken(), fileName);
    }*/

    /**
     * Updates the Translucent layout to update the error.
     *
     * @param text
     *            Error message to be displayed.
     */
    void showErrorMessage(String text) {
        Log.d(TAG, "Into showErrorMessage method");
        dismissProgressDialog();
        stopPollingTimer();
//        updateActionPanel(true);
        //This will cancel the image downloading part if any error occurs
        if (mGetAlbumArtAsync != null) {
            mGetAlbumArtAsync.cancel(true);
        }
        // Also till that time show a Idle Album Art
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                //updateAlbumArt(true);
            }
        }, 1500);



        // remove the listener.
        if (mService != null) {
            mService.removeCCMListener();
            mService.cancelNotification(getApplicationContext(),
                    CCMService.NOTIFICATION_ID);
            // Stop the service.
            stopService(new Intent(Constants.ACTION_PLAY_TRACK));
        }
        resetMusicControls();
        clearNotification();
        if (text.equals(getString(R.string.ccm_player_player_undefined_message))) {
            mTrackInfo.setText(R.string.ccm_player_error_playing_track_message);
            mTrackInfo.invalidate();
            mTrackArtist
                    .setText(getString(R.string.ccm_player_error_playing_track_message_detail));
            mTrackArtist.invalidate();

        } else if (text
                .equals(getString(R.string.ccm_player_error_playing_track_message))) {

            mTrackInfo.setText(text);
            mTrackArtist
                    .setText(getString(R.string.ccm_player_error_playing_track_message_detail));

        } else if (text
                .equals(getString(R.string.ccm_player_player_inactive_message))) {
            // check if player is inactive and delete the cache directory
            mTrackInfo.setText(text);
            mTrackArtist.setText("");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    clearCache();
                }
            }).start();
        }else if(text.equals(getString(R.string.error_network_unavailable))){
            mTrackInfo.setText(text);
            mTrackArtist.setText(getString(R.string.error_network_unavailable_subtitle));
        }
        else {
            mTrackInfo.setText(text);
            mTrackArtist.setText("");
        }

        // Disabled the button if error occurs
        updateControlButton(false);
        // Also till that time show a Idle Album Art
        //updateAlbumArt(true);
        CCMApplication.sIsError = true;
        if(mRetryingTimer == null) {
            mRetryingTimer = new Timer();
        }
        mRetryingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Retrying............ " + CCMApplication.sIsError);
                        if (CCMApplication.sIsError) {
                            getCommand(false);
                        }
                    }
                });
            }
        }, mRetryInterval);
    }

    /**
     *
     * Starts the {@link GetNextTrackTaskAsync}
     *
     * @param forced
     *            skipped forcefully by user
     */
    public void getNextTrack(boolean forced) {

        if (mNextTrackAsync != null) {
            mNextTrackAsync.cancel(true);
            mNextTrackAsync = null;
        }
        if(mGetCommandAsync != null) {
            mGetCommandAsync.cancel(true);
            mGetCommandAsync = null;
        }
        //Remove notification when we search for next track
        if (mService != null) {
            clearNotification();
        }
        updateTime();
        mNextTrackAsync = new GetNextTrackTaskAsync(forced);
        mNextTrackAsync.execute();
    }

    void getNextTrack() {
        getNextTrack(false);
    }

    /**
     * Update time to initial state
     */
    private void updateTime() {
        if (mTrackCurrentDuration != null && mTrackTotalDuration != null) {
            mTrackCurrentDuration.setText("0:00");
            mTrackTotalDuration.setText("0:00");
        }
    }
    /**
     * Starts the {@link GetCommandTaskAsync} to check the next command.
     */
    void getCommand(boolean forced) {
        if (mGetCommandAsync != null) {
            mGetCommandAsync.cancel(true);
            mGetCommandAsync = null;
        }
        if(mNextTrackAsync != null) {
            mNextTrackAsync.cancel(true);
            mNextTrackAsync = null;
        }
//        setDefaultAlbumArt();
        mGetCommandAsync = new GetCommandTaskAsync(forced);
        mGetCommandAsync.execute();
    }

    /**
     * Starts the {@link LogOutTaskAsync} to logout from server.
     */
    void logout() {
        if (mLogoutAsync != null) {
            mLogoutAsync.cancel(true);
        }
        mLogoutAsync = new LogOutTaskAsync();
        mLogoutAsync.execute();
    }

    /**
     * Starts the {@link ActivatePlayerTaskAsync} to activate the registered
     * player.
     */
    void activatePlayer() {
        if (mActivatePlayerAsync != null) {
            mActivatePlayerAsync.cancel(true);
        }
        mActivatePlayerAsync = new ActivatePlayerTaskAsync();
        mActivatePlayerAsync.execute();
    }

    /**
     * Starts {@link GetAlbumArtTask} to load the album art.
     *
     * @param imgUrl
     */
    void loadAlbumArt(String imgUrl) {
        // Check the preference if user is allowing the album art to download.
        // Default value is true.
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (pref != null
                && pref.getBoolean(Constants.PREF_KEY_DISABLE_ALBUM_ART, false)) {
            return;
        }
        Log.d(TAG, "loadAlbumArt() imgUrl : " + imgUrl);
        if (TextUtils.isEmpty(imgUrl)) {
            return;
        }
        if (mGetAlbumArtAsync != null) {
            mGetAlbumArtAsync.cancel(true);
        }
        // System.gc();
        mGetAlbumArtAsync = new GetAlbumArtTask();
        mGetAlbumArtAsync.execute(imgUrl + Constants.TOKEN_SUFFIX + getToken());
    }

    /**
     * Starts the {@link CCMService} to play the track.
     *
     * @author Rushikesh
     */
    void startMusic() {
        // Implicit intents with startService are not safe:
        // Warning is showing while playing song so change it this code
        Intent intent = new Intent(CCMPlayer.this, CCMService.class);
        intent.setAction(Constants.ACTION_PLAY_TRACK);
        startService(intent);

        if (mService != null) {
            mService.setCCMListener(CCMPlayer.this);
        }
        // enable controls if everything goes well
        updateActionPanel(true);
        updateControlButton(true);
        showInitializeTrackProgress();
    }

    /**
     * Shows the {@link #mProgressDialog} for the track initialization.
     */
    void showInitializeTrackProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                    mProgressDialog
                            .setMessage(getString(R.string.ccm_player_track_init_progress_message));
                    if (mIsActive) {
                        mProgressDialog.show();
                    }
                }
            }
        });
    }

    /**
     * Stops playing track. Stops the polling timer as well as
     * {@link CCMService}.
     */
    void stopMusic() {
        stopPollingTimer();
        stopService(new Intent(Constants.ACTION_PLAY_TRACK));
    }

    /**
     * Reinitialize the player controls.
     */
    void reinitialiseControls() {
        if (mService != null) {
            mTrackInfo.setText(mService.getSongName());
            mTrackArtist.setText(mService.getArtistName());
            loadAlbumArt(mService.getAlbumArtUrl());
            mPlayTrack.setEnabled(true);
            if (mService.isPaused()) {
                mPlayTrack.setVisibility(View.VISIBLE);
                mPauseTrack.setVisibility(View.GONE);
            } else if (mService.isPlaying()) {
                mPlayTrack.setVisibility(View.GONE);
                mPauseTrack.setVisibility(View.VISIBLE);
            } else {
                // Don't know the state, lets get the next track.
                getCommand(false);
            }
            mTrackSeek.setProgress(0);
        }
    }

    @Override
    public void onPlayerError() {
        showErrorMessage(getApplicationContext().getString(
                R.string.ccm_player_error_playing_track_message));
    }

    @Override
    public void onTrackStopped() {
        // remove the listener.
        if (mService != null) {
            mService.removeCCMListener();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        mIsActive = true;
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        mIsActive = true;
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart()");
        super.onRestart();
        mIsActive = true;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }


    /**
     * Logs out of the CCM Application.
     */
    private void clearDataOnLogout() {
        clearPreferences();
        CCMApplication.isSkipMessageShown = true;
        SharedPreferences settingsPref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (settingsPref != null) {
            settingsPref.edit().clear().commit();
        }
        stopMusic();
        // Nullify the ServerUtilities instance.
        ServerUtilities.instance = null;
        CCMApplication.sSkipCount = 0;
        CCMApplication.sIsSessionOngoing = false;
        CCMApplication.sIsPhoneRinging = false;
        // Stop the retry timer.
        if(mRetryingTimer != null) {
            mRetryingTimer.cancel();
            mRetryingTimer = null;
        }
        SharedPreferences mPref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(Constants.PREF_KEY_IS_USER_SIGNOUT, true);
        editor.commit();

        CCMApplication.isSkipMessageShown = true;
        Intent launchNext = new Intent(getApplicationContext(), CCMLogin.class);
        startActivity(launchNext);
        // Finish the player activity.
        CCMPlayer.this.finish();
    }

    /**
     * Clearing the preference we want to clear except the username
     */
    private void clearPreferences() {
        SharedPreferences mPref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove(Constants.PARAM_PASSWORD);
        editor.remove(Constants.PARAM_DEVICE_AUTH_TOKEN);
        editor.remove(Constants.PREF_KEY_DISABLE_ALBUM_ART);
        editor.remove(Constants.PARAM_CUSTID);
        editor.remove(Constants.PARAM_USERID);
        editor.commit();
    }

    /**
     * Async Task to activate the registered device on the server.
     *
     * @author Mahesh Chauhan
     *
     */
    class ActivatePlayerTaskAsync extends AsyncTask<Void, Void, Void> {
        ActivatePlayerResponse mActivateResponse;
        ErrorResponse mErrorResponse;
        ProgressDialog mAsyncDialog;
        public ActivatePlayerTaskAsync() {
            mAsyncDialog = new ProgressDialog(CCMPlayer.this);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Into ActivatePlayerTaskAsync onPreExecute()");
            mAsyncDialog
                    .setMessage(getString(R.string.ccm_player_activating_player_progress_message));
            mAsyncDialog.setCancelable(false);
            mAsyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Into ActivatePlayerTaskAsync doInBackground()");
            try {
                ResponseData response = sendActivatePlayerRequest();
                if (response != null) {
                    JSONObject result = response.getResponseJson();
                    // Check if its success or failure.
                    if (response.isSuccess()) {
                        // Parse the json response.
                        mActivateResponse = (ActivatePlayerResponse) JsonParser
                                .parseJsonToType(
                                        getApplicationContext(),
                                        result,
                                        new TypeToken<ActivatePlayerResponse>() {
                                        }.getType());
                    } else {
                        mErrorResponse = (ErrorResponse) JsonParser
                                .parseJsonToType(getApplicationContext(),
                                        result, new TypeToken<ErrorResponse>() {
                                        }.getType());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            if (mIsActive && mAsyncDialog != null && mAsyncDialog.isShowing()) {
                mAsyncDialog.dismiss();
            }
            String errorString = null;
            Log.d(TAG, "Into GetCommandTaskAsync onPostExecute()");
            if (mActivateResponse != null) {
                int activateStatus = mActivateResponse.getActiveStatus();
                if (activateStatus == ActivatePlayerResponse.PLAYER_STATUS_ACTIVE) {
                    mIsFirstTime = false;
                    mForceActivate = false;
                    getCommand(false);
                    //getNextTrack(false);
                } else { // Error
                    errorString = getString(R.string.ccm_player_player_undefined_message);
                }
            } else {
                // Error
                errorString = getString(R.string.ccm_player_player_undefined_message);
            }
            if (errorString != null) {
                //updateAlbumArt(true);
                mTrackInfo
                        .setText(R.string.ccm_player_error_playing_track_message);
                mTrackArtist
                        .setText(R.string.ccm_player_error_playing_track_message_detail);

            }
        }
    }

    /**
     * Async Task to activate the registered device on the server.
     *
     * @author Mahesh Chauhan
     *
     */
    class LogOutTaskAsync extends AsyncTask<Void, Void, String> {
        ProgressDialog mAsyncDialog;
        public LogOutTaskAsync() {
            mAsyncDialog = new ProgressDialog(CCMPlayer.this);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Into LogOutTaskAsync onPreExecute()");
            mAsyncDialog
                    .setMessage(getString(R.string.ccm_player_logging_out_progress_message));
            mAsyncDialog.setCancelable(false);
            mAsyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Into LogOutTaskAsync doInBackground()");
            String error = getString(R.string.ccm_player_logout_failed_message);
            try {
                ResponseData response = sendSignOutRequest();
                if (response != null) {
                    // Check if its failure then send a message to show.
                    if (response.isSuccess()) {
                        error = null;
                        clearCache();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return error;
        }

        @Override
        protected void onPostExecute(String response) {
            if (mIsActive && mAsyncDialog != null && mAsyncDialog.isShowing()) {
                mAsyncDialog.dismiss();
            }
            if (response == null) {
                // Successfully logged out from server, clear data from device
                // and launch Login Screen again.
                clearDataOnLogout();
                //mBannerArt.removeAllViews();
            } else {
                // Error
                Toast.makeText(CCMPlayer.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    /***
     * Async task is to remove song from server play list
     */

    class RemoveAsynctask extends AsyncTask<Void, Void, String>{
        ProgressDialog mAsyncDialog;
        RemoveAsynctask(){
            mAsyncDialog=new ProgressDialog(CCMPlayer.this);
            mAsyncDialog.setCancelable(false);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAsyncDialog.setMessage(getResources().getString(R.string.ccm_player_removeing_song));
            mAsyncDialog.show();
            if(mService!=null && mService.isPlaying()){
                mService.pauseMusic();
            }
        }
        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Into RemoveAsynctask doInBackground()");
            String error = getString(R.string.ccm_player_failed_removeing_song);
            try {
                ResponseData response = sendRemoveSongRequest();
                if (response != null) {
                    // Check if its failure then send a message to show.
                    JSONObject jsonObject=response.getResponseJson();
                    Log.d(TAG, "Into RemoveAsynctask doInBackground() response"+response.getResponseJson());
                    if (response.isSuccess()) {
                        error = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return error;
        }
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (mAsyncDialog.isShowing()) {
                mAsyncDialog.dismiss();
            }
            if (response == null) {
                showToast(getResources().getString(R.string.ccm_player_sucess_removeing_song));
                updateActionPanel(true);
                //getCommand(false);
                onTrackCompleted();
            } else {
                showToast(getResources().getString(R.string.ccm_player_failed_removeing_song));
                if(mService!=null){
                    mService.startMusic();
                }
            }
        }

    }
    @Override
    public void onDrawerClosed(View arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDrawerOpened(View arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDrawerSlide(View drawerView, float offset) {
        // We need to slide the rest UI to right.
        View container = findViewById(R.id.container);
        container.setTranslationX(offset * drawerView.getWidth());
    }

    @Override
    public void onDrawerStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    private void updateSkipActionState(boolean enabled) {

        if (CCMApplication.sSkipCount >= Constants.CCM_MAX_SKIP_LIMIT) {
            mNextTrack.setClickable(false);
            mSkipTrackView.setEnabled(false);
            if(CCMApplication.isSkipMessageShown){
                CCMApplication.isSkipMessageShown=false;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        showToast(getResources().getString(R.string.ccm_reached_max_limit_for_skip_track));
                    }
                }, 1500);
                //showToast(getResources().getString(R.string.ccm_reached_max_limit_for_skip_track));
            }

        } else {

            mNextTrack.setClickable(enabled);
            mSkipTrackView.setEnabled(enabled);
        }
    }

    private void updateControlButton(final boolean enabled) {

        mPlayTrack.setClickable(enabled);
        mPlayTrackView.setEnabled(enabled);
        // you can not remove messages from server
        if (mTrack != null && mTrack.isMessage()) {
            mRemoveSong.setClickable(false);
            mRemoveSongView.setEnabled(false);
        } else {
            mRemoveSong.setClickable(enabled);
            mRemoveSongView.setEnabled(enabled);

        }
        if (CCMApplication.sSkipCount >= Constants.CCM_MAX_SKIP_LIMIT) {
            updateSkipActionState(false);
        } else {
            updateSkipActionState(enabled);
        }
        if (enabled) {
            mTrackSeekLayout.setVisibility(View.VISIBLE);
        } else {
            mTrackSeekLayout.setVisibility(View.INVISIBLE);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
            }
        }, 1500);
    }

    /* private void updateAlbumArt(boolean enable) {
         if (enable) {
             mBannerArt.startFlipping();
             mBannerArt.setVisibility(View.VISIBLE);
             mAlbumArt.setVisibility(View.GONE);
         } else {
             mBannerArt.stopFlipping();
             mBannerArt.setVisibility(View.GONE);
             mAlbumArt.setVisibility(View.VISIBLE);
         }
     }
 */
    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CCMPlayer.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPlayerStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CCMPlayer.this.finish();
            }
        });
    }

    /**
     * Cancel all notification on error
     */
    public void clearNotification() {
        // Clear all notification
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    /**
     * Deletes the cached Nodes and actual files from application directory.
     */
    public void clearCache() {
        // Clear the cached Tracks.
        CCMApplication.clearCachedTracks();
        // Delete all the download track files if any.
        File downloadedTracks = getApplicationContext().getFilesDir();
        if (downloadedTracks != null) {
            for (File f : downloadedTracks.listFiles()) {
                if (f != null) {
                    f.delete();
                }
            }
        }
    }

    private String getToken() {
        SharedPreferences pref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        String token = null;
        if (pref != null) {
            token = pref.getString(Constants.PARAM_DEVICE_AUTH_TOKEN, "");
        }
        return token;
    }

    private String getUserName(){
        SharedPreferences pref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        String userName = null;
        if (pref != null) {
            userName = pref.getString(Constants.PARAM_USERNAME, "");
        }
        return userName;
    }


    /**
     * Set default album art when we switch to new song
     * */
   /* private void setDefaultAlbumArt()
    {
    	// to display default image if song is complete 
        if(mAlbumArt.getVisibility() == View.VISIBLE)
        {
        	Bitmap bitmap = null;
        	try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ccm_no_album_art, options);
            
       BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        CCMUtils.setViewBackgroundDrawable(mAlbumArt, drawable);
        // Also give it to service so that it can be updated to Notification
        // as well.
        if (mService != null) {
        	
        	//updateAlbumArt(false);
            mService.setAlbumArt(bitmap);
        }
        } catch (Exception ae) {
            System.gc();
            System.out.println("Out of memory in bitmap");
        }
        }
    }
   */
    private void updateActionPanel(final boolean enabled){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (enabled) {

                    // to show remove, play, pause and skip actions
/*
                    if (mTrackActionPanel.getVisibility() != View.VISIBLE) {
                        mRemovSongAction.setVisibility(View.GONE);
                        mTrackActionPanel.setVisibility(View.VISIBLE);
                    }
*/

                } else {
                    // to show action after clicking on remove song option
                    if (mRemovSongAction.getVisibility() != View.VISIBLE) {
                        mTrackActionPanel.setVisibility(View.GONE);
                        mRemovSongAction.setVisibility(View.VISIBLE);
                    }
                }

            }
        });


    }
    private int getCustomerId(){
        int custId=-1;
        SharedPreferences pref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        if (pref != null) {
            custId = pref.getInt(Constants.PARAM_CUSTID,-1);
        }
        return custId;

    }
    private String getApplicationVesrion(){
        PackageInfo packageInfo=null;
        try {
            packageInfo = CCMPlayer.this.getPackageManager()
                    .getPackageInfo(CCMPlayer.this.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String appVersion = packageInfo.versionName;

        return appVersion;
    }
    public void setMediaID(int mediaID) {
        this.mediaID = mediaID;
    }
    private int gettMediaID() {
        return mediaID;
    }
    public void showDialog() {
        mDialog.setCancelable(false);
        TextView ok = (TextView) mDialog.findViewById(R.id.ccm_activate_Ok);
        TextView cancel=(TextView) mDialog.findViewById(R.id.ccm_activate_cancel);
        TextView message=(TextView) mDialog.findViewById(R.id.ccm_activate_dialog_text);
        message.setText(R.string.ccm_active_message);
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                activatePlayer();
                mDialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showErrorMessage(getString(R.string.ccm_player_player_inactive_message));
                mDialog.dismiss();
            }
        });

        if(!isDeviceActivateDialogShown){
            isDeviceActivateDialogShown=true;
            mDialog.show();
        }

    }

}
