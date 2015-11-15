package com.enterprise.charky.wisor.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by charky on 13.11.15.
 * Wrapping JSON_RPC-Command for communication with Kodi
 */

/*
{"jsonrpc": "2.0", "id": 0, "method": "Addons.ExecuteAddon", "params":
    { "addonid": "script.chyfy", "params":
        { "wsID": "1", "powerState": "0" }
    }
}
 */
public class JsonRPC extends JSONObject {

    private JSONObject paramsObj;

    public JsonRPC(){
        super();
        initBasicStructure();
    }

    public void setWSParams(int wsID, boolean powerState){
        try {
            paramsObj.put("wsID", ""+wsID);
            paramsObj.put("powerState", (powerState)?"1":"0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initBasicStructure(){

        try {
            // Parameter Corpus
            paramsObj = new JSONObject();

            // Basic
            this.put("jsonrpc", "2.0");
            this.put("id","0");
            this.put("method", "Addons.ExecuteAddon");

            // chyfy Addon
            JSONObject chyfyAddon = new JSONObject();
            chyfyAddon.put("addonid", "script.chyfy");
            chyfyAddon.put("params", paramsObj);

            // Finalize
            this.put("params", chyfyAddon);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
