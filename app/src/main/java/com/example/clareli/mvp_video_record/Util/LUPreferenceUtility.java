package com.example.clareli.mvp_video_record.Util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.clareli.mvp_video_record.Util.IConstant.USER_PREFERENCES;

public class LUPreferenceUtility {
    private final WeakReference<Activity> _messageViewReference;

    public LUPreferenceUtility(Activity activity){
        _messageViewReference = new WeakReference<>(activity);

    }

    public boolean saveMapToPreference(String key, Map<String, Object> value) {
        SharedPreferences preferences   = _messageViewReference.get().getSharedPreferences(USER_PREFERENCES,
                Context.MODE_PRIVATE);
        JSONObject jsonObject           = null;
        String jsonString               = null;
        SharedPreferences.Editor editor = null;
        boolean success                 = false;

        if (preferences != null) {
            editor = preferences.edit();
            jsonObject = new JSONObject(value);
            jsonString = jsonObject.toString();
            editor.putString(key, jsonString);
            success = editor.commit();
        }
        return success;
    }

    public Map<String, Object> loadMapFromPreference(String key) {
        Map<String, Object> map         = new HashMap<String, Object>();
        SharedPreferences preferences   = _messageViewReference.get().getSharedPreferences(USER_PREFERENCES,
                Context.MODE_PRIVATE);
        JSONObject jsonObject           = null;
        String jsonString               = null;
        Iterator<String> iterator       = null;
        String mapKey                   = null;
        String mapValue                 = null;

        try {
            if (preferences != null) {
                jsonString = preferences.getString(key, null);
                if (jsonString != null) {
                    jsonObject = new JSONObject(jsonString);
                    iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        mapKey = iterator.next();
                        mapValue = (String)jsonObject.get(mapKey);
                        map.put(mapKey, mapValue);
                    }
                    return map;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public boolean saveArrayToPreference(String key, ArrayList value) {
        SharedPreferences preferences   = _messageViewReference.get().getSharedPreferences(USER_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = null;
        boolean success                 = false;
        Gson gson                       = null;
        String json                     = null;

        if (preferences != null) {
            editor = preferences.edit();
            gson = new Gson();
            json = gson.toJson(value);
            editor.putString(key, json);
            success = editor.commit();
        }
        return success;
    }

    public ArrayList loadArrayFromPreference(String key) {
        SharedPreferences preferences           = _messageViewReference.get().getSharedPreferences(USER_PREFERENCES,
                Context.MODE_PRIVATE);
        ArrayList<Map<String, Object>> value    = null;
        Gson gson                               = null;
        String json                             = null;
        Type type                               = null;

        if (preferences != null) {
            json = preferences.getString(key, null);
            if (json != null) {
                type = new TypeToken<ArrayList<Map<String, Object>>>() {
                }.getType();
                gson = new Gson();
                value = gson.fromJson(json, type);
            }
        }
        return value;
    }

    public boolean saveStringToPreference(String key, Object value) {
        SharedPreferences preferences   = _messageViewReference.get().getSharedPreferences(USER_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = null;
        boolean success                 = false;

        if (preferences != null) {
            editor = preferences.edit();
            editor.putString(key, String.valueOf(value));
            success = editor.commit();
        }
        return success;
    }

    public String loadStringFromPreference(String key) {
        SharedPreferences preferences = _messageViewReference.get().getSharedPreferences(USER_PREFERENCES,
                Context.MODE_PRIVATE);
        String value                  = null;

        if (preferences != null) {
            value = preferences.getString(key, null);
        }
        return value;
    }
}
