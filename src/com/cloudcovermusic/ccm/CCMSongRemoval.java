package com.cloudcovermusic.ccm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.cloudcovermusic.json.JsonParser;
import com.android.cloudcovermusic.model.SongRemovalModal;
import com.android.cloudcovermusic.service.CCMService;
import com.android.cloudcovermusic.utils.CCMException;
import com.android.cloudcovermusic.utils.Constants;
import com.android.cloudcovermusic.utils.ServerUtilities;
import com.android.cloudcovermusic.utils.ServerUtilities.ResponseData;
import com.google.gson.reflect.TypeToken;
/***
 * This is class to remove song from play list and you can play song for 15 second
 * @author rushikesh
 *
 */
public class CCMSongRemoval extends Activity {
    private static final String TAG = CCMSongRemoval.class.getSimpleName();
    MediaPlayer mPlayer;
    private ListView listView;

    DownImageLoader _DownImageLoader;
    List<SongRemovalModal> itemList;
    Activity activity;
    String mPath;
    int i=0;
    ArrayList<Integer> boundsList;
    
 // media id is used while removing song
    private int mediaID;
    
    private int itemPostion=-1;
    
    SongRemovalListAdapter listAdapter;
    
    SongRemovalModal previousItem;
    
    PlayListAsyncTask playListAsyncTask;
    CCMService mService;
    boolean isPreviewPlaying = true;
    boolean isMusicStopPlaying=false;
    CountDownTimer cntr_aCounter;
    private TextView txtHeader;
    private ImageView backBtn;
    
