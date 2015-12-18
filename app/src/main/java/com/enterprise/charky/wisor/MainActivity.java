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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.enterprise.charky.wisor.util.HttpPostSender;
import com.enterprise.charky.wisor.util.MissingNetworkDialogFragment;
import com.enterprise.charky.wisor.util.SwitchAdapter;
import com.enterprise.charky.wisor.util.VoiceResultInterpreter;
import com.enterprise.charky.wisor.util.WSCommand;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        MissingNetworkDialogFragment.MissingNetworkDialogListener,
        SwitchAdapter.CardButtonListener{

    //Finals
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 6001;

    //Data
    private String[] SwitchCaptionsNames;

    //Utils
    private HttpPostSender httpPostSender;
    private VoiceResultInterpreter voiceResultInterpreter;

    //Android
    private SwitchAdapter switchAdapter;
    private Intent voiceRecognitionIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init Objects
        httpPostSender = new HttpPostSender(this);
        SwitchCaptionsNames = new String[3];
        voiceResultInterpreter = new VoiceResultInterpreter(
                getResources().getString(R.string.info_ws_status_on),
                getResources().getString(R.string.info_ws_status_off));

        //InitBindingsAndListeners
        initViewBindings();

        //Create VoiceRecognitionIntent
        createVoiceRecognitionIntent();

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

        //Refresh the Settings regarding the VoiceResultInterpreter
        voiceResultInterpreter.setPreferences(sharedPref);

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
        sendJSONRPC(new WSCommand(wsID,btLightOn));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check for Result from the VoiceRecognitionIntent and if the Result is valid
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
                && resultCode == RESULT_OK) {

            //Extract Data
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            //Analyse Data
            WSCommand wsCommand = voiceResultInterpreter.analyseMatches(matches);
            //Check for a valid Recognition
            if(wsCommand.validVoiceRecognition){
                sendJSONRPC(wsCommand);
            }else{
                setSnackbarText(getResources().getString(R.string.response_voice_error),
                        R.color.red);
            }
        }
    }

    private void initViewBindings(){
        //RecyclerView
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
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

    public void sendJSONRPC(WSCommand wsCommand){
        // Get Values
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String iAddress = "http://" + sharedPref.getString("pref_key_server_name", "") + ":"
                + sharedPref.getString("pref_key_server_port", "") + "/jsonrpc";
        try {
            httpPostSender.createConnect(iAddress);
            httpPostSender.sendWSCommand(wsCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setResponseStatus(WSCommand wsCommand) {
        //If ok set Green and OK
        String msg;
        int rColor;
        //Correcting the ID, Wireless Switches starts at 1
        int correctID = wsCommand.wsID - 1;
        String statusText = (wsCommand.powerState)?
                getResources().getString(R.string.info_ws_status_on):
                getResources().getString(R.string.info_ws_status_off);
        if (wsCommand.sendingCommandSuccessful && wsCommand.jsonReturnOK()) {
            msg = getResources().getString(R.string.response_status_ok);
            rColor = R.color.green;
        } else {
            msg = getResources().getString(R.string.response_status_error);
            rColor = R.color.red;
        }
        //Replacing the placeholder starting with #
        msg = msg.replace("#ws_name",SwitchCaptionsNames[correctID])
                .replace("#ws_status",statusText);
        setSnackbarText(msg, rColor);
    }

    public void setSnackbarText(String msg, int rColor){
        View v = findViewById(R.id.root_RelativeLayout);
        Snackbar sb = Snackbar.make(v, "ResponseStatusText Error", Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        //Set Text
        sb.setText(msg);
        //If ok set Green and OK
        if(rColor != -1){
            TextView textView = (TextView) sb.getView()
                    .findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(this, rColor));
        }
        sb.show();
    }

    private void createVoiceRecognitionIntent(){
        voiceRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceRecognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getClass().getPackage().getName());
        voiceRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceRecognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
        voiceRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                R.string.info_voice_rec_start);
    }

    private void startVoiceRecognitionActivity() {
        startActivityForResult(voiceRecognitionIntent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    private void checkNetworkStatus(){
        // Set visibility and enabled by Network availability
        if(!httpPostSender.isNetworkAvailable()){
            MissingNetworkDialogFragment newFragment = new MissingNetworkDialogFragment();
            newFragment.show(getFragmentManager(), "missing_network");
        }
    }
}
