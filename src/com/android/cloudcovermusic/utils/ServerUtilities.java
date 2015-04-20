package com.android.cloudcovermusic.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.cloudcovermusic.ccm.CCMPlayer;
import com.cloudcovermusic.ccm.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public final class ServerUtilities {

    private static final String TAG = ServerUtilities.class.getSimpleName();

    public static ServerUtilities instance = null;

    private HttpClient mClient = null;
    private String mServerDomain = "";
    /**
     * Connection timeout. 60 seconds
     */
    private static final int HTTP_CONNECTION_TIMEOUT = 60000;
    private static final int MAX_RETRY_COUNT = 3;
    private static final int MIN_RETRY_SLEEP_TIME = 3000;
    protected ServerUtilities() {
    }

    protected ServerUtilities(Context context) {
        Log.d(TAG, "Into DeviceManager constructor");

        mClient = getNewHttpClient();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref != null) {
            mServerDomain = pref.getString(Constants.PREF_KEY_SERVER_DOMAIN,
                    Constants.SERVER_URL);
        } else {
            mServerDomain = Constants.SERVER_URL;
        }
    }

    public static synchronized ServerUtilities getInstance(Context context) {
        if (instance == null) {
            instance = new ServerUtilities(context);
        }
        return instance;
    }

    public HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);

            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            // Set the HTTP Connection timeout.
            HttpConnectionParams.setConnectionTimeout(params,
                    HTTP_CONNECTION_TIMEOUT);
            // Set the Socket connection timeout.
            HttpConnectionParams.setSoTimeout(params, HTTP_CONNECTION_TIMEOUT);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(
                    params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public synchronized ResponseData sendPostRequest(final String uri,
            List<NameValuePair> params) throws CCMException {
        Log.d(TAG, "Into sendPostRequest() method for uri : " + mServerDomain + uri
                + " and params : " + params);
        HttpPost post = new HttpPost(mServerDomain + uri);
        ResponseData responseData = null;
        int tries = 0;
        try {
            if (params != null && params.size() > 0) {
                post.setEntity(new UrlEncodedFormEntity(params));
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
        }
        HttpResponse response = null;
        try {
            while (response == null && tries < MAX_RETRY_COUNT) {
                try {
                    response = mClient.execute(post);
                } catch (IOException e) {
                    // maybe just try again...
                    tries++;
                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
                    try {
                        // life is too short for exponential backoff
                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (response != null) {
                responseData = prepareServerResponse(response);
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
        }
        return responseData;
    }

    public synchronized ResponseData sendPutRequest(final String uri,
            List<NameValuePair> params) throws CCMException {
        Log.d(TAG, "Into sendPutRequest() method for uri : " + mServerDomain + uri
                + " and params : " + params);
        HttpPut put = new HttpPut(mServerDomain + uri);
        ResponseData responseData = null;
        int tries = 0;
        try {
            if (params != null && params.size() > 0) {
                put.setEntity(new UrlEncodedFormEntity(params));
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
        }
        HttpResponse response = null;
        try {
            while (response == null && tries < MAX_RETRY_COUNT) {
                try {
                    response = mClient.execute(put);
                } catch (IOException e) {
                    // maybe just try again...
                    tries++;
                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
                    try {
                        // life is too short for exponential backoff
                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (response != null) {
                responseData = prepareServerResponse(response);
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
        }
        return responseData;
    }
    
    
//    public synchronized ResponseData sendPutRequest(final String uri) throws CCMException {
//        Log.d(TAG, "Into sendPutRequest() method for uri : " + mServerDomain + uri);
//        HttpPut put = new HttpPut(mServerDomain + uri);
//        ResponseData responseData = null;
//        int tries = 0;
//
//        HttpResponse response = null;
//        try {
//            while (response == null && tries < MAX_RETRY_COUNT) {
//                try {
//                    response = mClient.execute(put);
//                } catch (IOException e) {
//                    // maybe just try again...
//                    tries++;
//                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
//                    try {
//                        // life is too short for exponential backoff
//                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//            if (response != null) {
//                responseData = prepareServerResponse(response);
//            } else {
//                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
//            }
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
//        }
//        return responseData;
//    }
    

    public synchronized ResponseData sendDeleteRequest(final String uri,
            List<NameValuePair> params) throws CCMException {
        Log.d(TAG, "Into sendDeleteRequest() method for uri : " + mServerDomain + uri
                + " and params : " + params);
        String urlParams = URLEncodedUtils.format(params, "utf-8");
        HttpDelete delete = new HttpDelete(mServerDomain + uri + "?"
                + urlParams);
        ResponseData responseData = null;
        int tries = 0;
        HttpResponse response = null;
        try {
            while (response == null && tries < MAX_RETRY_COUNT) {
                try {
                    response = mClient.execute(delete);
                } catch (IOException e) {
                    // maybe just try again...
                    tries++;
                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
                    try {
                        // life is too short for exponential backoff
                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (response != null) {
                responseData = prepareServerResponse(response);
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
        }
        return responseData;
    }
    
    public synchronized ResponseData sendDeleteRequest(final String uri) throws CCMException {
        Log.d(TAG, "Into sendDeleteRequest() method for uri : " + mServerDomain);
        HttpDelete delete = new HttpDelete(mServerDomain + uri);
        ResponseData responseData = null;
        int tries = 0;
        HttpResponse response = null;
        try {
            while (response == null && tries < MAX_RETRY_COUNT) {
                try {
                    response = mClient.execute(delete);
                } catch (IOException e) {
                    // maybe just try again...
                    tries++;
                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
                    try {
                        // life is too short for exponential backoff
                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (response != null) {
                responseData = prepareServerResponse(response);
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
        }
        return responseData;
    }


    public synchronized ResponseData sendGetRequest(final String uri)
            throws CCMException {
        Log.d(TAG, "Into sendGetRequest() method for uri : " + mServerDomain + uri);
        HttpGet httpGet = new HttpGet(mServerDomain + uri);
        ResponseData responseData = null;
        HttpResponse response = null;
        int tries = 0;
        try {
            while (response == null && tries < MAX_RETRY_COUNT) {
                try {
                    response = mClient.execute(httpGet);
                } catch (IOException e) {
                    // maybe just try again...
                    tries++;
                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
                    try {
                        // life is too short for exponential backoff
                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (response != null) {
                responseData = prepareServerResponse(response);
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
        }
        return responseData;
    }
    
    public synchronized ResponseData sendPutRequest(final String uri)throws CCMException {
        Log.d(TAG, "Into sendGetRequest() method for uri : " + mServerDomain + uri);
        HttpPut httpPut = new HttpPut(mServerDomain + uri);
     //   httpGet.setHeader("X-HTTP-Method-Override", "PUT");
        ResponseData responseData = null;
        HttpResponse response = null;
        int tries = 0;
        try {
            while (response == null && tries < MAX_RETRY_COUNT) {
                try {
                    response = mClient.execute(httpPut);
                } catch (IOException e) {
                    // maybe just try again...
                    tries++;
                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
                    try {
                        // life is too short for exponential backoff
                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (response != null) {
                responseData = prepareServerResponse(response);
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
        }
        return responseData;
    }

    public synchronized ResponseData sendPutRequest(final String uri,
            final Header[] headers, final String json) throws CCMException {
        Log.d(TAG, "Into sendPutRequest() method for uri : " + mServerDomain + uri
                + " and JSON : " + json);
        HttpPut httpPut = new HttpPut(mServerDomain + uri);
        ResponseData responseData = null;
        StringEntity se = null;
        int tries = 0;
        try {
            se = new StringEntity(json, HTTP.UTF_8);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            httpPut.setEntity(se);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
        }
        // Add headers
        if (headers != null && headers.length > 0) {
            httpPut.setHeaders(headers);
        }
        HttpResponse response = null;
        try {
            while (response == null && tries < MAX_RETRY_COUNT) {
                try {
                    response = mClient.execute(httpPut);
                } catch (IOException e) {
                    // maybe just try again...
                    tries++;
                    Log.d(TAG, "attempt %d failed... waiting. Tries : " + tries);
                    try {
                        // life is too short for exponential backoff
                        Thread.sleep(MIN_RETRY_SLEEP_TIME * tries);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (response != null) {
                responseData = prepareServerResponse(response);
            } else {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new CCMException(CCMException.ILLEGAL_STATE_EXCEPTION);
        }
        return responseData;
    }

    public String downloadResource(Context context, final String url_param, String fileName)
            throws CCMException {

    	String url = null;
    	
    	String randomText = CCMUtils.deviceDetails(context);
    	try{
    	url  = url_param+randomText;
    	}catch(Exception ae){ 
    		Log.d(TAG, "setting default param "+ae.getMessage());
    		url = url_param;
    	}
        Log.d(TAG, "Into downloadResource() method for uri : " + url);
        String filePath = null;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            URL resourceUrl = new URL(url);
            URLConnection connection = resourceUrl.openConnection();
            int fileSize =  connection.getContentLength();
            connection.setReadTimeout(10000); // enables throw
                                              // SocketTimeoutException
            is = connection.getInputStream();
            
            File file = new File(context.getFilesDir(), fileName);
            int i=1;
            while(file.exists()) {
                fileName = fileName + "_" + i;
                file = new File(context.getFilesDir(), fileName);
            }
            if(!file.exists()) {
                file.createNewFile();
            }
            Log.d(TAG, "Into downloadResource() filePath : " + file.getPath());
            fos = new FileOutputStream(file);
            // Read bytes to the Buffer until there is nothing more to read(-1).
            byte[] data = new byte[20000];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                fos.write(data,0,nRead);
            }
            Log.d(TAG, "Into downloadResource() done writing");
            filePath = file.getPath();
            
            if(file.length()!=fileSize){
            	 if (is != null) {
                     is.close();
                 }
                 if (fos != null) {
                     fos.close();
                 }
                 is = null;
                 fos = null;
                 if(file.exists())
                	 file.delete();
                 return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,
                    "downloadResource() Exception while downloading resource.");
            throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
                
            } catch (IOException e) {
                throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
            }
        }
        
        return filePath;
    }

    /**
     * Checks and returns whether there is an Internet connectivity or not. This
     * would be useful to check the network connectivity before making a network
     * call.
     * 
     * @param context
     * @return "True" -> is Connected , "False" -> if not.
     */
    public synchronized static boolean isNetworkAvailable(Context context) {
        boolean isConnected = false;
        final ConnectivityManager connectivityService = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityService != null) {
            final NetworkInfo networkInfo = connectivityService
                    .getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                isConnected = true;
            }
        }
        return isConnected;
    }

    /**
     * Prepares the custom ResponseData to be used by UI component from the LMMS
     * HttpResponse.
     * 
     * @param response
     *            HttpResponse
     * @return custom response data.
     */
    ResponseData prepareServerResponse(HttpResponse response)
            throws CCMException {
        ResponseData responseData = null;
        StatusLine statusLine = response.getStatusLine();
        if (statusLine != null) {
            int responseCode = statusLine.getStatusCode();
            // Get the entity.
            HttpEntity entity = response.getEntity();
            responseData = new ResponseData();
            if (entity != null) {
                // Get the input stream.
                try {
                    InputStream is = entity.getContent();
                    if (is != null) {
                        // Try to read the stream.
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(is, "iso-8859-1"));
                        StringBuilder builder = new StringBuilder();
                        for (String line = null; (line = reader.readLine()) != null;) {
                            builder.append(line).append("\n");
                        }
                        Log.d(TAG, "prepareServerResponse Response Code : "
                                + responseCode);
                        Log.d(TAG, "prepareServerResponse Response data : "
                                + builder.toString());
                        if (!TextUtils.isEmpty(builder.toString())) {
                            JSONTokener tokener = new JSONTokener(
                                    builder.toString());
                            JSONObject jsonObject = new JSONObject(tokener);
                            responseData.setResponseJson(jsonObject);
                        }
                        entity.consumeContent();
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new CCMException(CCMException.IO_EXCEPTION);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new CCMException(CCMException.JSON_EXCEPTION);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CCMException(CCMException.UNSPECIFIED_EXCEPTION);
                }
            }
            responseData.setHttpResponseCode(responseCode);
            switch (responseCode) {
            // Success cases.
            case HttpStatus.SC_OK:
            case HttpStatus.SC_ACCEPTED:
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_NO_CONTENT: {
                responseData.setSuccess(true);
                break;
            }
            // Failure cases.
            case HttpStatus.SC_BAD_REQUEST:
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_FORBIDDEN:
            case HttpStatus.SC_NOT_FOUND:
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
            case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            case HttpStatus.SC_UNPROCESSABLE_ENTITY: {
                responseData.setSuccess(false);
                break;
            }
            default:
                responseData.setSuccess(false);
                break;
            }
        }
        return responseData;
    }

    /**
     * This class encapsulates the server response. - mHttpResponseCode will
     * store the HTTP response code from the LMMS Server. - mIsSuccess will
     * basically checks the HTTP response code and decide whether server
     * response is failure or success. Whereas, mResponseJson will store the
     * response json which will give the the Result object (optional) and other
     * content sent by server.
     */
    public class ResponseData {
        private int mHttpResponseCode;
        private boolean mIsSuccess;
        private JSONObject mResponseJson;

        public int getHttpResponseCode() {
            return mHttpResponseCode;
        }

        public void setHttpResponseCode(int httpResponseCode) {
            this.mHttpResponseCode = httpResponseCode;
        }

        public boolean isSuccess() {
            return mIsSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.mIsSuccess = isSuccess;
        }

        public JSONObject getResponseJson() {
            return mResponseJson;
        }

        public void setResponseJson(JSONObject responseJson) {
            this.mResponseJson = responseJson;
        }
    }
}
