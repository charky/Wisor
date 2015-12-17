package com.enterprise.charky.wisor.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by charky on 17.12.15.
 * Wireless Switches Command holding all Information in the progress of operation
 * Information contain command-data and status-information while progressing and after
 */
public class WSCommand {
    //Status whether the Recognition was successful
    public boolean validVoiceRecognition = false;
    //ID of the wireless Socket which should be handled
    public int wsID = 1;
    //Indicator for the Action (true = Power on, vice versa)
    public boolean powerState = false;
    //CommandProcessingErrors
    public boolean sendingCommandSuccessful = true;
    //Return JSON Message
    public String jsonReturnMessage = "";


    public WSCommand(){

    }
    public WSCommand(int wsID, boolean powerState){
        this.wsID = wsID;
        this.powerState = powerState;
    }

    public String getJSONString(){
        return JsonRPC.getWSCommandString(this);
    }

    public boolean jsonReturnOK(){
        if ("Error".equals(jsonReturnMessage) || "".equals(jsonReturnMessage)) {
            return false;
        } else {
            try {
                JSONObject jsonObj = new JSONObject(jsonReturnMessage);
                String result = jsonObj.getString("result");
                if ("OK".equals(result)) {
                    return true;
                }
            } catch (JSONException e) {
                return false;
            }
        }
        return false;
    }

}
