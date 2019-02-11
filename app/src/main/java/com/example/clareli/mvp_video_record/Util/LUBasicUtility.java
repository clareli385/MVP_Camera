package com.example.clareli.mvp_video_record.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

//2015-12-30, Samson, add the lib for saving ArrayList to USER Preference.
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static android.content.Context.WIFI_SERVICE;
import static com.example.clareli.mvp_video_record.Util.IConstant.USER_PREFERENCES;

/**
 * Created by Samson on 12/25/15.
 */
public class LUBasicUtility {
    public final static int FPS_MESSAGE_ID = 1001;

    private int _fps;
    private long _preFPSTime;
    private WeakReference<Handler> _weakMainHandler;
    private Context _applicationContext;

    public LUBasicUtility(Handler handler, Context context) {
        _weakMainHandler = new WeakReference<Handler>(handler);
        _applicationContext = context;
    }

    public void calculateFPS() {
        long curTime        = System.currentTimeMillis();
        Message message     = null;
        Handler mainHandler = null;

        if ((curTime - _preFPSTime) >= 1000) {
            mainHandler = _weakMainHandler.get();
            if (mainHandler != null) {
                message = Message.obtain(mainHandler, FPS_MESSAGE_ID);
                message.arg1 = _fps;
                mainHandler.sendMessage(message);
                _fps = 0;
                _preFPSTime = curTime;
            }
        } else {
            _fps++;
        }
    }

    public String getBroadcastIP() {
        int ip          = getIPAddress();
        String ipString = null;

        if (ip != 0) {
            ipString = String.format(Locale.US, "%d.%d.%d.%s",
                    (ip & 0xff), (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff), "255");
        } else {
            ipString = "255.255.255.255";
        }
        return ipString;
    }

    public String getHostIP() {
        int ip          = getIPAddress();
        String ipString = null;

        if (ip != 0) {
            ipString = String.format(Locale.US, "%d.%d.%d.%d",
                    (ip & 0xff), (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff), (ip >> 24 & 0xff));
        } else {
            ipString = "255.255.255.255";
        }
        return ipString;
    }

    public boolean saveMapToPreference(String key, Map<String, String> value) {
        SharedPreferences preferences   = _applicationContext.getSharedPreferences(USER_PREFERENCES,
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

    public Map<String, String> loadMapFromPreference(String key) {
        Map<String, String> map         = new HashMap<String, String>();
        SharedPreferences preferences   = _applicationContext.getSharedPreferences(USER_PREFERENCES,
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
        SharedPreferences preferences   = _applicationContext.getSharedPreferences(USER_PREFERENCES,
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
        SharedPreferences preferences           = _applicationContext.getSharedPreferences(USER_PREFERENCES,
                                                                                    Context.MODE_PRIVATE);
        ArrayList<Map<String, String>> value    = null;
        Gson gson                               = null;
        String json                             = null;
        Type type                               = null;

        if (preferences != null) {
            json = preferences.getString(key, null);
            if (json != null) {
                type = new TypeToken<ArrayList<Map<String, String>>>() {
                }.getType();
                gson = new Gson();
                value = gson.fromJson(json, type);
            }
        }
        return value;
    }

    public boolean saveStringToPreference(String key, String value) {
        SharedPreferences preferences   = _applicationContext.getSharedPreferences(USER_PREFERENCES,
                                                                            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = null;
        boolean success                 = false;

        if (preferences != null) {
            editor = preferences.edit();
            editor.putString(key, value);
            success = editor.commit();
        }
        return success;
    }

    public String loadStringFromPreference(String key) {
        SharedPreferences preferences = _applicationContext.getSharedPreferences(USER_PREFERENCES,
                                                                        Context.MODE_PRIVATE);
        String value                  = null;

        if (preferences != null) {
            value = preferences.getString(key, null);
        }
        return value;
    }

    private int getIPAddress() {
        WifiManager manager = (WifiManager)_applicationContext.getSystemService(WIFI_SERVICE);
        WifiInfo info       = manager.getConnectionInfo();
        int ip              = 0;
        String ipString     = null;

        try {
            if(info != null) {
                ip = info.getIpAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }
}
