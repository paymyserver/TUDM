package com.android.cloudcovermusic.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonParser {

    public static Object parseJsonToType(Context context,
            JSONObject jsonObject, Type type) {
        Gson gson = new Gson();
        Object result = gson.fromJson(jsonObject.toString(), type);
        return result;
    }
    

    public static String convertTypeToJson(Context context, Object object,
            Type type) {
        Gson gson = new Gson();
        String result = gson.toJson(object, type);
        return result;
    }

    public static String convertMapToString(final HashMap<String, Object> map) {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return json;
    }

    public static String convertToJsonExcludingExpose(final Object data) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .create();
        String json = gson.toJson(data);
        return json;
    }

    /**
     * Utility API to convert given json string to hashmap
     * 
     * @param json
     *            : JSON string
     * @return : Hashmap of json
     */
    public static HashMap<String, Object> convertStringToMap(final String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        HashMap<String, Object> map = gson.fromJson(json, type);
        return map;
    }

    /**
     * Converts object to JSON
     * 
     * @param object
     *            : Object instance to be converted into Json.
     */
    public static String convertObjectToJson(final Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
}
