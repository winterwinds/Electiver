package com.example.electiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonTools {
    public static String getJsonKey(String key, String jsonString){
        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
