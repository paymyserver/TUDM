package com.turnupdmood.tudm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class MyExceptionHandler implements UncaughtExceptionHandler {
    private static final String TAG = MyExceptionHandler.class.getSimpleName();
    private final Context myContext;
    private final Class<?> myActivityClass;

    public MyExceptionHandler(Context context, Class<?> c) {
        myContext = context;
        myActivityClass = c;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        Log.e(TAG, stackTrace.toString());// You can use LogCat too
        Toast.makeText(
                myContext,
                myContext
                        .getString(R.string.toast_message_application_recovering),
                Toast.LENGTH_LONG).show();
        // for restarting the Activity
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}