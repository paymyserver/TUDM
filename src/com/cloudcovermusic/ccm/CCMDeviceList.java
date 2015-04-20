package com.cloudcovermusic.ccm;

import android.R.animator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.cloudcovermusic.json.JsonParser;
import com.android.cloudcovermusic.model.ActivatePlayerResponse;
import com.android.cloudcovermusic.model.ErrorResponse;
import com.android.cloudcovermusic.model.GetTrackResponse;
import com.android.cloudcovermusic.model.DeviceListItems;
import com.android.cloudcovermusic.utils.CCMException;
import com.android.cloudcovermusic.utils.Constants;
import com.android.cloudcovermusic.utils.ServerUtilities;
import com.android.cloudcovermusic.utils.ServerUtilities.ResponseData;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class to show device list and activated them from the list
 * @author Sourabh
 *
 */
public class CCMDeviceList extends Activity {

	private static final String TAG = CCMPlayer.class.getSimpleName();
	private ListView list;
	Context context;
	public ArrayList<DeviceListItems> str_names;
	Dialog mDialog;
	String mDeviceId;
	MySimpleArrayAdapter adapter;
	int mPosition=0;
	Typeface mRobotoRegular,mRobotoBold;
	private TextView txtHeader;
    private ImageView backBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ccm_devices_list_screen_layout);
		context = CCMDeviceList.this;
		list = (ListView) findViewById(R.id.listView1);
		
		mRobotoRegular = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Regular.ttf");
		mRobotoBold = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Bold.ttf");
		txtHeader = (TextView) findViewById(R.id.txtHeader);
        backBtn =  (ImageView) findViewById(R.id.imageBack);
        txtHeader.setText(getString(R.string.ccm_player_manage_device_title));
        txtHeader.setTypeface(mRobotoRegular);
		str_names = new ArrayList<DeviceListItems>();
		mDialog= new Dialog(CCMDeviceList.this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	mDialog.setContentView(R.layout.ccm_activateuser_dialog_layout);
		getDevicesList mGetDevicesList = new getDevicesList();
		mGetDevicesList.execute();
		
		backBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	class getDevicesList extends AsyncTask<Void, Void, String>
	{
		ProgressDialog mAsyncDialog;
		 String error=null;
		 String sRet=null;
		public getDevicesList() {
			mAsyncDialog = new ProgressDialog(CCMDeviceList.this);
		}

		@Override
		protected void onPreExecute() {
			mAsyncDialog
			.setMessage(getString(R.string.ccm_getting_list_message));
			mAsyncDialog.setCancelable(false);
			mAsyncDialog.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {

			try {
			    
			    if(!ServerUtilities.isNetworkAvailable(CCMDeviceList.this)){
                    error=getResources().getString(R.string.error_network_unavailable);
                    return error;
                }
			    
				ResponseData response = sendDeviceList();

				if (response != null) {
					JSONObject result = response.getResponseJson();
					// Check if its success or failure.
					if (response.isSuccess()) {
						JSONArray deviceArray = result.getJSONArray(Constants.REGISTER_DEVICE_API);
						for(int iLoop=0;iLoop<deviceArray.length();iLoop++)
						{
							//DeviceList deviceList = (DeviceList) JsonParser.parseJsonToType(getApplicationContext(), deviceArray.getJSONObject(iLoop),new TypeToken<DeviceList>(){}.getType() );
							/*DeviceList deviceList = (DeviceList) JsonParser
                            .parseJsonToType(
                                    getApplicationContext(),
                                    result,
                                    new TypeToken<DeviceList>() {
                                    }.getType());*/

							JSONObject deviceObject = deviceArray.getJSONObject(iLoop);
							//Passing values to Item call to store details
							str_names.add(new DeviceListItems(deviceObject.get(Constants.PARAM_UUID).toString(), 
									deviceObject.get(Constants.PARAM_ACTIVATE_PLAYER).toString(),
									deviceObject.get(Constants.PARAM_DEVICEID).toString(), 
									deviceObject.get(Constants.PARAM_CUSTID).toString(), 
									deviceObject.get(Constants.PARAM_USERID).toString(), 
									deviceObject.get(Constants.PARAM_DEVICE_NAME).toString(), 
									deviceObject.get(Constants.PARAM_DESCRIPTION).toString(), 
									deviceObject.get(Constants.PARAM_TYPE).toString(), 
									deviceObject.get(Constants.PARAM_LAST_SEEN).toString(), 
									deviceObject.get(Constants.PARAM_ISDELETED).toString()));
						}
						error=null;
						
						for(int iLoop=0;iLoop<str_names.size();iLoop++){
							if(str_names.get(iLoop).getActivePlayer().equals("1")){
								Collections.swap(str_names, iLoop, 0);
							}
						}
					}
					
				}
			} catch (CCMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return error;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(mAsyncDialog!=null)
			{
			    mAsyncDialog.dismiss();
			}
			if(result==null){
				
			    adapter = new MySimpleArrayAdapter(context, R.id.listView1,str_names);// data is String array value to be added in list view
	            //setting the adapter
	            list.setAdapter(adapter);
	            
			}else {
                if (error.equals(getResources().getString(
                        R.string.error_network_unavailable))) {
                    showToast(getResources().getString(
                        R.string.error_network_unavailable));
                } else {
                    showToast(getResources().getString(
                            R.string.ccm_devicelist_error_retriving_list));
                }
                finish();
			} 
		}
	}

	private ResponseData sendDeviceList() throws CCMException {
		// TODO Auto-generated method stub
		Log.d(TAG, "Into sendDeviceList() method");
		Context context = getApplicationContext();
		return ServerUtilities.getInstance(context).sendGetRequest(Constants.DEVICE_LIST_USERS+""+getCustId()+
				Constants.DEVICE_LIST_API + getToken());
	}

	/**
	 * Custom listview Adapter to display devices list
	 * @author Sourabh
	 *
	 */
	
	public class MySimpleArrayAdapter extends ArrayAdapter {
		private final Context context;
		private ArrayList<DeviceListItems> ar_name;
		private int[] ar_image;

		public MySimpleArrayAdapter(Context context, int textViewResourceId,ArrayList<DeviceListItems> str_names) {
			super(context, textViewResourceId, str_names);
			this.context = context;
			this.ar_name = str_names;
			this.ar_image = ar_image;

		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.ccm_devices_list_content, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			//holder.iv_icons.setImageResource(ar_image[position]);
			final DeviceListItems deviceList = (DeviceListItems) ar_name.get(position);
			holder.tvName.setText(deviceList.getName());
			holder.tvType.setText(deviceList.getType());
			holder.tvBulid.setText(deviceList.getDescription());
			String mType=deviceList.getType();
			
			if(mType.contains("Android Tablet") || mType.contains("iPad")){
				holder.iv_icons.setImageResource(R.drawable.ccm_tablet_device_icon);
			}else if(mType.contains("Android Phone") || mType.contains("iPhone") || mType.contains("iPod")){
				holder.iv_icons.setImageResource(R.drawable.ccm_phone_device_icon);
			}else if(mType.contains("PC") || mType.contains("Laptop") || mType.contains("TV")){
				holder.iv_icons.setImageResource(R.drawable.ccm_computer_browser_device);
			}else if(mType.contains("Cloud Box")){
				holder.iv_icons.setImageResource(R.drawable.ccm_box_device_icon);
			}else{
				holder.iv_icons.setImageResource(R.drawable.ccm_unknown_device_icon);
			}
			
			if(deviceList.getActivePlayer().equals("1"))
			{
				holder.rl_itemLayout.setBackgroundColor(getResources().getColor(R.color.ccm_highlight_text_color));
			}else{
				holder.rl_itemLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
			}
			System.out.println("Actiation Player "+deviceList.getActivePlayer());
			System.out.println("Device ID "+deviceList.getDeviceId());
			System.out.println("Device name "+deviceList.getName());
			holder.rl_itemLayout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				    if(!ServerUtilities.isNetworkAvailable(CCMDeviceList.this)){
	                    showToast(getResources().getString(R.string.error_network_unavailable));
	                    return;
	                }
				    if(!deviceList.getActivePlayer().equals("1"))
					{
				    mDeviceId=deviceList.getDeviceId();
    				showDialog(holder.rl_itemLayout);
					}
	                
				}
			});
			
			return convertView;
		}
	}

	private class ViewHolder {
		ImageView iv_icons;
		TextView tvName,tvType,tvBulid ;
		LinearLayout ll_ItemLayout;
		LinearLayout rl_itemLayout;

		ViewHolder(View view) {
			iv_icons = (ImageView) view.findViewById(R.id.typeImage);
			tvName = (TextView) view.findViewById(R.id.name);
			tvType = (TextView) view.findViewById(R.id.type);
			tvBulid = (TextView) view.findViewById(R.id.build);
			ll_ItemLayout = (LinearLayout) view.findViewById(R.id.itemLayout);
			rl_itemLayout = (LinearLayout) view.findViewById(R.id.container);
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
	 * Method to get customer id stored in preference 
	 * @return Cust ID
	 */
	private int getCustId() {
		SharedPreferences pref = getSharedPreferences(
				Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
		int custId = 0;
		if (pref != null) {
			custId = pref.getInt(Constants.PARAM_CUSTID,0);
		}
		return custId;
	}
	
	/**
     * This method will show dialog box for activation of selected device
     * 
     * @param message
     * @author Sourabh
     */
    public void showDialog(final LinearLayout rl_itemLayout) {
    	
        mDialog.show();
        TextView tv_Text = (TextView) mDialog
                .findViewById(R.id.ccm_activate_dialog_text);
        TextView tv_Ok = (TextView) mDialog.findViewById(R.id.ccm_activate_Ok);
        TextView tv_Cancel = (TextView) mDialog.findViewById(R.id.ccm_activate_cancel);

        tv_Text.setText(getResources().getString(R.string.ccm_manage_device_alert_message));
        tv_Ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	ActivatePlayerTaskAsync mActivatePlayerTaskAsync = new ActivatePlayerTaskAsync();
            	mActivatePlayerTaskAsync.execute();
            	invalidateList();
            	//rl_itemLayout.setBackgroundColor(getResources().getColor(R.color.ccm_highlight_text_color));
                mDialog.dismiss();
                
            }
        });
        
        tv_Cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }
    
    /**
     * Async Task to activate the registered device on the server.
     * 
     * 
     */
    class ActivatePlayerTaskAsync extends AsyncTask<Void, Void, String> {
        ActivatePlayerResponse mActivateResponse;
        ErrorResponse mErrorResponse;
        ProgressDialog mAsyncDialog;
        String sRet=null;
        public ActivatePlayerTaskAsync() {
            mAsyncDialog = new ProgressDialog(CCMDeviceList.this);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Into ActivatePlayerTaskAsync onPreExecute()");
            mAsyncDialog
                    .setMessage(getString(R.string.ccm_player_activating_player_progress_message));
            mAsyncDialog.setCancelable(false);
            mAsyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Into ActivatePlayerTaskAsync doInBackground()");
            try {
                ResponseData response = sendActivatePlayerRequest();
                if (response != null) {
                    JSONObject result = response.getResponseJson();
                    // Check if its success or failure.
                    if (response.isSuccess()) {
                        // Parse the json response.
                        mActivateResponse = (ActivatePlayerResponse) JsonParser
                                .parseJsonToType(
                                        getApplicationContext(),
                                        result,
                                        new TypeToken<ActivatePlayerResponse>() {
                                        }.getType());
                    } else {
                        mErrorResponse = (ErrorResponse) JsonParser
                                .parseJsonToType(getApplicationContext(),
                                        result, new TypeToken<ErrorResponse>() {
                                        }.getType());
                        sRet = response.getResponseJson().getString("message");
                        sRet = "Not able to activate device";
    					return sRet;
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (mAsyncDialog != null && mAsyncDialog.isShowing()) {
                mAsyncDialog.dismiss();
            }
            if(sRet!=null){
            showToast(""+sRet);
            }
            String errorString = null;
            Log.d(TAG, "Into GetCommandTaskAsync onPostExecute()");
            if (mActivateResponse != null) {
                int activateStatus = mActivateResponse.getActiveStatus();
                if (activateStatus == ActivatePlayerResponse.PLAYER_STATUS_ACTIVE) {
                	showToast("Activated");
                } else { // Error
                    errorString = getString(R.string.ccm_player_player_undefined_message);
                    //showToast(errorString);
                }
            } else {
                // Error
                errorString = getString(R.string.ccm_player_player_undefined_message);
                //showToast(errorString);
            }
            if (errorString != null) {
            	
            	//showToast(getString(R.string.ccm_devicelist_successfully_activated));
            	str_names.clear();
                getDevicesList mGetDevicesList = new getDevicesList();
        		mGetDevicesList.execute();
            }
        }
    }
    
    /**
     * Sends a {@link HttpPut} request to activate the registered device.
     * 
     * @return {@link ResponseData}
     * @throws CCMException
     */
    protected ResponseData sendActivatePlayerRequest() throws CCMException {
        Log.d(TAG, "Into sendActivatePlayerRequest() method");
        Context context = getApplicationContext();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(
                Constants.PARAM_DEVICE_AUTH_TOKEN, getToken()));
        return ServerUtilities.getInstance(context).sendPutRequest(
        		Constants.REGISTER_DEVICE_API+"/"+mDeviceId+"/"+
                        Constants.ACTIVATE_PLAYER_API_TOKEN, nameValuePairs);
        
    }
    
    private void invalidateList(){
    	list.invalidate();
    	adapter.notifyDataSetChanged();
        
    }
    
    public void showToast(final String message)
    {
    	runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_LONG).show();
			}}
    		);
    		
    }
    
}
