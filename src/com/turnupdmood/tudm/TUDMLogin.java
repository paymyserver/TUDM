package com.turnupdmood.tudm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.tudm.json.JsonParser;
import com.android.tudm.model.ErrorResponse;
import com.android.tudm.model.LoginResponse;
import com.android.tudm.utils.TUDMErrorHandler;
import com.android.tudm.utils.TUDMException;
import com.android.tudm.utils.TUDMUtils;
import com.android.tudm.utils.Constants;
import com.android.tudm.utils.ServerUtilities;
import com.android.tudm.utils.ServerUtilities.ResponseData;
import com.google.gson.reflect.TypeToken;

/**
 * {@link TUDMLogin} is the decision making activity of the
 * {@link TUDMApplication}.<br>
 * When first started, it first checks whether the user is already logged in or
 * not. If not, it provides user the UI to login to CCMServer.<br>
 * Once successfully logged in, it redirects user to {@link TUDMPlayer} activity
 * which plays the tracks/songs once the player is activated.
 *
 * @author Mahesh Chauhan
 *
 */
public class TUDMLogin extends Activity {
    private static final String TAG = TUDMLogin.class.getSimpleName();
    // Holds the token received from CCM Server. Once user logged in and
    // registers the device successfully we received that token. For all the
    // subsequent call, this token will be used.
    private String mAuthToken;
    // Used to access application shared preferences.
    private SharedPreferences mPref = null;
    private EditText mUserNameView;
    private EditText mPasswordView;
    private TextView mErrorTextView;
    private Button mSignIn;
    private Button mRegister;
    private LinearLayout mHelp;
    private LinearLayout mForgotPwd;
    private TextView mBottomForgotText,mBottomNeedHelpText;
 // add custom font for text view
    Typeface mRobotoRegular, mRobotoBold;
    /**
     * Boolean to keep track whether a only authentication is needed or not. If
     * true, only /login API will be called to validate the user credentials
     * else /login and then /token both api will be called to validate the
     * credentials and to get the token.
     */
    private boolean mOnlyAuthenticate = false;
    public String deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = getSharedPreferences(Constants.CLOUD_COVER_PLAYER_PREFERENCE,
                Context.MODE_PRIVATE);
        if (mPref != null) {
            mAuthToken = mPref.getString(Constants.PARAM_DEVICE_AUTH_TOKEN, "");
            Log.d(TAG, "onCreate() mAuthToken : " + mAuthToken);
        }
        if (!TextUtils.isEmpty(mAuthToken) && TUDMApplication.sIsSessionOngoing) {
            // This is the case when user is already logged in the cloud cover
            // Music player and have the auth-token and also the current session
            // is ongong. So just start playing the
            // music. :)
            launchMainActivity(false, false);
            return;
        }

        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.tudm_login_screen_layout);
        mUserNameView = (EditText) findViewById(R.id.ccmUsername);
        mPasswordView = (EditText) findViewById(R.id.ccmPassword);
        String username = mPref.getString(Constants.PARAM_USERNAME, "");
        if (!TextUtils.isEmpty(mAuthToken) && !TUDMApplication.sIsSessionOngoing) {
            // This is a case when the user has a valid token but the session
            // expired. Lets only re-authenticate .
            mOnlyAuthenticate = true;
            if (!TextUtils.isEmpty(mAuthToken)) {
                mUserNameView.setEnabled(false);
                mUserNameView.setText(username);
            }
        } else {
            mOnlyAuthenticate = false;
            mUserNameView.setEnabled(true);
            mUserNameView.setText(username);
        }
        mSignIn = (Button) findViewById(R.id.ccmSignIn);
        mRegister=(Button)findViewById(R.id.ccmRegister);
        mHelp = (LinearLayout) findViewById(R.id.ccmHelp);
        mForgotPwd = (LinearLayout) findViewById(R.id.ccmForgotpwd);
        mErrorTextView = (TextView) findViewById(R.id.ccmErrorLabel);
        mBottomForgotText = (TextView) findViewById(R.id.ccmForgotpwdText);
        mBottomNeedHelpText = (TextView) findViewById(R.id.ccmNeedHelpText);
        /**
         * Adding font to textview and edittext
         */
        mRobotoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Regular.ttf");
        mRobotoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Bold.ttf");
        mErrorTextView.setTypeface(mRobotoRegular);
        mUserNameView.setTypeface(mRobotoRegular);
        mPasswordView.setTypeface(mRobotoRegular);
        mSignIn.setTypeface(mRobotoRegular);
        mRegister.setTypeface(mRobotoRegular);
        mBottomForgotText.setTypeface(mRobotoRegular);
        mBottomNeedHelpText.setTypeface(mRobotoRegular);
        /**
         * This input filter is given to avoid space which is entered in
         * username and password
         *
         * @author Sourabh
         * */
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                    Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isSpaceChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        mUserNameView.setFilters(new InputFilter[]{filter});
        mPasswordView.setFilters(new InputFilter[]{filter});
        initializeListeners();
        // Check for the updates from AppBlade server.
        //AppBlade.checkForUpdates(CCMLogin.this, true);

        if(getResources().getString(R.string.screen_type).equals("Android Tablet"))
        {
        	deviceType="Android Tablet";
        }
        else
        {
        	deviceType="Android Phone";
        }
    }

    private void initializeListeners() {
        mSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // launchMainActivity(true, true);
            	String username = mUserNameView.getText().toString();
                String password = mPasswordView.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    String error = getString(R.string.field_missing_msg);
                    mErrorTextView.setText(error);
                } else if (username.equalsIgnoreCase("debug")
                        && password.equalsIgnoreCase("debug")) {
                	if(TUDMUtils.isDevelopmentAppVersion(getApplicationContext()) || TUDMUtils.checkDebugFile())
                	{
                    // Also clear the username and password.
                    mUserNameView.setText("");
                    mPasswordView.setText("");
                    launchSettingsActivity();
                	}
                } else {
                    // Lets login to the server.
                	if(ServerUtilities.isNetworkAvailable(TUDMLogin.this))
                	{
                    loginUser();
                	}else{
                	mErrorTextView.setText(getString(R.string.error_network_unavailable));
                	}
                }
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent launchRegister = new Intent(getApplicationContext(), Register_User.class);
                launchRegister.putExtra(Constants.INTENT_EXTRA_NEED_HELP_TYPE, TUDMHelp.NEED_HELP_TYPE_LOGIN);
                startActivity(launchRegister);
            }
        });

        mHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchHelp = new Intent(getApplicationContext(), TUDMHelp.class);
                launchHelp.putExtra(Constants.INTENT_EXTRA_NEED_HELP_TYPE, TUDMHelp.NEED_HELP_TYPE_LOGIN);
                startActivity(launchHelp);
            }
        });

        mForgotPwd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent launchForgotPwd = new Intent(getApplicationContext(),
                        TUDMForgotPwd.class);
                startActivity(launchForgotPwd);
            }
        });

    }

    /**
     * Launches the {@link TUDMSettingActivity} screen which let developer/user
     * to change some developer settings.
     */
    void launchSettingsActivity() {
        Intent launchNext = new Intent(getApplicationContext(),
                TUDMSettingActivity.class);
        startActivity(launchNext);
    }

    /**
     * Launches the {@link TUDMPlayer} screen which will start playing the track
     * if the player is already activated.<br>
     * Else will let user to activate the player.
     *
     * @param firstTime
     *            Suggests whether the {@link TUDMPlayer} is launching first time
     *            after login.
     */
    void launchMainActivity(boolean firstTime, boolean forceActivation) {
        Intent launchNext = new Intent(getApplicationContext(), TUDMPlayer.class);
        launchNext.putExtra(Constants.INTENT_EXTRA_LAUNCH_FIRST_TIME, firstTime);
        launchNext.putExtra(Constants.INTENT_EXTRA_FORCE_ACTIVATE, forceActivation);
        startActivity(launchNext);
        // Finish the login activity.
        finish();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        hideKeyboard();
    }

    private void hideKeyboard()
    {
        if (this.mPasswordView != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.mPasswordView.getWindowToken(), 0);
        }
    }

    /**
     * API which will get called when user clicks the Sign In button.
     */
    public void loginUser() {
        new SignInTaskAsync().execute();
    }

    /**
     * {@link AsyncTask} to perform the Sign In operation. It takes the username
     * and password and tries to login to the server.
     *
     * @author Mahesh Chauhan
     *
     */
    class SignInTaskAsync extends AsyncTask<Void, String, String> {
        String mUsername = null;
        String mPassword = null;
        String mUserAuthenticationToken = null;
        boolean mForceActivation = false;
        ProgressDialog asyncDialog = new ProgressDialog(TUDMLogin.this);
        // previously logedin user name
        String mLogedInUserName="";
        public SignInTaskAsync() {
            mLogedInUserName=mPref.getString(Constants.PARAM_USERNAME, "");
            mUsername = mUserNameView.getText().toString();
            mPassword = mPasswordView.getText().toString();
        }

        @Override
        protected void onPreExecute() {
            asyncDialog
                    .setMessage(getString(R.string.sign_in_progress_message));
            asyncDialog.setCancelable(false);
            asyncDialog.show();
            mErrorTextView.setText("");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String sRet = getString(R.string.login_failed);
            Context context = getApplicationContext();
            if(mOnlyAuthenticate) {
                // We will just check the username and password here locally and
                // if matched, lets user into the application.
                String storedPassword = mPref.getString(
                        Constants.PARAM_PASSWORD, "");
                if (!TextUtils.isEmpty(storedPassword)
                        && !TextUtils.isEmpty(mPassword)
                        && mPassword.equals(storedPassword)) {
                    return null;
                } else {
                    return context.getResources().getString(
                            R.string.error_msg_incorrect_username_or_password);
                }
            }
            // Check Internet connectivity before making a login call.
            int strId = -1;
            if (!ServerUtilities.isNetworkAvailable(context)) {
                strId = TUDMErrorHandler.ERROR_NO_NETWORK;
                return TUDMErrorHandler.getErrorMessage(context, strId);
            }

            try {
                ResponseData signInResponse = sendSignInRequest(mUsername,
                        mPassword);
                if (signInResponse != null) {
                    // Check if its success or failure.
                    if (signInResponse.isSuccess()) {
                        launchMainActivity(true,true);
                        // Get the auth Token
                        /*JSONObject responseJson = signInResponse
                                .getResponseJson();
                        LoginResponse loginResponse = (LoginResponse) JsonParser
                                .parseJsonToType(context, responseJson,
                                        new TypeToken<LoginResponse>() {
                                        }.getType());
                        if (loginResponse != null) {
                            // Try to register the device here.
                            mAuthToken = loginResponse.getToken();
                            int custId = loginResponse.getCustId();
                            int userId = loginResponse.getUserId();
                            ResponseData registerDeviceResponse = sendRegisterDeviceRequest(
                                    mAuthToken, custId,
                                    userId, mUsername);
                            //
                            Editor edit = mPref.edit();
                            edit.putInt(Constants.PARAM_CUSTID,
                            		custId);
                            edit.putInt(Constants.PARAM_USERID,
                            		userId);
                            edit.commit();
                            if (registerDeviceResponse.isSuccess()) {
                                Editor editor = mPref.edit();
                                editor
                                        .putString(
                                                Constants.PARAM_DEVICE_AUTH_TOKEN,
                                                mAuthToken);
                                editor.putString(
                                        Constants.PARAM_USERNAME,
                                        mUsername);
                                editor.putString(
                                        Constants.PARAM_PASSWORD,
                                        mPassword);
                                editor.commit();
                                sRet = null;
                                // This is to make sure we will activate the
                                // player forcefully as this device is not yet
                                // been registered with this user earlier.
                                mForceActivation = true;
                        */    } else {
                                // Check if its a "DuplicateError" then we will ignore it and continue.
                               /* ErrorResponse errorResponse = (ErrorResponse) JsonParser
                                        .parseJsonToType(
                                                context,
                                                registerDeviceResponse
                                                        .getResponseJson(),
                                                new TypeToken<ErrorResponse>() {
                                                }.getType());
                                if (errorResponse != null
                                        && errorResponse.getCode() != null
                                        && errorResponse
                                                .getCode()
                                                .equalsIgnoreCase(
                                                        TUDMErrorHandler.ERROR_DUPLICATE_DEVICE)) {
                                    Editor editor = mPref.edit();
                                    editor.putString(
                                            Constants.PARAM_DEVICE_AUTH_TOKEN,
                                            mAuthToken);
                                    editor.putString(
                                            Constants.PARAM_USERNAME,
                                            mUsername);
                                    editor.putString(
                                            Constants.PARAM_PASSWORD,
                                            mPassword);
                                    editor.commit();
                                    sRet = null;
                                } else {
                                    // Its a failure.
                                	if(TUDMUtils.isDevelopmentAppVersion(getApplicationContext()))
                                	{
                                	sRet = registerDeviceResponse.getResponseJson().getString("message");
                                	return sRet;
                                	}
                                    return parseResult(context,
                                            registerDeviceResponse);
                                }*/
                         //   }
                      //  }
                    //} else {
                        // Its a failure.
                    	/*if(TUDMUtils.isDevelopmentAppVersion(getApplicationContext()))
                    	{
                    	sRet = signInResponse.getResponseJson().getString("message");
                    	return sRet;
                    	}
                        return parseResult(context, signInResponse);*/
                    }
                }
            } catch (TUDMException e) {
                int exceptionType = e.getExceptionType();
                // This check is needed. Since we want send message back only if
                // it is IOEXCEPTION.
                if(TUDMUtils.isDevelopmentAppVersion(getApplicationContext()))
            	{
                if (exceptionType == TUDMException.IO_EXCEPTION) {
                    sRet = TUDMErrorHandler.getErrorMessage(context,
                            exceptionType);
                }
            	}
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sRet;
        }

        @Override
        protected void onPostExecute(String errorMsg) {
            Log.d(TAG, "Into onPostExecute with status : " + errorMsg);
            // Dismiss the progress first.
            if (asyncDialog != null && asyncDialog.isShowing()) {
                asyncDialog.dismiss();
            }
            if (errorMsg == null) {
                // Also starts the user session.
                TUDMApplication.sIsSessionOngoing = true;
                // if u ser login with another user then show activation dialog in login screen
                // so mForceActivation=true
                if(!mLogedInUserName.equals(mUsername)){
                    mForceActivation=true;
                }
                launchMainActivity(true, mForceActivation);
            } else {
                // Show the error to user.
                mErrorTextView.setText(errorMsg);
                Log.d(TAG, "SignInTask error : " + errorMsg);
                if (mUserNameView.isEnabled()) {
                    mUserNameView.setText("");
                }
                mPasswordView.setText("");
            }
        }
    }

    /**
     * Sends the Sign In request to the server for the authorization of the
     * user. Once logged in, a token will be received.
     *
     * @param username which will be used to send the sign in request.
     * @param password which will be used to send the sign in request.
     * @return response
     * @throws com.android.tudm.utils.TUDMException
     */
    protected ResponseData sendSignInRequest(String username, String password)
            throws TUDMException {
        Log.d(TAG, "Into sendSignInRequest() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_USERNAME,
                username));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_PASSWORD,
                password));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_DEVICE_UUID,
                TUDMUtils.getUUID(getApplicationContext(), username)));
        return ServerUtilities.getInstance(context).sendPostRequest(
                Constants.TOKEN_API, nameValuePairs);
    }

    /**
     * Validate the credentials with the server whether the provided username is
     * correct or not.
     *
     * @param username
     *            which will be used to validate.
     * @param password
     *            which will be used to validate.
     * @return response
     * @throws com.android.tudm.utils.TUDMException
     */
    protected ResponseData validateCredentials(String username, String password)
            throws TUDMException {
        Log.d(TAG, "Into validateCredentials() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_USERNAME,
                username));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_PASSWORD,
                password));
        return ServerUtilities.getInstance(context).sendPostRequest(
                Constants.LOGIN_API, nameValuePairs);
    }

    String parseResult(Context context, ResponseData response)
            throws TUDMException {
        // If not able to get the result object, get error
        // strings on the basis of HTTP error codes.
        return TUDMErrorHandler.getErrorMessage(context,
                response.getHttpResponseCode());
    }

    /**
     * Tries to register the user's device to the CCM Server. Device name, type,
     * description and UUID will be require to register the device.
     *
     * @param token
     * @return
     * @throws com.android.tudm.utils.TUDMException
     */
    protected ResponseData sendRegisterDeviceRequest(String token, int custId,
            int userId, String username) throws TUDMException {
        Log.d(TAG, "Into sendRegisterDeviceRequest() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_DESCRIPTION,
                ""+ TUDMUtils.getDeviceAndSdkVersion(TUDMLogin.this)
                +" Resolution "+ TUDMUtils.getDeviceResolution(TUDMLogin.this)));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_DEVICE_NAME,
                TUDMUtils.getDeviceName()));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_TYPE,
        		deviceType));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_UUID,
                TUDMUtils.getUUID(context, username)));
        nameValuePairs.add(new BasicNameValuePair(
                Constants.PARAM_DEVICE_AUTH_TOKEN, token));
        nameValuePairs.add(new BasicNameValuePair(
                Constants.PARAM_CUSTID, String.valueOf(custId)));
        nameValuePairs.add(new BasicNameValuePair(
                Constants.PARAM_USERID, String.valueOf(userId)));
        return ServerUtilities.getInstance(context).sendPostRequest(
                Constants.REGISTER_DEVICE_API,
                nameValuePairs);
    }

}
