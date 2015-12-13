package com.enterprise.charky.wisor.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.enterprise.charky.wisor.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by charky on 13.11.15.
 * Handling all Network-Actions
 */
public class HttpPostSender {

    private final MainActivity _mainMainActivity;
    private HttpURLConnection urlConnection;
    private boolean connectionCreated;

    public HttpPostSender(MainActivity mainActivity){
        _mainMainActivity = mainActivity;
        connectionCreated = false;
    }

    public void createConnect(String iAddress) throws IOException {
        if (!connectionCreated) {
            //Create URL and Connection
            URL url = new URL(iAddress);
            urlConnection = (HttpURLConnection) url.openConnection();

            //Configure Connection
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            connectionCreated = true;
        }

    }

    public void sendPostData(JsonRPC postData) {
        if(connectionCreated) {
            SendPostDataAsync spdAsync = new SendPostDataAsync();
            spdAsync.execute(postData.toString());
        }
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)_mainMainActivity.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null && networkInfo.isConnected();
    }

    private class SendPostDataAsync extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... postData) {
            //send Request
            try {
                Log.d("HttpPostSender", postData[0]);
                urlConnection.connect();
                connectionCreated = false;
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection
                        .getOutputStream()));
                writer.write(postData[0]);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                _mainMainActivity.setResponseStatus(false);
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                GetDataAsync gtAsync = new GetDataAsync();
                gtAsync.execute();
            } else {
                _mainMainActivity.setResponseStatus(false);
            }
        }
    }

    private class GetDataAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            //get Response
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection
                        .getInputStream()));
                String nextLine;
                while ((nextLine = reader.readLine()) != null) {
                    sb.append(nextLine);
                }
                reader.close();
                urlConnection.disconnect();
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Error";
        }

        @Override
        protected void onPostExecute(String jsonReturn) {
            if ("Error".equals(jsonReturn)) {
                _mainMainActivity.setResponseStatus(false);
            } else {
                Log.d("HttpPostSender", jsonReturn);
                try {
                    JSONObject jsonObj = new JSONObject(jsonReturn);
                    String result = jsonObj.getString("result");
                    if ("OK".equals(result)) {
                        _mainMainActivity.setResponseStatus(true);
                    } else {
                        _mainMainActivity.setResponseStatus(false);
                    }
                } catch (JSONException e) {
                    _mainMainActivity.setResponseStatus(false);
                }
            }
        }
    }
}
