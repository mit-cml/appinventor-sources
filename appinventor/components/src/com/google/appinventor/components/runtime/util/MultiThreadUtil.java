package com.google.appinventor.components.runtime.util;

import android.os.AsyncTask;
import android.os.Handler;

public final class MultiThreadUtil {

    private MultiThreadUtil() {}

    public static class MultiThreadException extends RuntimeException {
        public static final long serialVersionUID = 0;
        public MultiThreadException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    public static void runInNewThread(final YailProcedure procedure, final YailProcedure callback) {
        AsyncTask<Object, Object, Object> task = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... args) {
                return procedure.call();
            }
            @Override
            protected void onPostExecute(Object result) {
                callback.call(result);
            }
        };
        try {
            task.execute();
        } catch (RuntimeException e) {
            // If there is a RuntimeException, it must be:
            //    java.lang.RuntimeException: An error occurred while executing doInBackground()
            // and get a cause, so e.getCause() would not be null here all the time.
            if (e.getCause().getCause() != null) {
                Throwable exceptionInProcedure = e.getCause().getCause();
                if (exceptionInProcedure.getClass().getName() == "android.view.ViewRootImpl$CalledFromWrongThreadException") {
                    throw new MultiThreadException("Can not operate views in background task", e);
                } else {
                    throw new MultiThreadException("Multi-Thread Exception:" + exceptionInProcedure.getClass().getName(), e);
                }
            }
            throw new MultiThreadException("Multi-Thread Exception:" + e.getCause().getClass().getName(), e);
        }
    }

    public static void runAfterPeriod(long millis, final YailProcedure procedure) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                procedure.call();
            }
        }, millis);
    }

}