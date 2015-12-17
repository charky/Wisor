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

    public void setPreferences(SharedPreferences sharedPref){
        mainSharedPref = sharedPref;
    }

    public WSCommand analyseMatches(ArrayList<String> matches){
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
                    //sendJSONRPC(3, true);
                    break;
                }else if(words[1].equals("aus")){
                    //sendJSONRPC(3, false);
                    break;
                }
            }

        }
        Log.d("VoiceTest", listString);
        return new WSCommand();
    }

}
