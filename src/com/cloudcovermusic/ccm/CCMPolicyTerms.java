package com.cloudcovermusic.ccm;

import com.android.cloudcovermusic.utils.Constants;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class CCMPolicyTerms extends Activity {
    private static final String TAG = CCMPolicyTerms.class.getSimpleName();
    private WebView mWebview;
    private ProgressBar mProgressBar;
    String value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ccm_privacy_terms_screen_layout);
        value = getIntent().getExtras().getString("value");
        
        mWebview = (WebView) findViewById(R.id.webView);
        WebSettings settings = mWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mWebview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100
                        && mProgressBar.getVisibility() == ProgressBar.GONE) {
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
                mProgressBar.setProgress(progress);
                if (progress == 100) {
                    mProgressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });
        mWebview.getSettings().setBuiltInZoomControls(true);
        mWebview.setInitialScale(100);
        if(value.equals(Constants.CCM_ANDROID_PRIVACY)){
        mWebview.loadUrl("https://www.cloudcovermusic.com/privacy-policy");
        }
        else if(value.equals(Constants.CCM_ANDROID_TERMS)){
        	 mWebview.loadUrl("https://www.cloudcovermusic.com/terms-of-use");
        }
    }
}
