package com.cloudcovermusic.ccm;

import com.android.cloudcovermusic.utils.Constants;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Help Screen to show the Help content to the user.<br>
 * This activity can be launched from {@link CCMLogin} or {@link CCMPlayer}.
 * 
 * @author Mahesh Chauhan
 * 
 */
public class CCMHelp extends Activity {
    private static final String TAG = CCMHelp.class.getSimpleName();
    // TextView for help screen.
    private TextView mHelpText;
    public static final int NEED_HELP_TYPE_LOGIN = 0;
    public static final int NEED_HELP_TYPE_PLAYER = 1;
 // add custom font for text view
    Typeface mRobotoRegular, mRobotoBold;
    private TextView txtHeader;
    private ImageView backBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bindDataToViews();
    }

    /**
     * Initializes the views of this screen.
     */
    public void init() {
        mHelpText = (TextView) findViewById(R.id.ccmHelpContent);
        
        /**
         * Adding font to textview and edittext
         */
        mRobotoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Regular.ttf");
        mRobotoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Bold.ttf");
        mHelpText.setTypeface(mRobotoRegular);
        
       
		txtHeader = (TextView) findViewById(R.id.txtHeader);
        backBtn =  (ImageView) findViewById(R.id.imageBack);
        txtHeader.setTypeface(mRobotoRegular);
        txtHeader.setText(getString(R.string.ccm_player_option_help));
        backBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
    }

    /**
     * Bind the data to the views.
     */
    private void bindDataToViews() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int type = extras.getInt(Constants.INTENT_EXTRA_NEED_HELP_TYPE);
            if (type == NEED_HELP_TYPE_LOGIN) {
                setContentView(R.layout.ccm_login_help_screen_layout);
                init();
                mHelpText.setText(getString(R.string.ccm_help_content_login));
                mHelpText.setGravity(Gravity.CENTER_VERTICAL);
            } else if (type == NEED_HELP_TYPE_PLAYER) {
                setContentView(R.layout.ccm_help_screen_layout);
                init();
                mHelpText.setText(Html
                        .fromHtml(getString(R.string.ccm_help_content_player)));
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}
