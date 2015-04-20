package com.turnupdmood.tudm;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tudm.utils.Constants;

import java.util.ArrayList;


public class PlayListActivity extends Activity {

    private GridView Mygrid;
    private int mPhotoSize, mPhotoSpacing;
    private Myadapter imageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play_list);
        Mygrid = (GridView)findViewById(R.id.gridView);
        Mygrid.setAdapter(new Myadapter(this));
        Mygrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Log.d("onclick","position");
                try {
                    Intent myIntent = null;
                    if (position == 0) {
                        Log.d("onclick","position");
                         myIntent = new Intent(PlayListActivity.this, TUDMPlayListManager.class);
                        myIntent.putExtra("PLAYLIST_NAME","Agnee");

                    }
                    if (position == 1){
                        myIntent = new Intent(PlayListActivity.this,TUDMPlayListManager.class);
                        myIntent.putExtra("PLAYLIST_NAME","indian_ocean");
                    }

                    startActivity(myIntent);
                }
                catch(Exception e){
                    Log.d("erroe","error");
                }
            }
        });


    }
    class item {
        int imageId;
        String name;
        item(int imageId,String name){
            this.imageId = imageId;
            this.name = name;
        }
    }

    public void onPlaylistClick(View view){

        ImageView cover = (ImageView) view.findViewById(R.id.imageView);
        //TextView title = (TextView) view.findViewById(R.id.title);

        Intent launchNext = new Intent(getApplicationContext(), TUDMPlayListManager.class);
        //Toast.makeText(PlayListActivity.this, title.getText(), Toast.LENGTH_LONG).show();
//        launchNext.putExtra("playListName", title.getText());
       // launchNext.putExtra(Constants.INTENT_EXTRA_FORCE_ACTIVATE, forceActivation);
        startActivity(launchNext);
        // Finish the login activity.
        //finish();

    }
    // ///////// ImageAdapter class /////////////////
    public class Myadapter extends BaseAdapter {
        ArrayList<item> list;
        Context context;
        Myadapter(Context context)
        {
            this.context = context;
            list = new ArrayList<item>();
            Resources res = context.getResources();
            String [] temp = res.getStringArray(R.array.images);
            int[] images = {R.drawable.cover_mb,R.drawable.cover_mb,R.drawable.cover_mb,R.drawable.cover_mb,R.drawable.cover_mb,
                    R.drawable.cover_mb,R.drawable.cover_mb};
            for(int i=0;i<7;i++)
            {
                item playlist = new item(images[i],temp[i]);
                list.add(playlist);
            }
        }
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
        class ViewHolder
        {
            ImageView myplaylist;
            ViewHolder(View v){
                myplaylist = (ImageView) v.findViewById(R.id.imageView);
            }
        }
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            View row = view;
            ViewHolder viewHolder = null;
            if(row == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.photo_item, viewGroup, false);
                viewHolder = new ViewHolder(row);
                row.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) row.getTag();
            }
            item temp = list.get(position);
            viewHolder.myplaylist.setImageResource(temp.imageId);
            return row;
        }

    }

}
