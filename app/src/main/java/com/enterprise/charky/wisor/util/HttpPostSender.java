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

    public void sendWSCommand(WSCommand wsCommand) {
        if(connectionCreated) {
            SendPostDataAsync spdAsync = new SendPostDataAsync();
            spdAsync.execute(wsCommand);
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

    private class SendPostDataAsync extends AsyncTask<WSCommand, Void, WSCommand> {



        @Override
        protected WSCommand doInBackground(WSCommand... wsCommand) {
            //send Request
            WSCommand localWSC = wsCommand[0];
            try {
                urlConnection.connect();
                connectionCreated = false;
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection
                        .getOutputStream()));
                writer.write(localWSC.getJSONString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                localWSC.sendingCommandSuccessful = false;
                return localWSC;
            }
            return localWSC;
        }
        @Override
        protected void onPostExecute(WSCommand wsCommand) {
            if(wsCommand.sendingCommandSuccessful){
                GetDataAsync gtAsync = new GetDataAsync();
                gtAsync.execute(wsCommand);
            } else {
                _mainMainActivity.setResponseStatus(wsCommand);
            }
        }
    }

    private class GetDataAsync extends AsyncTask<WSCommand, Void, WSCommand> {

        @Override
        protected WSCommand doInBackground(WSCommand... wsCommand) {
            //get Response
            WSCommand localWSC = wsCommand[0];
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
                localWSC.jsonReturnMessage = sb.toString();
                return localWSC;
            } catch (IOException e) {
                e.printStackTrace();
            }
            localWSC.jsonReturnMessage = "Error";
            return localWSC;
        }

        @Override
        protected void onPostExecute(WSCommand wsCommand) {
            _mainMainActivity.setResponseStatus(wsCommand);
        }
    }
}
