package com.shopper.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtils {

    public static JSONObject getJSON(String url) throws IOException, JSONException {
        String connectionResultStr = connectionResult(url).toString();
        return new JSONObject(connectionResultStr);
    }

    public static StringBuilder connectionResult(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        URLConnection connection = url.openConnection();

        String line;
        StringBuilder builder = new StringBuilder();
        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while((line = reader.readLine()) != null) {
            builder.append(line);
        }
        try {
            is.close();
        } catch (Exception e) {}
        return builder;
    }
}