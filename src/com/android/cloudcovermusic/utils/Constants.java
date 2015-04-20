package com.android.cloudcovermusic.utils;

/**
 * Helper class providing constants common to other classes in the app.
 */
public final class Constants {
    public static final String CLOUD_COVER_PLAYER_PREFERENCE = "cloud_cover_player_preferences";
    public static final String PARAM_DEVICE_AUTH_TOKEN = "token";
    public static final String SERVER_URL = "http://tudm003.appspot.com";
    public static final String TOKEN_SUFFIX = "?token=";
    public static final String NEXT_TRACK_API = "media/next" + TOKEN_SUFFIX;
    public static final String NEXT_COMMAND_API = "commands/next"
            + TOKEN_SUFFIX;
    public static final String MANAGE_MESSAGE="messages/presets"+TOKEN_SUFFIX;
    public static final String ME="me"+TOKEN_SUFFIX;
    public static final String NO_PRESET_MESSAGE="users//message_preset";
    public static final String DEVICE_LIST_USERS="users/";
    public static final String DEVICE_LIST_API = "/devices"+ TOKEN_SUFFIX;
    public static final String ACTIVATE_PLAYER_API_TOKEN = "activate?";
    public static final String DEACTIVATE_USER_API="deactivate/device";
    public static final String PREVIOUS_SONG="previous/songs";
    public static final String ACTIVATE_PLAYER_API = "activate";

    public static final String LOGIN_API = "login";
    public static final String TOKEN_API = "token";
    public static final String REGISTER_DEVICE_API = "devices";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_FORGOT_PWD = "forgot_password";
    public static final String PARAM_DEVICE_UUID = "device_uuid";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_UUID = "uuid";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_CUSTID = "custid";
    public static final String PARAM_USERID = "userid";
    public static final String PARAM_DEVICEID = "deviceid";
    public static final String PARAM_LAST_SEEN = "last_seen";
    public static final String PARAM_ACTIVATE_PLAYER = "active_player";
    public static final String PARAM_ISDELETED = "isDeleted";
    public static final String PARAM_DEVICE_NAME = "name";
    public static final String PARAM_OFFSET="&offset=";
    public static final String PARAM_OFFSET_VALUE="0";
    public static final String PARAM_LIMIT="&limit=";
    public static final String PARAM_LIMIT_VALUE="20";
    public static final String ACTION_PLAY_TRACK = "com.android.cloudcovermusic.PLAY_TRACK";
    public static final String CCM_BOX_MANUFACTURER_PREFIX = "Wonder";
    public static final String CCM_BOX_MANUFACTURER_SIMULATION = "Simulating WonderMedia";
    public static final String CCM_DEBUG_MODE_CREDENTIALS = "debug";
    public static final String INTENT_EXTRA_APP_RECOVERING_FROM_CRASH = "isApplicationRecovering";
    public static final String INTENT_EXTRA_IS_LOGIN_SETTINGS = "isLoginSettings";
    public static final String PREF_KEY_DISABLE_ALBUM_ART = "prefDisableAlbumArt";
    public static final String PREF_KEY_SERVER_DOMAIN = "prefServerDomain";
    public static final String INTENT_EXTRA_LAUNCH_FIRST_TIME = "launchFirstTime";
    public static final String INTENT_EXTRA_FORCE_ACTIVATE = "forceActivate";
    public static final String INTENT_EXTRA_DOWNLOADED_TRACK = "downloadedTrack";
    public static final String INTENT_EXTRA_NEED_HELP_TYPE = "needHelpType";
    public static final int CCM_MAX_SKIP_LIMIT = 6;
    public static final String LOGIN_USERNAME = "login_username" ;
    public static final String LOGIN_PASSWORD = "login_password" ;
    public static final String FIRST_INSTALL = "first_install" ;
    public static final String REMOVE_SONG_API = "media_removed";
    public static final String PARAM_MEDIA_ID="mediaid";
    public static final String PARAM_REASON="reason";
 // Notification Id for the notification to be displayed.
    public static final int NOTIFICATION_ID = 1;
    public static final String PARAM_REMOVE_SONG_REASON_INAPPOPRIATE="BadLanguage";
    public static final String PARAM_REMOVE_SONG_REASON_WRONG_STATION="WrongPlaylist";
    public static final String PARAM_REMOVE_SONG_REASON_DISLIKE="DontLikeSong";
    public static final String PREF_KEY_IS_USER_SIGNOUT = "isUserSignOut";
    public static final String CCM_ANDROID_TERMS = "terms";
    public static final String CCM_ANDROID_PRIVACY = "privacy";
    public static final String PARAM_MESSAGE_PRESET = "message_preset=";
    public static final String PARAM_USERS = "users/";
	public static String PARAM_MESSAGE_PRESET_SUFFIX="/message_preset";
	public static String NOT_FOUND_ERROR="NotFoundError";
	public static String MESSAGE_SUBSCRIPTION="message_subscription";
	public static String PRESETS="presets";
	
}
