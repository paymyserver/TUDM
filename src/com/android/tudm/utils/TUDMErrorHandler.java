package com.android.tudm.utils;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.util.Log;

import com.turnupdmood.tudm.R;
public class TUDMErrorHandler {
    private final static String TAG = TUDMErrorHandler.class.getSimpleName();
    // Error codes from server
    public static final int ERROR_INVALID_PARAMETERS = 206;
    public static final int ERROR_INVALID_USERNAME = 207;
    public static final int ERROR_INVALID_PASSWORD = 208;
    public static final int ERROR_INVALID_CREDENTIALS = 209;

    public static final int ERROR_FAILED_CONVERSION_TO_JSON = 260;
    public static final int ERROR_NO_NETWORK = 261;

    // Error code received if we tries to register a device which is already
    // registered on the server.
    public static final String ERROR_DUPLICATE_DEVICE = "DuplicateError";

    public static String getErrorMessage(Context context, int errorCode) {
        Log.d(TAG, "Error Code::" + errorCode);
        switch (errorCode) {
            // Error codes from server
        case ERROR_INVALID_USERNAME:
        case ERROR_INVALID_PASSWORD:
        case ERROR_INVALID_CREDENTIALS:
            return context.getResources().getString(
                    R.string.error_msg_incorrect_username_or_password);
        case ERROR_FAILED_CONVERSION_TO_JSON:
            return context.getResources().getString(
                    R.string.error_msg_failed_json_conversion);
        case ERROR_NO_NETWORK:
            return context.getResources().getString(
                    R.string.error_network_unavailable);
        case HttpStatus.SC_BAD_REQUEST:
            return context.getResources().getString(R.string.error_bad_request);
        case HttpStatus.SC_UNAUTHORIZED:
            return context.getResources()
                    .getString(R.string.error_unauthorized);
        case HttpStatus.SC_UNPROCESSABLE_ENTITY:
            return context.getResources().getString(
                    R.string.error_unprocessable_request);
        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            return context.getResources().getString(
                    R.string.error_internal_server);
        case TUDMException.IO_EXCEPTION:
            return context.getResources().getString(
                    R.string.error_server_unreachable);
        default:
            return context.getResources().getString(R.string.error_bad_request);
        }
    }
}
