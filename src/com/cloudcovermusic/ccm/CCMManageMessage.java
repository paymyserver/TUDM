package com.cloudcovermusic.ccm;
/***
 * Using this class you can activate message preset and stop playing preset message
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.cloudcovermusic.json.JsonParser;
import com.android.cloudcovermusic.model.MeModal;
import com.android.cloudcovermusic.model.PresetMessage;
import com.android.cloudcovermusic.model.SongRemovalModal;
import com.android.cloudcovermusic.utils.CCMException;
import com.android.cloudcovermusic.utils.CCMUtils;
import com.android.cloudcovermusic.utils.Constants;
import com.android.cloudcovermusic.utils.ServerUtilities;
import com.android.cloudcovermusic.utils.ServerUtilities.ResponseData;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import android.R.color;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CCMManageMessage extends Activity {
	private static final String TAG = CCMManageMessage.class.getSimpleName();
	
	/**
	 * Instance of {@link AsyncManageMessage}
	 * Asynctask to get preset message from server
	 */
	private AsyncManageMessage asyncManageMessage;
	/**
	 * Instance of {@link ManageMessageAdapter}
	 * Holds data for list view
	 */
	private ManageMessageAdapter manageMessageAdapter;
	public List<PresetMessage> listItem;
	private ListView mListView;
	// add custom font for text view
	private Typeface mRobotoRegular, mRobotoBold;
	private String presetId;
	private String userId;

	MeModal meModal;
	
	// Show dialog 
    private Dialog mDialog;
    
	private TextView txtHeader;
    private ImageView backBtn;
    private ImageView cancelImageView;
    
    boolean isPresetRegister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ccm_manage_message_screen_layout);
		init();
		
		getActiveMessage();
	}

	/**
	 * Initilize all views
	 */
	private void init() {
		mRobotoRegular = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Regular.ttf");
		mRobotoBold = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Bold.ttf");
		mListView = (ListView) findViewById(R.id.manageMessageList);
		txtHeader = (TextView) findViewById(R.id.txtHeader);
        backBtn =  (ImageView) findViewById(R.id.imageBack);
        txtHeader.setText(getString(R.string.ccm_msong_title));
        txtHeader.setTypeface(mRobotoRegular);
		
		listItem = new ArrayList<PresetMessage>();
		manageMessageAdapter = new ManageMessageAdapter(
				getApplicationContext(), R.layout.ccm_manage_song_layout,
				listItem);
		mListView.setAdapter(manageMessageAdapter);
		
		mDialog = new Dialog(CCMManageMessage.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.ccm_activateuser_dialog_layout);

        backBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
        cancelImageView = (ImageView) findViewById(R.id.manageMessageCancel);
        cancelImageView.setVisibility(View.VISIBLE);
		cancelImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog();
			}
		});
	}
	

	/**
	 * get list of preset messages
	 */
	private void getManageMessage() {
		asyncManageMessage = new AsyncManageMessage();
		asyncManageMessage.execute();
	}

	/**
	 * get active preset from server
	 */
	private void getActiveMessage() {

		new AsyncActivMessages().execute();

	}
	
	/**
	 * This activate the preset message on server
	 */
	private void getActivatePresetMessage(){
		new AsyncActivatePresetMessage().execute();
	}

	/**
	 * This async task get the active preset message and song id from server
	 * @author rushikesh
	 *
	 */
	private class AsyncActivMessages extends AsyncTask<Void, Void, String> {

		String error = null;
		ProgressDialog asyncProgressDialog;
		
		AsyncActivMessages(){
			asyncProgressDialog=new ProgressDialog(CCMManageMessage.this);
			asyncProgressDialog.setMessage(getResources().getString(
					R.string.ccm_player_error_playing_track_message));
			asyncProgressDialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			if (asyncProgressDialog != null) {
				asyncProgressDialog.show();
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			Log.d(TAG, "get active message doInBackGround");
			if (!ServerUtilities.isNetworkAvailable(CCMManageMessage.this)) {
				error = getResources().getString(
						R.string.error_network_unavailable);

			}

			try {
				ResponseData responseData = sentActiveMessageRequest();

				if (responseData != null && responseData.isSuccess()) {

					meModal = (MeModal) JsonParser.parseJsonToType(
							getApplicationContext(),
							responseData.getResponseJson(),
							new TypeToken<MeModal>() {
							}.getType());

					error = null;
				} else {
					error = getResources().getString(
							R.string.ccm_msong_list_unsuccessful);
					finish();
				}

			} catch (CCMException e) {
				e.printStackTrace();
			}

			return error;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result == null) {
				
				getManageMessage();
			} else {
				showToast(error);
				finish();
			}
			 
			if(asyncProgressDialog!=null){
				asyncProgressDialog.dismiss();
			}
		}
	}

	
