package com.turnupdmood.tudm;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tudm.json.JsonParser;
import com.android.tudm.model.ManagePlaylist;
import com.android.tudm.model.MeModal;
import com.android.tudm.service.TUDMCachingService;
import com.android.tudm.service.TUDMService;
import com.android.tudm.utils.TUDMException;
import com.android.tudm.utils.TUDMUtils;
import com.android.tudm.utils.Constants;
import com.android.tudm.utils.ServerUtilities;
import com.android.tudm.utils.ServerUtilities.ResponseData;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author NikeshTagrem
 *
 */
public class TUDMPlayListManager extends Activity{

	public static String TAG = "TUDMPlayListManager";
	public ArrayList<String> lstManagePlaylist;
	Context mContext;
	public ListView PlayListView;
	TextView txtStickeyheader;
	PlayListManagerAdapter playlistadapter = null;
	public int activePlaylistID = 0;
	ImageView headerImageicon = null;
	Button btnback;
    public String PLAYLIST_NAME;
	Typeface mRobotoRegular,mRobotoBold;
	 private TextView txtHeader;
	    private ImageView backBtn;
	    
	    String DAYPARTINGMUSIC = "dayparting presets";
	    String MIXMUSIC = "Mix Stations";

	public enum MusicType {

		PublicMusic ("public"),
		PrivateMusic ("private"),
		DaypartingMusic ("dayparting"),
		MixMusic ("mix");

		private final String name;       

		private MusicType(String s) {
			name = s;
		}

		public boolean equalsName(String otherName){
			return (otherName == null)? false:name.equals(otherName);
		}

		public String toString(){
			return name;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.com_manage_playlist_layout);
		try
		{
			Initilization();
            Intent intentObject = getIntent();
            PLAYLIST_NAME = intentObject.getStringExtra("PLAYLIST_NAME");
            Log.d("PlayListName in PLM: ",PLAYLIST_NAME);
			new PlayListAsync().execute();
		}
		catch(TUDMException ex)
		{
			Log.d(TAG, ex.getMessage());
		}

		PlayListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				try
				{
					String localManagePlaylist = lstManagePlaylist.get(firstVisibleItem);
					//if(localManagePlaylist !=null)
						/*if(localManagePlaylist.isHeader())
						{
						*/
                       // String value = txtStickeyheader.getText().toString();
						//	txtStickeyheader.setText("Agnee");
							//if(localManagePlaylist !=null && DAYPARTINGMUSIC.equalsIgnoreCase(localManagePlaylist.getName()))
							//{
							///	headerImageicon.setBackgroundResource(R.drawable.manage_playlist_dayparting);
							//}
						/*	else if(localManagePlaylist.getName() !=null && MIXMUSIC.equalsIgnoreCase(localManagePlaylist.getName().toLowerCase()))
							{
								headerImageicon.setBackgroundResource(R.drawable.manage_playlist_mix);
							}*/
						//	else
						//	{
						//		headerImageicon.setBackgroundResource(R.drawable.manage_playlist_ccm_station);
						//	}
						//}
				}
				catch(Exception ex)
				{
					Log.i("Exception", ex.getMessage());
				}
			}
		});
		
		backBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void SetAdapter()
	{
		PlayListView.setAdapter(playlistadapter);
		playlistadapter.notifyDataSetChanged();
	}

	private void Initilization() throws TUDMException
	{
		try
		{
			lstManagePlaylist = new ArrayList<String>();
			this.mContext = this;
			PlayListView = (ListView) findViewById(R.id.PlayListView);
			//txtStickeyheader = (TextView) findViewById(R.id.txtStickeyheader);
			//headerImageicon = (ImageView) findViewById(R.id.imgStickeyheader);
					mRobotoRegular = Typeface.createFromAsset(getAssets(),
			                 "fonts/Roboto-Regular.ttf");
			         mRobotoBold = Typeface.createFromAsset(getAssets(),
			                 "fonts/Roboto-Bold.ttf");
			         txtHeader = (TextView) findViewById(R.id.txtHeader);
			         backBtn =  (ImageView) findViewById(R.id.imageBack);
			         txtHeader.setText(getString(R.string.ccm_player_manage_Playlist));
			         txtHeader.setTypeface(mRobotoBold);
			playlistadapter = new PlayListManagerAdapter();

		}
		catch(Exception ex)
		{
			Log.d(TAG, ex.getMessage());
		}
	}

	class PlayListAsync extends AsyncTask<Object, Void, Object>
	{
		private ProgressDialog asyncDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			asyncDialog=new ProgressDialog(mContext);
			asyncDialog.setMessage(getResources().getString(R.string.ccm_removesong_please_wait));
			asyncDialog.setCancelable(false);
			asyncDialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			String error=null;
			try {
				if(!ServerUtilities.isNetworkAvailable(mContext)){
					error=getResources().getString(R.string.error_network_unavailable);
					return error;
				}

				ResponseData response = GetPlayListfromService();

				if (response != null) {
					if (response.isSuccess()) {
                        Log.d("response data ",response.getResponseJson().toString());

					lstManagePlaylist = ConvertObjectToManagePlayListArray(response.getResponseJson());
					/*	MeModal model = GetActivePlaylistID();
						if(model.getMusicId() !=null)
							activePlaylistID = Integer.parseInt(model.getMusicId());*/
					}
				}

			}catch (Exception e) {
				Log.d(TAG, ""+e.getMessage());
			}
			return error;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(asyncDialog !=null)
				asyncDialog.dismiss();
			SetAdapter();
			if(lstManagePlaylist !=null && lstManagePlaylist.size()== 0)
			{
				if(!ServerUtilities.isNetworkAvailable(TUDMPlayListManager.this))
				TUDMUtils.ShowMessage(TUDMPlayListManager.this, TUDMPlayListManager.this.getResources().getString(R.string.error_network_unavailable));
				else
				TUDMUtils.ShowMessage(TUDMPlayListManager.this, TUDMPlayListManager.this.getResources().getString(R.string.ccm_removesong_error_retriving_prevois_playlist));
				finish();
			}
		}

		private ResponseData GetPlayListfromService() throws TUDMException
		{
			/*return ServerUtilities.getInstance(mContext).sendGetRequest("music?token="+ TUDMUtils.getToken(mContext));*/

            //Toast.makeText(this,"PlayList Naame:"+PLAYLIST_NAME,Toast.LENGTH_LONG).show();
                return ServerUtilities.getInstance(mContext).sendGetRequest(PLAYLIST_NAME);

		}
	}

	class PlayListManagerAdapter extends BaseAdapter
	{
		private LayoutInflater mInflater;

		public PlayListManagerAdapter() {

			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			if(lstManagePlaylist !=null)
				return lstManagePlaylist.size();
			else
				return 0;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View convertView = arg1;

			Button buttonSelect = null;
			TextView TextTitle;
			ImageView imgPlaying,HeaderImage;
			LinearLayout llManageListItem;
			String manageplaylist = lstManagePlaylist.get(arg0);
			try
			{
				if(manageplaylist !=null)
				{
					//if(manageplaylist.isHeader())
					//{
						convertView = mInflater.inflate(R.layout.com_manage_playlist_row_header, null);
						HeaderImage = (ImageView) convertView.findViewById(R.id.imgHeader);
						/*if(manageplaylist.getName() !=null && DAYPARTINGMUSIC.equalsIgnoreCase(manageplaylist.getName()))
						{
							HeaderImage.setBackgroundResource(R.drawable.manage_playlist_dayparting);
						}
						else if(manageplaylist.getName() !=null && MIXMUSIC.equalsIgnoreCase(manageplaylist.getName()))
						{
							HeaderImage.setBackgroundResource(R.drawable.manage_playlist_mix);
						}
						else
						{*/
						//	HeaderImage.setBackgroundResource(R.drawable.manage_playlist_ccm_station);
						//}
						//SetText(R.id.txtHeaderName, convertView, "Agnee");
					//}
					//else
					//{
						convertView = mInflater.inflate(R.layout.com_manage_playlist_row_item, null);
						buttonSelect = (Button) convertView.findViewById(R.id.btnSelect);
						//imgPlaying = (ImageView) convertView.findViewById(R.id.imgPlaying);
						llManageListItem = (LinearLayout) convertView.findViewById(R.id.llManageListItem);
						//TextTitle =	(TextView) SetText(R.id.txtTitle, convertView, manageplaylist);
						SetText(R.id.txtSubName, convertView, manageplaylist);
						//SetText(R.id.txtDescriptoin, convertView, "");
						onSelectClicked(buttonSelect,manageplaylist,llManageListItem);
						/*if(activePlaylistID == manageplaylist.getId())
						{
							buttonSelect.setVisibility(View.GONE);
							imgPlaying.setVisibility(View.VISIBLE);
							TextTitle.setTextColor(mContext.getResources().getColor(R.color.ccm_playlistmanage_active_playlist_titlecolor));
							llManageListItem.setBackgroundResource(R.color.ccm_playlistmanage_active_playlist_background);
						}*/
					//}
				}
			}
			catch(Exception ex)
			{
				Log.d(TAG, ex.getMessage());
			}
			return convertView;
		}
		
		private void Refresh()
		{
			notifyDataSetChanged();
		}

		private void onSelectClicked(final Button button,final String managePlayList,final LinearLayout llListContentManage)
		{

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

                    Intent launchNext = new Intent(TUDMPlayListManager.this, TUDMPlayer.class);
                       // launchNext.putExtra(Constants.INTENT_EXTRA_LAUNCH_FIRST_TIME, firstTime);
                        launchNext.putExtra("SONG_NAME", managePlayList);
                        launchNext.putExtra("PLAYLIST_NAME", PLAYLIST_NAME);

                    launchNext.putExtra(Constants.INTENT_EXTRA_LAUNCH_FIRST_TIME, true);
                    launchNext.putExtra(Constants.INTENT_EXTRA_FORCE_ACTIVATE, true);

                       startActivity(launchNext);

                        // Finish the login activity.
                    //   finish();
                   // }

/*

					new AsyncTask<Object, Void, Object>()
					{
						private ProgressDialog asyncDialog;
						protected void onPreExecute() {

							asyncDialog=new ProgressDialog(mContext);
							asyncDialog.setMessage(getResources().getString(R.string.ccm_removesong_please_wait));
							asyncDialog.setCancelable(false);
							asyncDialog.show();
						};

						@Override
						protected Object doInBackground(Object... params) {
							String error=null;
							try {
								if(!ServerUtilities.isNetworkAvailable(mContext)){
									error=getResources().getString(R.string.error_network_unavailable);
									return error;
								}
								*/
/*if(managePlayList.getMusicType().equalsIgnoreCase(MusicType.PublicMusic.toString()))
								{
									managePlayList.setMusicType("playlist");
								}*//*

								*/
/*String urlParam = "users/"+ TUDMUtils.getUserId(mContext)+"/music?token="+
										TUDMUtils.getToken(mContext)+"&custid="+
										TUDMUtils.getCustId(mContext)+"&music_id="+
										managePlayList.getId()+"&music_type="+managePlayList.getMusicType();*//*

								return ServerUtilities.getInstance(mContext).sendPutRequest("");

							}catch (Exception e) {
								Log.d(TAG, ""+e.getMessage());
							}
							return error;
*/
						}

						/*@Override
						protected void onPostExecute(Object result) {
							super.onPostExecute(result);
							if(asyncDialog !=null)
								asyncDialog.dismiss();
							if(ServerUtilities.isNetworkAvailable(mContext)){
							if(result !=null)
							{
								ResponseData response = null;
								try
								{
								 response = (ResponseData) result;
								}
								catch(ClassCastException ex)
								{
									TUDMUtils.ShowMessage(mContext, getResources().getString(R.string.error_network_unavailable));
								}
								if (response != null) {
									if (response.isSuccess()) {
										//activePlaylistID = managePlayList.getId();
										button.setVisibility(View.GONE);
										//imgPlaying.setVisibility(View.VISIBLE);
										llListContentManage.setBackgroundResource(R.color.ccm_playlistmanage_active_playlist_background);
										//txtTitle.setBackgroundResource(R.color.ccm_playlistmanage_active_playlist_titlecolor);
										TUDMUtils.ShowMessage(mContext, mContext.getResources().getString(R.string.com_mangage_playlist_active_message_1) + " \"" + managePlayList + "\" " + getResources().getString(R.string.com_mangage_playlist_active_message_2));
										Refresh();
									}
									else
									{
										TUDMUtils.ShowMessage(mContext, getResources().getString(R.string.ccm_manage_playlist_error_response));
									}
								}
							}
							}
							else
							{
								TUDMUtils.ShowMessage(mContext, getResources().getString(R.string.error_network_unavailable));}
							
						}

					}.execute();
				}*/
			});
		}

		private View SetText(int id,View view,String value)
		{
			TextView textview = (TextView) view.findViewById(id);
			if(textview !=null)

			{
				if(value !=null && !value.equals(""))
					textview.setText(value);
				else
					textview.setVisibility(View.GONE);
			}
			
			return textview;
		}
	}

	private ArrayList<String> ConvertObjectToManagePlayListArray(JSONObject jsonobject)throws JSONException
	{
		//TODO 

		ManagePlaylist manageplaylist = null;
		ArrayList lstManagePlaylist = new ArrayList<String>();
		JSONArray jsonArray = null;
		int musiclenth = 0;
        Iterator iterator=jsonobject.keys();
        while (iterator.hasNext()){
            lstManagePlaylist.add(jsonobject.getString((String) iterator.next()));
        }
       	/*//getJSONArray(MusicType.DaypartingMusic.toString());
		musiclenth = jsonArray.length();
		if(musiclenth>0)
		{
			manageplaylist = new ManagePlaylist();
			manageplaylist.setHeader(true);
			manageplaylist.setName(getString(R.string.com_manage_playlist_Dayparting));
			lstManagePlaylist.add(manageplaylist);

			for(int i=0;i<musiclenth;i++)
			{
				manageplaylist = (ManagePlaylist) JsonParser.parseJsonToType(mContext,jsonArray.getJSONObject(i),
						new TypeToken<ManagePlaylist>() {
				}.getType());
				manageplaylist.setId(manageplaylist.getDaypartingid());
				manageplaylist.setMusicType(MusicType.DaypartingMusic.toString());
				lstManagePlaylist.add(manageplaylist);
			}
		}
		
		jsonArray = jsonobject.getJSONArray(MusicType.MixMusic.toString());
		musiclenth = jsonArray.length();
		if(musiclenth>0)
		{
			manageplaylist = new ManagePlaylist();
			manageplaylist.setHeader(true);
			manageplaylist.setName(getString(R.string.com_manage_playlist_Mix));
			lstManagePlaylist.add(manageplaylist);

			for(int i=0;i<musiclenth;i++)
			{
				manageplaylist = (ManagePlaylist) JsonParser.parseJsonToType(mContext,jsonArray.getJSONObject(i),
						new TypeToken<ManagePlaylist>() {
				}.getType());
				manageplaylist.setId(manageplaylist.getMixid());
				manageplaylist.setMusicType(MusicType.MixMusic.toString());
				lstManagePlaylist.add(manageplaylist);
			}
		}
		
		jsonArray = jsonobject.getJSONArray(MusicType.PublicMusic.toString());
		musiclenth = jsonArray.length();
		if(musiclenth>0)
		{
			manageplaylist = new ManagePlaylist();
			manageplaylist.setHeader(true);
			manageplaylist.setName(getString(R.string.com_manage_playlist_Public));
			lstManagePlaylist.add(manageplaylist);

			for(int i=0;i<musiclenth;i++)
			{
				manageplaylist = (ManagePlaylist) JsonParser.parseJsonToType(mContext,jsonArray.getJSONObject(i),
						new TypeToken<ManagePlaylist>() {
				}.getType());
				manageplaylist.setId(manageplaylist.getPlaylistid());
				manageplaylist.setMusicType(MusicType.PublicMusic.toString());
				lstManagePlaylist.add(manageplaylist);
			}
		}

		jsonArray = jsonobject.getJSONArray(MusicType.PrivateMusic.toString());
		musiclenth = jsonArray.length();
		if(musiclenth>0)
		{
			manageplaylist = new ManagePlaylist();
			manageplaylist.setHeader(true);
			manageplaylist.setName(MusicType.PrivateMusic.toString());
			lstManagePlaylist.add(manageplaylist);

			for(int i=0;i<musiclenth;i++)
			{
				manageplaylist = (ManagePlaylist) JsonParser.parseJsonToType(mContext,jsonArray.getJSONObject(i),
						new TypeToken<ManagePlaylist>() {
				}.getType());
				manageplaylist.setMusicType(MusicType.PrivateMusic.toString());
				lstManagePlaylist.add(manageplaylist);
			}
		}*/
		return lstManagePlaylist;
	}

	public MeModal GetActivePlaylistID() throws TUDMException
	{
		MeModal memodal = null;
		ResponseData responedata = ServerUtilities.getInstance(mContext).sendGetRequest(
				Constants.ME + TUDMUtils.getToken(mContext));

		if(responedata.isSuccess())
		{
			memodal = (MeModal) JsonParser.parseJsonToType(getApplicationContext(),
					responedata.getResponseJson(),new TypeToken<MeModal>() {}.getType());
		}
		return memodal;
	}
}
