package com.tahiru.flikrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}


class GetRawData extends AsyncTask<String, Void, String> {
    private static final String TAG = "GetRawData";
    private final OnDownloadComplete mCallback;
    private DownloadStatus mDownloadStatus;

    public GetRawData(OnDownloadComplete callback) {

        this.mDownloadStatus = DownloadStatus.IDLE;
        mCallback = callback;
    }


    void runInSameTHread(String s) {
        Log.d(TAG, "runInSameTHread: starts");

        onPostExecute(doInBackground());

        Log.d(TAG, "runInSameTHread: Ends");
    }



    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: parameter = " + s);
        if (mCallback != null) {
            mCallback.onDownloadComplete(s, mDownloadStatus);
        }
        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (strings == null) {
            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {
            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            //connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: the response code was " + response);


            StringBuilder result = new StringBuilder();

            Log.d(TAG, "doInBackground: the response code was " + 1);

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            Log.d(TAG, "doInBackground: the response code was " + 2);

            String line;
            while (null != (line = reader.readLine())) {
                Log.d(TAG, "doInBackground: the response code was " + 4);

                result.append(line).append("\n");
            }
            mDownloadStatus = DownloadStatus.OK;

            Log.d(TAG, "doInBackground: the response code was " + 3);

            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL" + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO exception reading data " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security exception - needs permission" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream" + e.getMessage());
                }
            }
        }

        Log.d(TAG, "doInBackground: ends");

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }

    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }


}
