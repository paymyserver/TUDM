package com.android.cloudcovermusic.utils;

import java.io.File;
import java.util.UUID;

import com.cloudcovermusic.ccm.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class CCMUtils {
	private static final String TAG = CCMUtils.class.getSimpleName();

	/**
	 * Generates a unique identifier for the device.
	 * 
	 * @param context
	 * @param username If not null, prefix it to the uuid.
	 * @return
	 */
	public static String getUUID(Context context, String username) {
		String deviceId = "";
		final TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		// Append a prefix to differentiate the same deviceId for different
		// user. It will let a same device to be registered with different
		// users.
		String prefix = !TextUtils.isEmpty(username) ? username + "-" : "";
		deviceId = prefix + deviceUuid.toString();
		Log.d(TAG, "getUUID() deviceId : " + deviceId);
		return deviceId;
	}

	private static String capitalize(String s) {
		if (TextUtils.isEmpty(s)) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	public static boolean isDevelopmentAppVersion(Context context) {
		boolean isDevVersion = false;
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			String appVersion = packageInfo.versionName;
			String versionIncrementalNo = appVersion.substring(appVersion
					.lastIndexOf(".") + 1);
			if (!TextUtils.isEmpty(versionIncrementalNo)) {
				isDevVersion = (Integer.parseInt(versionIncrementalNo) % 2) != 0;
			}
		} catch (NameNotFoundException e) {
			// should never happen
		}
		return isDevVersion;
	}

	/**
	 * Sets a {@link Drawable} as a background to the specified {@link View}. It
	 * checks for the API level and uses either
	 * {@link View#setBackground(Drawable)} or
	 * {@link View#setBackgroundDrawable(Drawable)}
	 * 
	 * @param view
	 * @param drawable
	 */
	@SuppressLint("NewApi") public static void setViewBackgroundDrawable(View view, Drawable drawable) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(drawable);
		} else {
			// Version previous than JellyBean.
			view.setBackgroundDrawable(drawable);
		}
	}

	/**
	 * Removes special characters from the passed string and return a filtered
	 * string.
	 * 
	 * @param str
	 * @return
	 */
	public static String removeSpecialCharacters(String str) {
		String temp = "";
		for (char x : str.toCharArray()) {
			if ((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z')
					|| (x >= 0 && x <= 9) || (x == '_')) {
				temp += x;
			}
		}
		return temp;
	}

	/*public int getVersionName(Context context)
    {
    	PackageInfo pInfo = null;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int version = Integer.parseInt(pInfo.versionName);
		return version;

    }*/

	/**
	 * this method check if a debug file is present in device memory to put the app on debug mode
	 * 
	 * @return true if file is present
	 */
	public static boolean checkDebugFile()
	{
		boolean fileExist=false;
		File file = new File(""+Environment.getExternalStorageDirectory());
		String[] fileList=file.list();
		for (int iLoop = 0; iLoop < fileList.length; iLoop++) {
			if (fileList[iLoop].equals("debug.txt")) {
				fileExist=true;
			}
		}
		return fileExist;
	}

	/**
	 * Gives device resolution height X width
	 * @author Sourabh
	 * @param act
	 * @return
	 */
	public static String getDeviceResolution(Activity act)
	{
		int height,width;
		DisplayMetrics displaymetrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		height = displaymetrics.heightPixels;
		width = displaymetrics.widthPixels;
		return height+"X"+width;
	}

	/**
	 * gives sdk version and device app version eg 1.2,4.0 and app v1.0
	 * @author Sourabh
	 * @param context
	 * @return
	 */
	public static String getDeviceAndSdkVersion(Context context)
	{
		String version;
		PackageInfo pInfo = null;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		version = pInfo.versionName;
		String sdkVersion = android.os.Build.VERSION.RELEASE;
		return "Android "+sdkVersion+" Cloud Cover v"+version;
	}

	/**
	 * gives a login token
	 * @param context
	 * @return
	 */
	public static String getToken(Context context) 
	{
		SharedPreferences pref = context.getSharedPreferences(
				Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
		String token = null;
		if (pref != null) {
			token = pref.getString(Constants.PARAM_DEVICE_AUTH_TOKEN, "");
		}
		return token;
	}

	/**
     * get the user id of logged in
     * @param context
     * @return
     */
	public static int getUserId(Context context){

		SharedPreferences pref = context.getSharedPreferences(
				Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
		int userId = 0;
		if (pref != null) {
			userId = pref.getInt(Constants.PARAM_USERID, 0);
		}
		return userId;
	}
	
	public static int getCustId(Context context){

		SharedPreferences pref = context.getSharedPreferences(
				Constants.CLOUD_COVER_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
		int custid = 0;
		if (pref != null) {
			custid = pref.getInt(Constants.PARAM_CUSTID, 0);
		}
		return custid;
	}
	
	public static void ShowMessage(Context context,String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	public static String deviceDetails(Context context){
		SharedPreferences mPref = context.getSharedPreferences(Constants.CLOUD_COVER_PLAYER_PREFERENCE,
				              Context.MODE_PRIVATE);
				 String username = mPref.getString(Constants.PARAM_USERNAME, "");
				 
		return "&"+RemoveSpace(CCMUtils.getDeviceAndSdkVersion(context)+"_"+CCMUtils.getUUID(context,username)+
				"_"+context.getString(R.string.screen_type)+"_"+getDeviceName());
    	
    }
    public static String RemoveSpace(String text){
    	return text.replace(" ", "_");
    }
}
