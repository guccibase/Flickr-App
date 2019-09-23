package com.tahiru.flikrbrowser;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class GetFlickrJsonData extends AsyncTask<String, Void, List<Photo>> implements GetRawData.OnDownloadComplete {

    private static final String TAG = "GetFlickrJsonData";
    private final OnDataAvailable mCallBack;
    private List<Photo> mPhotoList = null;
    private String mBaseURL;
    private String mLanguage;
    private boolean mMatchAll;

    public GetFlickrJsonData(OnDataAvailable mCallBack, String mBaseURL, String mLanguage, boolean mMatchAll) {
        Log.d(TAG, "GetFlickrJsonData: called");
        this.mBaseURL = mBaseURL;
        this.mLanguage = mLanguage;
        this.mMatchAll = mMatchAll;
        this.mCallBack = mCallBack;
    }

    void executeOnSameThread(String searchCriteria) {
        Log.d(TAG, "executeOnSameThread: Starts");
        String destinationUri = createUri(searchCriteria, mLanguage, mMatchAll);

        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(destinationUri);
        Log.d(TAG, "executeOnSameThread: ends");

    }

    @Override
    protected void onPostExecute(List<Photo> photos) {
        super.onPostExecute(photos);
    }

    @Override
    protected List<Photo> doInBackground(String... params) {
        Log.d(TAG, "doInBackground: starts");
        String destinationUri = createUri(params[0], mLanguage, mMatchAll);
        GetRawData getRawData = new GetRawData(this);

        return null;
    }

    private String createUri(String searchCriteria, String mLanguage, boolean mMatchAll) {
        Log.d(TAG, "createUri: Starts");

        return Uri.parse(mBaseURL).buildUpon()

                .appendQueryParameter("tags", searchCriteria)
                .appendQueryParameter("tagmode", mMatchAll ? "ALL" : "ANY")
                .appendQueryParameter("lang", mLanguage)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .build().toString();

    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        Log.d(TAG, "onDownloadComplete: Status = " + status);

        if (status == DownloadStatus.OK) {
            mPhotoList = new ArrayList<>();

            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("items");

                for (int i = 1; i < itemsArray.length(); i++) {
                    JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                    String title = jsonPhoto.getString("title");
                    String author = jsonPhoto.getString("author");
                    String authoId = jsonPhoto.getString("author_id");
                    String tags = jsonPhoto.getString("tags");

                    JSONObject jsonMedia = jsonPhoto.getJSONObject("media");
                    String photoUrl = jsonMedia.getString("m");

                    String link = photoUrl.replaceFirst("_m.", "_b");

                    Photo photoObject = new Photo(title, author, authoId, link, tags, photoUrl);
                    mPhotoList.add(photoObject);

                    Log.d(TAG, "onDownloadComplete: " + photoObject.toString());

                }

            } catch (JSONException json) {
                json.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Error processing json data " + json.getMessage());
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }

        if (mCallBack != null) {
            //now inform the caller that processing is done - possibly returning null if there
            // was an error
            mCallBack.onDataAvailable(mPhotoList, status);
        }

        Log.d(TAG, "onDownloadComplete: ends");
    }

    interface OnDataAvailable {
        void onDataAvailable(List<Photo> data, DownloadStatus status);
    }
}
