package com.enterprise.charky.wisor.util;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by charky on 17.12.15.
 * This Class capsules all Interpreting Action therefore it needs the Preferences
 */
public class VoiceResultInterpreter {

    private SharedPreferences mainSharedPref;
    private String onWord;
    private String offWord;

    public VoiceResultInterpreter(String onWord, String offWord){
        this.onWord = onWord.toLowerCase();
        this.offWord = offWord.toLowerCase();
    }

    public void setPreferences(SharedPreferences sharedPref){
        mainSharedPref = sharedPref;
    }

    public WSCommand analyseMatches(ArrayList<String> matches){
        String listString = "";
        String[] words;
        String word1;
        String word2;
        WSCommand wsCommand = new WSCommand();
        //Loop throw each match
        for (String wordStr : matches)
        {
            listString += wordStr + "\t";
            //Extract all words
            words = wordStr.split(" ");
            //Two words are needed
            if (words.length < 2 ){
                continue;
            }else{
                int i=0;
                word1 = words[i];
                i++;
                while(i<words.length-1){
                    word1 += " " + words[i];
                    i++;
                }
                word2 = words[i];
            }

            //Lower Case for easier comparison
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();
            //Check word 1
            if(word1.equals(mainSharedPref.getString("pref_key_ws1_name", "").toLowerCase())) {
                wsCommand.wsID = 1;
            }else if(word1.equals(mainSharedPref.getString("pref_key_ws2_name", "").toLowerCase())) {
                wsCommand.wsID = 2;
            }else if(word1.equals(mainSharedPref.getString("pref_key_ws3_name", "").toLowerCase())) {
                wsCommand.wsID = 3;
            }else{
                continue;
            }

            //Check action
            if(word2.equals(onWord)) {
                wsCommand.powerState = true;
                wsCommand.validVoiceRecognition = true;
                break;
            }else if(word2.equals(offWord)){
                wsCommand.powerState = false;
                wsCommand.validVoiceRecognition = true;
                break;
            }
        }
        Log.d("VoiceResultInterpreter", listString);
        return wsCommand;
    }

}
