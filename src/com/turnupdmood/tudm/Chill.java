package com.turnupdmood.tudm;

/**
 * Created by sushil.pangarkar on 4/16/2015.
 */
import android.app.Activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class Chill extends Activity {

    ListView list;
    String[] itemname ={
            "Rangdebasanti",
            "see you again",
            "songname4",
            "songname5",
            "songname6",
            "songname7",
            "songname8",
            "songname9",
            "sogname10"
    };

    Integer[] imgid={
            R.drawable.images,
            R.drawable.cover_mb,
            R.drawable.cover_mb,
            R.drawable.cover_mb,
            R.drawable.cover_mb,
            R.drawable.cover_mb,
            R.drawable.cover_mb,
            R.drawable.cover_mb,
            R.drawable.cover_mb
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chil);

        CustomList adapter=new CustomList(this, itemname, imgid);
        list=(ListView)findViewById(R.id.list1);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                String Slecteditem= itemname[+position];
                Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();

            }
        });
    }



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