 // add custom font for text view
    Typeface mRobotoRegular, mRobotoBold;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ccm_removeong_screen_layout);
       
        init();
        activity=CCMSongRemoval.this;
        boundsList = new ArrayList<Integer>();
        
    }
    /**
     * Initilize all views
     */
    private void init(){
        
        itemList = new ArrayList<SongRemovalModal>();
        listView=(ListView) findViewById(R.id.ccmSongRemovalList);
        getPlayListData();
        
        mRobotoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Regular.ttf");
        mRobotoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Bold.ttf");
        txtHeader = (TextView) findViewById(R.id.txtHeader);
        backBtn =  (ImageView) findViewById(R.id.imageBack);
        txtHeader.setText(getString(R.string.ccm_player_option_song_removal));
        txtHeader.setTypeface(mRobotoRegular);
        
        backBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
    }
    private class PlayListAsyncTask extends AsyncTask<Object, Object, String> {
        
        private ProgressDialog asyncDialog;
        CCMService mService;
        public PlayListAsyncTask() {
            
            asyncDialog=new ProgressDialog(CCMSongRemoval.this);
            asyncDialog.setMessage(getResources().getString(R.string.ccm_removesong_please_wait));
            asyncDialog.setCancelable(false);
            mService = CCMService.getInstance();
        }
        
        protected void onPreExecute() {
            super.onPreExecute();
            asyncDialog.show();
           
        }
        protected String doInBackground(Object... params) {
            String error=null;
            SongRemovalModal songModal;
            try {
                if(!ServerUtilities.isNetworkAvailable(CCMSongRemoval.this)){
                    error=getResources().getString(R.string.error_network_unavailable);
                    return error;
                }
                
                ResponseData response = sentPreviousSongRequest();
                if (response != null) {
                    // Check if its success or failure.
                    if (response.isSuccess()) {
                        JSONObject result = response.getResponseJson();
                        if(result!=null){
                            JSONArray jsonArray=result.getJSONArray("songs");
                            int songCount=Integer.parseInt(Constants.PARAM_LIMIT_VALUE);
                            
                            for (int i = 0; i < jsonArray.length(); i++) {
                             
                             // Parse the json response.
                                songModal = (SongRemovalModal) JsonParser
                                        .parseJsonToType(getApplicationContext(),
                                                jsonArray.getJSONObject(i),
                                                new TypeToken<SongRemovalModal>() {
                                                }.getType());
                                
                                if(songModal!=null && itemList!=null){
                                   if(itemList.size()<songCount && !CCMApplication.isTrackCached(songModal.getName())){
                                        itemList.add(songModal);
                                        boundsList.add(0);
                                    }
                                }else if (itemList!=null&&itemList.size()>songCount){
                                    break;
                                }
                                
                                
                            }
                            error="";
                        } 
                        
                    }
                }
                
            }catch (CCMException ccmException){
                Log.d(TAG, ""+ccmException.getMessage());
            }
            catch (Exception e) {
                Log.d(TAG, ""+e.getMessage());
            }
            return error;
        }
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            
            if (result == null) {
                showToast(getResources()
                        .getString(
                                R.string.ccm_removesong_error_retriving_prevois_playlist));
                finish();
            } else if (result.equalsIgnoreCase(getResources().getString(
                    R.string.error_network_unavailable))) {
                showToast(result);
                finish();
            } else {
            	if(mService!=null)
            	{
            	try{
            	for(int i=0;i<itemList.size();i++){
            	if(mService.getSongName().contains(itemList.get(i).getName())){
            		if(i!=0){
            		Collections.swap(itemList, i, 0);
            		}
            	}
            	}
            	}catch(Exception ae){
            		System.out.println("no song name found");
            	}
            	}
            	listAdapter=new SongRemovalListAdapter(getApplicationContext(),R.id.ccmSongRemovalList,itemList);
                listView.setAdapter(listAdapter);

            }
            if (asyncDialog != null) {
                asyncDialog.dismiss();
            }
        }
    }
    
    private void getPlayListData(){
        if(playListAsyncTask!=null){
            playListAsyncTask.cancel(true);
        }
        
        new PlayListAsyncTask().execute();
    }
    
    private class SongRemovalListAdapter extends ArrayAdapter{
        Context context=getApplicationContext();
        List< SongRemovalModal> listItem;
        
        public SongRemovalListAdapter(Context context, int resource,List<SongRemovalModal> listItem)  {
            super(context, resource);
            this.context=context;
            this.listItem=listItem;
            
        }

        @Override
        public SongRemovalModal getItem(int position) {
            return itemList.get(position);
        }

        
        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final ViewHolder holder;
                final SongRemovalModal mItem=getItem(position);
                if(mItem!=null){
                    if(convertView==null){
                        convertView = inflater.inflate(R.layout.ccm_removesong_layout, null);
                        holder = new ViewHolder(convertView);
                        convertView.setTag(holder);
                    }else{
                        holder = (ViewHolder) convertView.getTag();
                    }
                    //holder.mProgressBar.setTag(position);
                    
                    //Toast.makeText(activity, ""+mItem.getAlbum().getThumbnail()+Constants.TOKEN_SUFFIX+getToken(), Toast.LENGTH_LONG).show();
                    _DownImageLoader = new DownImageLoader(activity);
                    holder.mAlbumImageView.setTag(mItem.getAlbum().getThumbnail()+Constants.TOKEN_SUFFIX+getToken()); //set url tag to image
                     _DownImageLoader.DisplayImage(mItem.getAlbum().getThumbnail()+Constants.TOKEN_SUFFIX+getToken(),activity,holder.mAlbumImageView);
                    
                    holder.mSongTitle.setTypeface(mRobotoBold);
                    holder.mSongTitle.setText(mItem.getName());
                    holder.mArtistAndAlbumName.setTypeface(mRobotoRegular);
                    holder.mArtistAndAlbumName.setText(mItem.getArtist().getName()+" - "+ mItem.getAlbum().getName());

                    holder.mPlayText.setTypeface(mRobotoRegular);
                    holder.mRemoveText.setTypeface(mRobotoRegular);
                    
                    holder.mSongTitle.setText(mItem.getName());
                    holder.mArtistAndAlbumName.setText(mItem.getArtist().getName()+" - "+ mItem.getAlbum().getName());
                    if(mItem.isItemClicked()){
                        holder.playAndRemoveSong.setVisibility(View.GONE);
                        holder.removSongAction.setVisibility(View.VISIBLE);
                    }else{
                        holder.isClicked=false;
                        holder.playAndRemoveSong.setVisibility(View.VISIBLE);
                        holder.removSongAction.setVisibility(View.GONE);
                    }
                    
                    if(mItem.isPlayedClicked()){
                    	holder.mPlayPreview.setVisibility(View.GONE);
						holder.mStopPreview.setVisibility(View.VISIBLE);
						holder.mProgressBar.setVisibility(View.VISIBLE);
                    }
                    else{
                    	holder.mPlayPreview.setVisibility(View.VISIBLE);
						holder.mStopPreview.setVisibility(View.GONE);
						holder.mProgressBar.setVisibility(View.GONE);
                    }
                    
                    holder.playSongLayout.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                        	//if(isPreviewPlaying)
                        	{
                        		if(ServerUtilities.isNetworkAvailable(activity)){
                            stopMusic();
                            if(getPreviousItem()!=null){
                                getPreviousItem().setItemClicked(false);
                                try{
                                	if(mPlayer!=null && mPlayer.isPlaying())
                                	{
                                		getPreviousItem().isPlayedClicked(false);
                                    	holder.playSongLayout.setEnabled(true);
                    		        	invalidateList();
                    		        	isPreviewPlaying=true;
                                		mPlayer.stop();
                                	}

                                	}catch(Exception e)
                            		{
                            			System.out.println("Force stop on back press");
                            		}
                            }
                            setPreviousItem(mItem);
                            
                            mItem.isPlayedClicked(true);
                            if(ServerUtilities.isNetworkAvailable(activity))
							{
							holder.playSongLayout.setEnabled(false);
							holder.mPlayPreview.setVisibility(View.GONE);
							holder.mStopPreview.setVisibility(View.VISIBLE);
							holder.mProgressBar.setVisibility(View.VISIBLE);
							getTrack mgetTrack = new getTrack(mItem.getSonguri().getOgg()+Constants.TOKEN_SUFFIX+getToken(),holder.mProgressBar,
									holder.mPlayPreview,holder.mStopPreview,holder.playSongLayout,mItem,position);
							mgetTrack.execute();
							}else
							{
								 showToast(getResources().getString(R.string.error_network_unavailable));
							}
                            invalidateList();
                        	}else{
                    			showToast(getResources().getString(R.string.error_network_unavailable));
                    		}
                        	}/*else{
                        		showToast(getResources().getString(R.string.ccm_removesong_preview_playing));
                        	}*/
                        }
                    });
                    
                    holder.removeSongLayout.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View view) {
                        	
                            if(getPreviousItem()!=null){
                                getPreviousItem().setItemClicked(false);
                                try{
                                	if(mPlayer!=null && mPlayer.isPlaying())
                                	{
                                		getPreviousItem().isPlayedClicked(false);
                                    	holder.playSongLayout.setEnabled(true);
                    		        	invalidateList();
                    		        	isPreviewPlaying=true;
                                		mPlayer.stop();
                                	}

                                	}catch(Exception e)
                            		{
                            			System.out.println("Force stop on back press");
                            		}
                            }
                            setPreviousItem(mItem);
                            mItem.setItemClicked(true);
                            invalidateList();
                            holder.playAndRemoveSong.setVisibility(View.GONE);
                            holder.removSongAction.setVisibility(View.VISIBLE);
                        }
                    });
                    
                      holder.cancelSongLayout.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View view) {
                            mItem.setItemClicked(false);
                            holder.playAndRemoveSong.setVisibility(View.VISIBLE);
                            holder.removSongAction.setVisibility(View.GONE);
                            
                        }
                    });
                      
                      holder.inappropriateLayout.setOnClickListener(new View.OnClickListener() {
                          
                          @Override
                          public void onClick(View view) {
                              setMediaID(Integer.parseInt(mItem.getMediaId()));
                              setItemPostion(position);
                              removeSong(Constants.PARAM_REMOVE_SONG_REASON_INAPPOPRIATE);
                          }
                      });
                      
                      holder.wrongStationLayout.setOnClickListener(new View.OnClickListener() {
                          
                          @Override
                          public void onClick(View view) {
                              setMediaID(Integer.parseInt(mItem.getMediaId()));
                              setItemPostion(position);
                              removeSong(Constants.PARAM_REMOVE_SONG_REASON_WRONG_STATION);
                          }
                      });
                      
                      holder.disLikeLayout.setOnClickListener(new View.OnClickListener() {
                          
                          @Override
                          public void onClick(View view) {
                              setMediaID(Integer.parseInt(mItem.getMediaId()));
                              setItemPostion(position);
                              removeSong(Constants.PARAM_REMOVE_SONG_REASON_DISLIKE);
                          }
                      });
                      
                      try{
                      holder.mProgressBar.setProgress(boundsList.get(position));
                      }catch(Exception ae){
                    	  System.out.println("No value for progress bar");
                      }
                }
            return convertView;
        }
    }
    
    private static class ViewHolder {
        
        ImageView mAlbumImageView;
        TextView mSongTitle;
        TextView mArtistAndAlbumName;
        TextView mPlayText;
        TextView mRemoveText;
        LinearLayout playSongLayout;
        LinearLayout removeSongLayout;
        ProgressBar mProgressBar;
        ImageView mPlayPreview,mStopPreview;

        LinearLayout cancelSongLayout;
        LinearLayout playAndRemoveSong;
        LinearLayout removSongAction;
        LinearLayout inappropriateLayout;
        LinearLayout wrongStationLayout;
        LinearLayout disLikeLayout;
        boolean isClicked;
        //TextView mPlayText;

        ViewHolder(View view) {
            mAlbumImageView = (ImageView) view.findViewById(R.id.ccmSongRemovaeImageViwe);
            mSongTitle = (TextView) view.findViewById(R.id.ccmRemoveSongTitle);
            mPlayText=(TextView) view.findViewById(R.id.playText);
            mRemoveText=(TextView) view.findViewById(R.id.removeText);
            mArtistAndAlbumName = (TextView) view.findViewById(R.id.ccmRemoveSongArtistAndAlbum);
            playSongLayout=(LinearLayout) view.findViewById(R.id.ccmRemoveSong_PlaySong);
            removeSongLayout=(LinearLayout) view.findViewById(R.id.ccmRemoveSong_RemoveSong);

            mProgressBar = (ProgressBar) view.findViewById(R.id.circularProgressBar);
            mPlayPreview = (ImageView) view.findViewById(R.id.ccm_play_preview);
            mStopPreview = (ImageView) view.findViewById(R.id.ccm_stop_preview);

            cancelSongLayout=(LinearLayout) view.findViewById(R.id.ccmRemovSongCancel);
            playAndRemoveSong=(LinearLayout) view.findViewById(R.id.ccmPlayAndRemoveSong);
            removSongAction=(LinearLayout) view.findViewById(R.id.ccmRemovSong_Action);
            inappropriateLayout=(LinearLayout) view.findViewById(R.id.ccmRemovSongInapppropriate);
            wrongStationLayout=(LinearLayout) view.findViewById(R.id.ccmRemovSongWrongstation);
            disLikeLayout=(LinearLayout) view.findViewById(R.id.ccmRemovSongDislike);
            //mPlayText = (TextView) view.findViewById(R.id.playText);

        }
    }
    
    
    
    private ResponseData sentPreviousSongRequest() throws CCMException{
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        return ServerUtilities.getInstance(context).sendGetRequest(
                Constants.PREVIOUS_SONG+Constants.TOKEN_SUFFIX+getToken()+""+Constants.PARAM_OFFSET+""+Constants.PARAM_OFFSET_VALUE+""+Constants.PARAM_LIMIT+""+Constants.PARAM_LIMIT_VALUE);
        
    }
    
    /**
     * this method call remove song api to remove song from server play list
     * reason will say why user want to remove song
     */
    String mReason;
    private void removeSong(String reason) {
        mReason=reason;
        RemoveAsynctask mRemoveAsynctask=new RemoveAsynctask();
        mRemoveAsynctask.execute();
        
    }
    
    /***
     * Async task is to remove song from server play list
     */
    
    class RemoveAsynctask extends AsyncTask<Void, Void, String>{
        ProgressDialog mAsyncDialog;
        RemoveAsynctask(){
            mAsyncDialog=new ProgressDialog(CCMSongRemoval.this);
            mAsyncDialog.setCancelable(false);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAsyncDialog.setMessage(getResources().getString(R.string.ccm_player_removeing_song));
            mAsyncDialog.show();
           
        }
        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Into RemoveAsynctask doInBackground()");
            String error = getString(R.string.ccm_player_failed_removeing_song);
            try {
                if(!ServerUtilities.isNetworkAvailable(CCMSongRemoval.this)){
                    error=getResources().getString(R.string.error_network_unavailable);
                    return error;
                }
                
                ResponseData response = sendRemoveSongRequest();
                if (response != null) {
                    // Check if its failure then send a message to show.
                    JSONObject jsonObject=response.getResponseJson();
                    Log.d(TAG, "Into RemoveAsynctask doInBackground() response"+response.getResponseJson());
                    if (response.isSuccess()) {
                        error = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return error;
        }
        @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                if (mAsyncDialog.isShowing()) {
                    mAsyncDialog.dismiss();
                }
                if (response == null) {
                   // updateActionPanel(true);
                    showToast(getResources().getString(R.string.ccm_player_sucess_removeing_song));
                    itemList.remove(getItemPostion());
                    getPlayListData();
                   // invalidateList();
                } else {
                    if(response.equals(getResources().getString(R.string.error_network_unavailable))){
                        showToast(getResources().getString(R.string.error_network_unavailable));
                    }else{
                        showToast(getResources().getString(R.string.ccm_player_failed_removeing_song));
                    }
                }
            }
        
    }
    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CCMSongRemoval.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
   
 protected ResponseData sendRemoveSongRequest() throws CCMException{
        
        Log.d(TAG, "Into sendRemoveSongRequest() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(
                Constants.PARAM_DEVICE_AUTH_TOKEN, getToken()));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_CUSTID,
                getCustomerId() + ""));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_MEDIA_ID,
                gettMediaID() + ""));
        nameValuePairs.add(new BasicNameValuePair(Constants.PARAM_REASON,
                mReason));

        return ServerUtilities.getInstance(context).sendPostRequest(
                Constants.REMOVE_SONG_API, nameValuePairs);
    }
 private int getCustomerId(){
     int custId=-1;
     SharedPreferences pref = getSharedPreferences(
             Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
     if (pref != null) {
         custId = pref.getInt(Constants.PARAM_CUSTID,-1);
     }
     return custId;
     
 }
 
 public void setMediaID(int mediaID) {
     this.mediaID = mediaID;
 }
 private int gettMediaID() {
     return mediaID;
 }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        itemList.clear();
        //playMusic();
        if(_DownImageLoader!=null){
    		_DownImageLoader.clearCache(); 
    	}
    }
    private String getToken() {
        SharedPreferences pref = getSharedPreferences(
                Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
        String token = null;
        if (pref != null) {
            token = pref.getString(Constants.PARAM_DEVICE_AUTH_TOKEN, "");
        }
        return token;
    }

    
    /**
     * Ascync task to download song and play them
     * @author Sourabh
     * @param mPath
	 * @param _progressBar
	 * @param _startView
	 * @param _stopView
	 * @param playSongLayout
     */
    class getTrack extends AsyncTask<Void,Void,Void> 
	{
		
		ProgressDialog mAsyncDialog;
		ProgressBar _progressBar;
		ImageView _startView,_stopView;
		String mPath=null;
		String mFilePath;
		LinearLayout playSongLayout;
		MediaPlayer mPlayer;
		 SongRemovalModal mItem;
		 int index;
		 //TextView mPlayText;

		/**
		 * 
		 * @param mPath
		 * @param _progressBar
		 * @param _startView
		 * @param _stopView
		 * @param playSongLayout
		 * @param mItem 
		 * @param position 
		 * @param mPlayText 
		 */
		public getTrack(String mPath,ProgressBar _progressBar,ImageView _startView,ImageView _stopView, LinearLayout playSongLayout, SongRemovalModal mItem, int position) {
			mAsyncDialog = new ProgressDialog(CCMSongRemoval.this);
			this._progressBar =_progressBar;
			this._startView = _startView;
			this._stopView = _stopView;
			this.mPath = mPath;
			this.playSongLayout = playSongLayout;
			this.mItem = mItem;
			this.index = position;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mAsyncDialog
			.setMessage("Initializing Track...");
			mAsyncDialog.setCancelable(false);
			mAsyncDialog.show();
			if(mPlayer!=null && mPlayer.isPlaying())
			{
				mPlayer.stop();
	        	mPlayer.release();
	        	mPlayer=null;
	        	invalidateList();
			}
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			mFilePath=downloadResource(CCMSongRemoval.this,
					mPath,
					"preview");
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mAsyncDialog.dismiss();
			System.out.println("File Path "+mFilePath);
			//Toast.makeText(getApplicationContext(), ""+mFilePath, Toast.LENGTH_LONG).show();
			if(mPath!=null){
			if (mPlayer == null) {
				mPlayer = new MediaPlayer();
				//mPlayer = MediaPlayer.create(getApplicationContext(),Uri.parse("http://api.cloudcovermusic.com/media/play/cf526b58e4cbce08d80f1a22fa34ed73.ogg?token=ec51fb096a27b4949b7729011e77f56dae37f050"));
				try {
					mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					mPlayer.setDataSource(getApplicationContext(), Uri.parse(mFilePath));
					
				} catch (IllegalArgumentException e) {
					System.out.println("IllegalArgumentException");
				} catch (SecurityException e) {
					System.out.println("SecurityException");
				} catch (IllegalStateException e) {
					System.out.println( "IllegalStateException 1 ");
				} catch (IOException e) {
					e.printStackTrace();
				}catch (NullPointerException np){
					showToast(getResources().getString(R.string.ccm_removesong_error_playing_track));
					_progressBar.setVisibility(View.GONE);
		        	_stopView.setVisibility(View.GONE);
		        	_startView.setVisibility(View.VISIBLE);
		        	playSongLayout.setEnabled(true);
		        	mItem.isPlayedClicked(false);
		        	invalidateList();
				}
				try {
						mPlayer.prepare();
					
				} catch (Exception e) {
					System.out.println("Error Initalizing");
					showToast(getResources().getString(R.string.ccm_removesong_error_playing_track));
					_progressBar.setVisibility(View.GONE);
		        	_stopView.setVisibility(View.GONE);
		        	_startView.setVisibility(View.VISIBLE);
		        	playSongLayout.setEnabled(true);
		        	mItem.isPlayedClicked(false);
		        	invalidateList();
				}
				mPlayer.setOnErrorListener(new OnErrorListener() {
					
					public boolean onError(MediaPlayer mp, int what, int extra) {
						// TODO Auto-generated method stub
						//Toast.makeText(getApplicationContext(), "what: "+what+": extra"+extra, Toast.LENGTH_LONG).show();
						return false;
					}
				});
				mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					  public void onPrepared(MediaPlayer mp) {
						  cntr_aCounter = new CountDownTimer(30000, 1000) {
						        public void onTick(final long millisUntilFinished) {
						        	//mPlayText.setText(getResources().getString(R.string.ccm_removesong_preview_song));
						        	mPlayer.start();
						        	isPreviewPlaying =false;
						        	CCMSongRemoval.this.mPlayer= mPlayer;
						        	 // TODO do your thing
						        	startProgressBar(_progressBar , mPlayer,index);
						        }   
						        public void onFinish() {
						            //code fire after finish
						        	//mPlayText.setText(getResources().getString(R.string.ccm_removesong_play_song));
						        	mPlayer.stop();
						        	mPlayer.release();
						        	mPlayer=null;
						        	_progressBar.setVisibility(View.GONE);
						        	_stopView.setVisibility(View.GONE);
						        	_startView.setVisibility(View.VISIBLE);
						        	playSongLayout.setEnabled(true);
						        	mItem.isPlayedClicked(false);
						        	isPreviewPlaying = true;
						        	invalidateList();
						        	
						        }
						        };cntr_aCounter.start();
					  }
					});
				/*mPlayer.setOnPreparedListener(new OnPreparedListener() {
			        public void onPrepared(MediaPlayer mp) {
			            
			        	
			           
			        }
			    });*/
			}
		}
		}
	}
    
    public void startProgressBar(final ProgressBar pb,final MediaPlayer mp,final int index)
	{
		 // create a thread for updating the progress bar
        Thread background = new Thread (new Runnable() {
           public void run() {
               // enter the code to be run while displaying the progressbar.
			   // This example is just going to increment the progress bar:
			   // So keep running until the progress value reaches maximum value
        	   while (i<= 30) {
        		   try{
        			   // TODO Auto-generated method stub
						i=(mp.getCurrentPosition()/1000)%60;
						boundsList.add(index, i);
						activity.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								try{
								pb.setProgress(boundsList.get(index));
								}catch(Exception e){
									System.out.println("progress bar error");
								}
							}
						});
					}catch(Exception e)
        		   {
        			   System.out.println("postion set error");
        		   }
			   }
        	}
        });
         
        // start the background thread
        background.start();
	}
    
    /**
     *  Downloads track and stores in the name of temp for playing and replaces it on next call
     * @author Sourabh
     * @param context
     * @param url
     * @param fileName
     * @return
     */
	public String downloadResource(Context context, final String url,
			String fileName) {

		String filePath = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URL resourceUrl = new URL(url);
			URLConnection connection = resourceUrl.openConnection();
			connection.setReadTimeout(10000); // enables throw
												// SocketTimeoutException
			is = connection.getInputStream();
			File file = new File(context.getFilesDir(), fileName);
			int i = 1;
			while (file.exists()) {
				file.delete();
				file = new File(context.getFilesDir(), fileName);
			}
			if (!file.exists()) {
				file.createNewFile();
			}

			fos = new FileOutputStream(file);
			// Read bytes to the Buffer until there is nothing more to read(-1).
			byte[] data = new byte[20000];
			int nRead;
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				fos.write(data, 0, nRead);
			}

			filePath = file.getPath();
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {

			}
		}
		return filePath;
	}

    public int getItemPostion() {
        return itemPostion;
    }
    public void setItemPostion(int itemPostion) {
        this.itemPostion = itemPostion;
    }
    
    public SongRemovalModal getPreviousItem() {
        return previousItem;
    }
    public void setPreviousItem(SongRemovalModal previousItem) {
        this.previousItem = previousItem;
    }
    
    private void invalidateList(){
        listView.invalidate();
        listAdapter.notifyDataSetChanged();
        /*if(_DownImageLoader!=null)
        {
        _DownImageLoader.clearCache();
        }*/
        
        for(int i=0;i<itemList.size();i++)
    	{
    		boundsList.add(0);
    	}
    }

    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	try{
    	if(mPlayer!=null && mPlayer.isPlaying())
    	{
    		mPlayer.stop();
    	}

    	}catch(Exception e)
		{
			System.out.println("Force stop on back press");
		}
    	playMusic();
    }
    
    private void stopMusic(){
        
        if(mService==null){
            mService=CCMService.getInstance();
        }
        if(mService!=null && mService.isPlaying()){
            mService.pauseMusic();
            isMusicStopPlaying=true;
        }
    }
    
    private void playMusic(){
        if(isMusicStopPlaying){
            if(mService==null){
                mService=CCMService.getInstance();
            }
            
            if(mService!=null){
                mService.startMusic();
            }
         }
    }
    
}