/**
 * This will get the list of preset messages from server
 * @author rushikesh
 *
 */
	private class AsyncManageMessage extends AsyncTask<Void, Void, String> {
		private ProgressDialog asyncProgress;
		String error = null;

		public AsyncManageMessage() {

			asyncProgress = new ProgressDialog(CCMManageMessage.this);
			asyncProgress.setMessage(getResources().getString(
					R.string.ccm_player_error_playing_track_message));
			asyncProgress.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (asyncProgress != null) {
				asyncProgress.show();
			}
			if(listItem!=null){
				listItem.clear();
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			Log.d(TAG, "doInBackGround");
			if (!ServerUtilities.isNetworkAvailable(CCMManageMessage.this)) {
				error = getResources().getString(
						R.string.error_network_unavailable);
				return error;
			}

			try {
				ResponseData response = sentManageMessageRequest();
				Log.d(TAG, "response >>> "
						+ response.getResponseJson().toString());
				if (response != null && response.isSuccess()) {

					JSONObject result = response.getResponseJson();
					if (result != null) {
						try {
							PresetMessage presetMessage;
							isPresetRegister=result.getBoolean(Constants.MESSAGE_SUBSCRIPTION);
							JSONArray jsonArray = result
									.getJSONArray(Constants.PRESETS);
							
							for (int i = 0; i < jsonArray.length(); i++) {

								presetMessage = (PresetMessage) JsonParser
										.parseJsonToType(
												getApplicationContext(),
												jsonArray.getJSONObject(i),
												new TypeToken<PresetMessage>() {
												}.getType());

								if (presetMessage != null) {
									listItem.add(presetMessage);

								}
							}

							error = null;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							error = getResources().getString(
									R.string.ccm_msong_list_unsuccessful);
							e.printStackTrace();
						}

					} else {
						error = getResources().getString(
								R.string.ccm_msong_list_unsuccessful);
					}
				} else {
					error = getResources().getString(
							R.string.ccm_msong_list_unsuccessful);
				}
			} catch (CCMException e) {
				error = getResources().getString(
						R.string.ccm_msong_list_unsuccessful);
				e.printStackTrace();
			}

			return error;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result == null) {
				
				if(isPresetRegister){
					
					if(listItem!=null && listItem.size()==0){
						
						showNoPrestDialog(getResources().getString(R.string.ccm_msong_no_preset));
						
					}else{
						
					manageMessageAdapter.notifyDataSetChanged();
					mListView.invalidate();
					
					}
					
				}else{
					showNoPrestDialog(getResources().getString(R.string.ccm_msong_no_pre_register));
				}
				
			} else {
				
				showToast(result);
				finish();

			}

			if (asyncProgress != null) {
				asyncProgress.dismiss();
			}

		}

	}

	/**
	 * Activate preset messages on server
	 * @author rushikesh
	 *
	 */
	private class AsyncActivatePresetMessage extends AsyncTask<Void, Void, String>{
		
		ProgressDialog asynDialog;
		
		String error;
		
		public AsyncActivatePresetMessage() {
			// TODO Auto-generated constructor stub
			
			asynDialog=new ProgressDialog(CCMManageMessage.this);
			asynDialog.setMessage(getResources().getString(R.string.ccm_player_error_playing_track_message));
			asynDialog.setCancelable(false);
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			if(asynDialog!=null){
				asynDialog.show();
			}
		}
		
		@Override
		protected String doInBackground(Void... params) {
			
			if (!ServerUtilities.isNetworkAvailable(CCMManageMessage.this)) {
				error = getResources().getString(
						R.string.error_network_unavailable);
				return error;
			}
		
			try {
				ResponseData responseData=sentActivatePresetMessage();
				
				if(responseData!=null && responseData.isSuccess()){
					error=null;
				}else{
					error=getResources().getString(R.string.ccm_msong_failed_preset_active);
				}
				
			} catch (CCMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error=getResources().getString(R.string.ccm_msong_failed_preset_active);;
			}
			
			
			return error;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if(asynDialog!=null){
				asynDialog.dismiss();
			}
			
			if(result==null){
				getActiveMessage();
			}else{
				showToast(result);
			}
		}
	}
	private ResponseData sentManageMessageRequest() throws CCMException {
		Context context = getApplicationContext();
		return ServerUtilities.getInstance(context).sendGetRequest(
				Constants.MANAGE_MESSAGE + getToken());

	}

	
	private ResponseData sentActivatePresetMessage() throws CCMException{
	Context context=getApplicationContext();
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs.add(new BasicNameValuePair(
			Constants.PARAM_DEVICE_AUTH_TOKEN, getToken()));

	int userId=CCMUtils.getUserId(CCMManageMessage.this);
	String url=Constants.PARAM_USERS+userId+Constants.PARAM_MESSAGE_PRESET_SUFFIX+Constants.TOKEN_SUFFIX+getToken()+"&"+Constants.PARAM_MESSAGE_PRESET+presetId;

	
	return ServerUtilities.getInstance(context).sendPutRequest(url);
	}
	
	
	private ResponseData sentActiveMessageRequest() throws CCMException {
		Context context = getApplicationContext();
		return ServerUtilities.getInstance(context).sendGetRequest(
				Constants.ME + getToken());

	}

	private ResponseData sentRemovePresetMessage() throws CCMException {

		Context context = getApplicationContext();
		int userId=CCMUtils.getUserId(context);
		
		String uri=Constants.PARAM_USERS+userId+Constants.PARAM_MESSAGE_PRESET_SUFFIX+Constants.TOKEN_SUFFIX+getToken();
		 
			return ServerUtilities.getInstance(context).sendDeleteRequest(uri);
		 

	}

	private void noPresetmessage() {
		new RemovePresetMessage().execute();
	}

	/**
	 * Stop palying preset messsages 
	 * No preset message
	 * @author rushikesh
	 *
	 */
	private class RemovePresetMessage extends AsyncTask<Void, Void, String> {

		String error = "";
		ProgressDialog asyncDialog;

		public RemovePresetMessage() {

			asyncDialog = new ProgressDialog(CCMManageMessage.this);
			asyncDialog.setMessage(getResources().getString(
					R.string.ccm_player_error_playing_track_message));
			asyncDialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (asyncDialog != null) {
				asyncDialog.show();
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			Log.d(TAG, "doInBackGround");
			if (!ServerUtilities.isNetworkAvailable(CCMManageMessage.this)) {
				error = getResources().getString(
						R.string.error_network_unavailable);
				return error;
			}

			ResponseData responseData=null;
			try {
				responseData = sentRemovePresetMessage();
				
				if (responseData!=null&&responseData.isSuccess()) {

					error = null;

				}else{
					error=getResources().getString(R.string.ccm_msong_failed_preset_remove);
				}
				
			} catch (CCMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error=getResources().getString(R.string.ccm_msong_failed_preset_remove);
			}

			
			return error;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result == null) {

				getActiveMessage();

			} else {
				showToast(result);
			}

			if (asyncDialog != null) {
				asyncDialog.dismiss();
			}
		}
	}
	
	/**
	 * create list of preset messages
	 * @author rushikesh
	 *
	 */

	private class ManageMessageAdapter extends ArrayAdapter {
		List<PresetMessage> itemList;
		Context context;

		public ManageMessageAdapter(Context context, int resource,
				List<PresetMessage> itemList) {
			super(context, resource, itemList);
			this.itemList = itemList;
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return itemList.size();
		}

		@Override
		public PresetMessage getItem(int position) {
			// TODO Auto-generated method stub
			return itemList.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final ViewHolder holder;
			final PresetMessage presetMessage;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.ccm_manage_song_layout,
						null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();

			}

			presetMessage = getItem(position);

			if (presetMessage != null) {

				holder.presetMessageName.setTypeface(mRobotoBold);
				holder.presetMessageName.setText(presetMessage.getName());
				holder.selectTextView.setTypeface(mRobotoRegular);
				holder.activePresetMessage.setTypeface(mRobotoRegular);
				holder.activePresetMessage.setText(presetMessage.getMessages());
				if (meModal != null) {
					if (presetMessage.getPreserId().equals(
							meModal.getMessagePreset())) {

						holder.selectTextView.setVisibility(View.GONE);
						holder.checkImage.setVisibility(View.VISIBLE);
						holder.manageSongLayout.setBackgroundColor(getResources().getColor(R.color.ccm_highlight_text_color));

					} else {
						holder.selectTextView.setVisibility(View.VISIBLE);
						holder.checkImage.setVisibility(View.GONE);
						holder.manageSongLayout.setBackgroundColor(color.white);
					}
				}

				holder.selectLinearLayout
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {

								presetId=presetMessage.getPreserId();
								getActivatePresetMessage();
								

							}
						});

			}

			return convertView;
		}

	}

	private static class ViewHolder {
		TextView presetMessageName;
		TextView selectTextView;
		TextView activePresetMessage;
		RelativeLayout selectLinearLayout;
		LinearLayout manageSongLayout;
		ImageView checkImage;

		ViewHolder(View view) {

			presetMessageName = (TextView) view
					.findViewById(R.id.manageSongListTitle);
			selectLinearLayout = (RelativeLayout) view
					.findViewById(R.id.manageSongSelect);
			selectTextView = (TextView) view
					.findViewById(R.id.manageSongeSelectTv);
			activePresetMessage = (TextView) view
					.findViewById(R.id.manageSongeActiveTv);
			manageSongLayout=(LinearLayout) view
					.findViewById(R.id.manageSongLayout);
			checkImage = (ImageView) view
					.findViewById(R.id.manageSongCheckImg);
		}
	}

	private void showToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(CCMManageMessage.this, msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	public String getToken() {
		SharedPreferences pref = getSharedPreferences(
				Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
		String token = null;
		if (pref != null) {
			token = pref.getString(Constants.PARAM_DEVICE_AUTH_TOKEN, "");
		}
		return token;
	}
	
	
	
	 public void showDialog() {
	        TextView ok = (TextView) mDialog.findViewById(R.id.ccm_activate_Ok);
	        TextView cancel=(TextView) mDialog.findViewById(R.id.ccm_activate_cancel);
	        TextView message=(TextView) mDialog.findViewById(R.id.ccm_activate_dialog_text);
	        message.setText(Html
	                .fromHtml(getString(R.string.ccm_msong_playno_messages)));
	        
	        mDialog.show();
	        ok.setOnClickListener(new View.OnClickListener() {

	            @Override
	            public void onClick(View v) {
	                mDialog.dismiss();
	                noPresetmessage();
	                
	            }
	        });
	        
	        cancel.setOnClickListener(new View.OnClickListener() {

	            @Override
	            public void onClick(View v) {
	               mDialog.dismiss();
	            }
	        });
	        
	        
	    }
	 
	 
	 /**
	     * This method will show dialog box which will display the response of
	     * success or failure
	     * 
	     * @param message
	     * @author Sourabh
	     */
	    public void showNoPrestDialog(String message) {
	     
	    	mDialog.setContentView(R.layout.ccm_forgotpwd_dialog_layout);
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
	                finish();
	            }
	        });
	    }


}
