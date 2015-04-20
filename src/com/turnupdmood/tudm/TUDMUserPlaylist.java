package com.turnupdmood.tudm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class TUDMUserPlaylist extends Activity {

    String SONG_NAME;
    TextView mTrackInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tudmuser_playist);
        Intent intent=getIntent();
        SONG_NAME = intent.getStringExtra("SONG_NAME");
        mTrackInfo = (TextView) findViewById(R.id.ccmSongInfo);
        mTrackInfo.setText(SONG_NAME);
        ImageView backBtn = (ImageView) findViewById(R.id.imageBack);
        Toast.makeText(getApplicationContext(),"Song is Added to Playlist",Toast.LENGTH_LONG).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tudmuser_playist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
