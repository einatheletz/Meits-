package com.example.metis.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.example.metis.utils.Consts.APP_FILES_PATH;

public class JsonToMap {

    private static Logger logger;

    static public Map<String, Object> createJson(String fileName, Context context) {
        if (logger == null) {
            logger = Logger.getInstance(APP_FILES_PATH);
        }
        try {
            logger.writeLog(Consts.LogType.INFO, "JsonToMap", "Reading JSON named: " + fileName);
            JSONObject obj = new JSONObject(ApplicationDataManager.readFile(fileName));
            return toMap(obj);
        } catch (JSONException e) {
            logger.writeLog(Consts.LogType.ERROR, "JsonToMap", "An error occurred while reading the JSON file. file name: " + fileName);
        }
        return new HashMap<>();
    }

    public static Map<String, Object> toMap(JSONObject jsonobj) throws JSONException {
        if(!jsonobj.toString().equals("")) {
            Map<String, Object> map = new HashMap<String, Object>();
            Iterator<String> keys = jsonobj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonobj.get(key);
                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                map.put(key, value);
            }
            return map;
        }
        return null;
    }

    /**
     * This convert json file to list of objects
     * @param array JsonArray
     * @return list of objects
     * @throws JSONException Exception
     */
    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
