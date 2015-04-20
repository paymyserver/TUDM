package com.turnupdmood.tudm;

import com.android.tudm.utils.Constants;
import com.android.tudm.utils.ServerUtilities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class TUDMSettingActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key != null
                && key.equalsIgnoreCase(Constants.PREF_KEY_SERVER_DOMAIN)) {
            // Make the ServerUtilities instance null so that it will pick the
            // new server domain.
            ServerUtilities.instance = null;
        }
    }
}