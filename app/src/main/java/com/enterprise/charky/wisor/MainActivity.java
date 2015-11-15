package com.enterprise.charky.wisor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.enterprise.charky.wisor.util.HttpPostSender;
import com.enterprise.charky.wisor.util.JsonRPC;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    //J-Objects
    private Handler handler;

    //Utils
    private HttpPostSender httpPostSender;
    private JsonRPC jsonRPC;

    //Views
    private Switch sw_ws1;
    private Switch sw_ws2;
    private Switch sw_ws3;
    private TextView tv_no_lan1;
    private TextView tv_no_lan2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //Init Objects
        httpPostSender = new HttpPostSender(this);
        jsonRPC = new JsonRPC();
        handler = new Handler();

        //InitBindingsAndListeners
        initViewBindings();

        //Init Settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }


    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //Set Switch-Names by Preference
        sw_ws1.setText(sharedPref.getString("pref_key_ws1_name", ""));
        sw_ws2.setText(sharedPref.getString("pref_key_ws2_name", ""));
        sw_ws3.setText(sharedPref.getString("pref_key_ws3_name", ""));

        // Set visibility and enabled by Network availability
        if(httpPostSender.isNetworkAvailable()){
            sw_ws1.setEnabled(true);
            sw_ws2.setEnabled(true);
            sw_ws3.setEnabled(true);
            tv_no_lan1.setVisibility(View.INVISIBLE);
            tv_no_lan2.setVisibility(View.INVISIBLE);
        }else{
            sw_ws1.setEnabled(false);
            sw_ws2.setEnabled(false);
            sw_ws3.setEnabled(false);
            tv_no_lan1.setVisibility(View.VISIBLE);
            tv_no_lan2.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Get Values
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String iAddress = "http://" + sharedPref.getString("pref_key_server_name", "") + ":"
                + sharedPref.getString("pref_key_server_port", "") + "/jsonrpc";
        int wsID = 1;
        int tvID = R.id.tv_status_ws1;
        switch (buttonView.getId()) {
            case R.id.sw_ws2:
                wsID = 2;
                tvID = R.id.tv_status_ws2;
                break;
            case R.id.sw_ws3:
                wsID = 3;
                tvID = R.id.tv_status_ws3;
                break;
        }
        //Prepare JsonRPC
        jsonRPC.setWSParams(wsID, isChecked);
        try {
            httpPostSender.createConnect(iAddress);
            httpPostSender.sendPostData(jsonRPC, tvID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initViewBindings(){
        //Switches
        sw_ws1 = (Switch) findViewById(R.id.sw_ws1);
        sw_ws1.setOnCheckedChangeListener(this);
        sw_ws2 = (Switch) findViewById(R.id.sw_ws2);
        sw_ws2.setOnCheckedChangeListener(this);
        sw_ws3 = (Switch) findViewById(R.id.sw_ws3);
        sw_ws3.setOnCheckedChangeListener(this);
        //Lan Error TextViews
        tv_no_lan1 = (TextView) findViewById(R.id.tv_error_lan1);
        tv_no_lan2 = (TextView) findViewById(R.id.tv_error_lan2);
    }

    public void setResponseStatus(boolean statusOK, int textViewID) {
        TextView tv = (TextView) findViewById(textViewID);
        //If ok set Green and OK
        if (statusOK) {
            tv.setText(getResources().getString(R.string.response_status_ok));
            tv.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            tv.setText(getResources().getString(R.string.response_status_error));
            tv.setTextColor(ContextCompat.getColor(this,R.color.red));
        }
        handler.postDelayed(new HideStatusLabel(textViewID), 3000);
    }

    //Runnable
    private class HideStatusLabel implements Runnable {

        private int tvID = -1;

        public HideStatusLabel(int tvID) {
            this.tvID = tvID;
        }

        @Override
        public void run() {
            ((TextView) findViewById(tvID)).setText("");
        }
    }
}
