package com.cloudcovermusic.ccm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.cloudcovermusic.utils.CCMErrorHandler;
import com.android.cloudcovermusic.utils.CCMException;
import com.android.cloudcovermusic.utils.Constants;
import com.android.cloudcovermusic.utils.ServerUtilities;
import com.android.cloudcovermusic.utils.ServerUtilities.ResponseData;

/**
 * This class is used for forgot password api
 * 
 * @author Sourabh
 * 
 */
public class CCMForgotPwd extends Activity {
    private static final String TAG = CCMForgotPwd.class.getSimpleName();
    EditText mUsername;
    EditText mEmailId;
    Button mSubmit;
    private String mAuthToken;
    // Used to access application shared preferences.
    private SharedPreferences mPref = null;
    // Show dialog after password retrive success
    private Dialog mDialog;
    private LinearLayout mHelp;
 // add custom font for text view
    Typeface mRobotoRegular, mRobotoBold;
    private TextView mBottonSignInText,mBottonNeedHelpText,mTittle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ccm_forgotpwd_screen_layout);
        init();
        mPref = getSharedPreferences(Constants.CLOUD_COVER_PLAYER_PREFERENCE,
                Context.MODE_PRIVATE);
        if (mPref != null) {
            mAuthToken = mPref.getString(Constants.PARAM_DEVICE_AUTH_TOKEN, "");
            Log.d(TAG, "onCreate() mAuthToken : " + mAuthToken);
        }

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString().trim();
                String email = mEmailId.getText().toString().trim();

                // validation before sending request to forgot password api
                if (!username.isEmpty() && !email.isEmpty()) {
                    if (editTextValidate(email)) {
                        new ForgotPasswordTaskAsync(username, email).execute();
                    } else {
                        showDialog(getString(R.string.ccm_forgot_pwd_dialog_failure_text));
                    }
                } else {
                	if(!username.isEmpty()){
                		new ForgotPasswordTaskAsync(username, "").execute();
                	}else if (!email.isEmpty()){
                		if(editTextValidate(email))
                		{
                		new ForgotPasswordTaskAsync("", email).execute();
                		}else
                		{
                			showDialog(getString(R.string.ccm_forgot_pwd_dialog_failure_text));
                		}
                	}else {
                    showDialog(getString(R.string.ccm_forgot_pwd_dialog_empty_text));
                	}
                }
            }
        });
        mHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchHelp = new Intent(getApplicationContext(), CCMHelp.class);
                launchHelp.putExtra(Constants.INTENT_EXTRA_NEED_HELP_TYPE, CCMHelp.NEED_HELP_TYPE_LOGIN);
                startActivity(launchHelp);
            }
        });
        ((View) findViewById(R.id.ccmLogin))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Just finish this activity so that login will be displayed.
                        CCMForgotPwd.this.finish();
                    }
                });
    }

    public void init() {
        mUsername = (EditText) findViewById(R.id.ccmUsername);
        mEmailId = (EditText) findViewById(R.id.ccmEmail);
        mSubmit = (Button) findViewById(R.id.ccmSubmit);
        mDialog = new Dialog(CCMForgotPwd.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.ccm_forgotpwd_dialog_layout);
        mHelp = (LinearLayout) findViewById(R.id.ccmHelp);
        mBottonSignInText = (TextView) findViewById(R.id.ccmSignInText);
        mBottonNeedHelpText = (TextView) findViewById(R.id.ccmNeedHelpText);
        mTittle = (TextView) findViewById(R.id.heading);
        /**
         * Adding font to textview and edittext
         */
        mRobotoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Regular.ttf");
        mRobotoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Bold.ttf");
        mUsername.setTypeface(mRobotoRegular);
        mEmailId.setTypeface(mRobotoRegular);
        mSubmit.setTypeface(mRobotoRegular);
        mTittle.setTypeface(mRobotoRegular);
        mBottonSignInText.setTypeface(mRobotoRegular);
        mBottonNeedHelpText.setTypeface(mRobotoRegular);
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
                	if(Character.isSpaceChar(source.charAt(i)))
                	{
                        return "";
                    }
                }
                return null;
            }
        };
        mUsername.setFilters(new InputFilter[]{filter});
        mEmailId.setFilters(new InputFilter[]{filter});
       
    }

    /**
     * Validates the email id entered.
     * @param arg
     * @return
     */
    public static boolean editTextValidate(String arg) {
        try {
            Pattern pattern = Pattern
                    .compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
            Matcher matcher = pattern.matcher(arg);
            return matcher.matches();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    class ForgotPasswordTaskAsync extends AsyncTask<Void, String, String> {
        String mUsername = "";
        String mEmail = "";
        String mUserAuthenticationToken = null;
        ProgressDialog asyncDialog = new ProgressDialog(CCMForgotPwd.this);

        public ForgotPasswordTaskAsync(String username, String email) {
            this.mUsername = username;
            this.mEmail = email;
        }

        @Override
        protected void onPreExecute() {
            asyncDialog.setMessage(getString(R.string.ccm_requesting));
            asyncDialog.setCancelable(false);
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String sRet = "";
            ResponseData response = null;
            Context context = getApplicationContext();
            // Check Internet connectivity before making a login call.
            int strId = -1;
            if (!ServerUtilities.isNetworkAvailable(context)) {
                strId = CCMErrorHandler.ERROR_NO_NETWORK;
                return CCMErrorHandler.getErrorMessage(context, strId);
            }
            try {
                response = sendForgotPasswordRequest(mUsername, mEmail);
                if (response != null) {
                    // Check if its failure then send a message to show.
                    if (response.isSuccess()) {
                        sRet = null;
                    } else {
                        Log.d(TAG,"response : " + response.toString());
                        sRet = response.toString();
                    }
                }
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
                showDialog(getString(R.string.ccm_forgot_pwd_dialog_success_text));
            } else {
                // Show the error to user.
                showDialog(getString(R.string.ccm_forgot_pwd_dialog_failure_text));
                Log.d(TAG, "ForgotPasswordTaskAsync error : " + errorMsg);
            }
        }
    }

    /**
     * Sends username/Email or both to server for forgot password api
     * 
     * @param username
     * @param email
     * @return response
     * @throws CCMException
     */
    protected ResponseData sendForgotPasswordRequest(String username,
            String email) throws CCMException {
        Log.d(TAG, "Into sendForgotRequest() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if (!TextUtils.isEmpty(username)) {
            nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_USERNAME,
                    username));
        }
        if (!TextUtils.isEmpty(email)) {
            nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_EMAIL,
                    email));
        }
        return ServerUtilities.getInstance(context).sendPostRequest(
                Constants.PARAM_FORGOT_PWD, nameValuePairs);
    }

    String parseResult(Context context, ResponseData response)
            throws CCMException {
        // If not able to get the result object, get error
        // strings on the basis of HTTP error codes.
        return CCMErrorHandler.getErrorMessage(context,
                response.getHttpResponseCode());
    }

    /**
     * This method will show dialog box which will display the response of
     * success or failure
     * 
     * @param message
     * @author Sourabh
     */
    public void showDialog(String message) {
    	mDialog.setCancelable(false);
        mDialog.show();
        TextView tv_Text = (TextView) mDialog
                .findViewById(R.id.ccm_dialog_text);
        TextView tv_OK = (TextView) mDialog.findViewById(R.id.ccm_tvOk);

        tv_Text.setText(message);
        tv_OK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }
}
