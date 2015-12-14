package com.enterprise.charky.wisor;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;

import com.enterprise.charky.wisor.util.HttpPostSender;
import com.enterprise.charky.wisor.util.JsonRPC;
import com.enterprise.charky.wisor.util.MissingNetworkDialogFragment;
import com.enterprise.charky.wisor.util.SwitchAdapter;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        MissingNetworkDialogFragment.MissingNetworkDialogListener,
        SwitchAdapter.CardButtonListener{

    //Finals
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 6001;

    //Data
    String[] SwitchCaptionsNames;

    //Utils
    private HttpPostSender httpPostSender;
    private JsonRPC jsonRPC;

    //Android
    private SwitchAdapter switchAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Views
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init Objects
        httpPostSender = new HttpPostSender(this);
        jsonRPC = new JsonRPC();
        SwitchCaptionsNames = new String[3];

        //InitBindingsAndListeners
        initViewBindings();

        //Init Settings on first start
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }


    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Set Switch-Names by Preference
        SwitchCaptionsNames[0] = sharedPref.getString("pref_key_ws1_name", "");
        SwitchCaptionsNames[1] = sharedPref.getString("pref_key_ws2_name", "");
        SwitchCaptionsNames[2] = sharedPref.getString("pref_key_ws3_name", "");

        //Notify Change to Adapter
        switchAdapter.notifyDataSetChanged();

        //Check Network availability
        checkNetworkStatus();
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
        }else if (id == R.id.action_voice_recognition) {
            startVoiceRecognitionActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        startVoiceRecognitionActivity();
        Snackbar.make(v, "Start Voice Recognition. Say your Commando.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //Cycle until Network is activated
        checkNetworkStatus();
    }

    @Override
    public void onCardButtonClick(View view, int wsID) {
        boolean btLightOn = (view.getId() == R.id.bt_light_on);
        //SendIt
        sendJSONRPC(wsID, btLightOn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
                && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String listString = "";
            String wordStr;
            String[] words;
            for (String s : matches)
            {
                listString += s + "\t";
                wordStr = matches.get(0);
                words = wordStr.split(" ");
                if (words.length > 1 && words[0].equals("Licht")) {
                    if(words[1].equals("an")) {
                        sendJSONRPC(3, true);
                        break;
                    }else if(words[1].equals("aus")){
                        sendJSONRPC(3, false);
                        break;
                    }
                }

            }
            Log.d("VoiceTest",listString);

        }
    }

    private void initViewBindings(){
        //RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        switchAdapter = new SwitchAdapter(SwitchCaptionsNames);
        switchAdapter.setCardButtonListener(this);
        mRecyclerView.setAdapter(switchAdapter);

        //FloatingActionButton
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab != null) {
            fab.setOnClickListener(this);
        }

    }

    private void sendJSONRPC(int wsID, boolean powerState){
        // Get Values
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String iAddress = "http://" + sharedPref.getString("pref_key_server_name", "") + ":"
                + sharedPref.getString("pref_key_server_port", "") + "/jsonrpc";
        //Prepare JsonRPC
        jsonRPC.setWSParams(wsID, powerState);
        try {
            httpPostSender.createConnect(iAddress);
            httpPostSender.sendPostData(jsonRPC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setResponseStatus(boolean statusOK) {
        View v = findViewById(R.id.root_RelativeLayout);

        Snackbar sb = Snackbar.make(v, "ResponseStatusText Error", Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        TextView textView = (TextView) sb.getView().findViewById(android.support.design.R.id.snackbar_text);

        //If ok set Green and OK
        if (statusOK) {
            sb.setText(getResources().getString(R.string.response_status_ok));
            textView.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            sb.setText(getResources().getString(R.string.response_status_error));
            textView.setTextColor(ContextCompat.getColor(this,R.color.red));
        }
        sb.show();
    }

    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    private void checkNetworkStatus(){
        // Set visibility and enabled by Network availability
        if(!httpPostSender.isNetworkAvailable()){
            MissingNetworkDialogFragment newFragment = new MissingNetworkDialogFragment();
            newFragment.show(getFragmentManager(), "missing_network");
        }
    }
}
