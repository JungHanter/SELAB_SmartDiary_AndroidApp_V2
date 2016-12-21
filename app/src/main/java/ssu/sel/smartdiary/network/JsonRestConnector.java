package ssu.sel.smartdiary.network;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import ssu.sel.smartdiary.GlobalUtils;

/**
 * Created by hanter on 16. 9. 21..
 */
public class JsonRestConnector {
    public static final String SERVER_URL = GlobalUtils.SERVER_URL;
    public static final int CONNECTION_TIME_OUT = 3000;
    public static final int READ_TIME_OUT = 10000;

    private String apiUrl = "";
    private String method = "";
    private OnConnectListener listener = null;
    private int readTimeOut = READ_TIME_OUT;

    public JsonRestConnector(String apiUrl, String method, OnConnectListener l) {
        this.apiUrl = apiUrl;
        this.method = method.toUpperCase();
        this.listener = l;
    }

    public void request(Map<String, Object> jsonMap) throws JSONException {
        JSONObject json = new JSONObject();
        for (String key : jsonMap.keySet()) {
            json.put(key, jsonMap.get(key));
        }
        request(json);
    }

    public void request(String jsonString) throws JSONException {
        request(new JSONObject(jsonString));
    }

    public void request(final JSONObject json) {
        AsyncTask<Void, Void, JSONObject> reqTask = new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                URL url = null;
                HttpURLConnection conn = null;
                OutputStream os = null;
                InputStream is = null;
                BufferedReader br = null;

                try {
                    if (method.equals("GET")) {
                        StringBuilder urlSB = new StringBuilder();
                        urlSB.append(SERVER_URL).append(apiUrl).append('?');

                        boolean bFirstIteration = true;
                        Iterator<String> keys = json.keys();
                        while(keys.hasNext()) {
                            if(bFirstIteration) {
                                bFirstIteration = false;
                            } else {
                                urlSB.append('&');
                            }

                            String key = keys.next();
                            Object value = json.get(key);
                            if (value instanceof String) {
                                value = java.net.URLEncoder.encode((String)value, "UTF-8");
                            }
                            urlSB.append(key).append('=').append(value);
                        }

                        Log.d("JsonConnector", "request GET url: " + urlSB.toString());

                        url = new URL(urlSB.toString());
                        conn = (HttpURLConnection)url.openConnection();

                        conn.setRequestProperty("Accept", "application/json");
                        conn.setRequestMethod("GET");

                        conn.setDoInput(true);
                        conn.setConnectTimeout(CONNECTION_TIME_OUT);
                        conn.setReadTimeout(readTimeOut);
                    } else {
                        url = new URL(SERVER_URL + apiUrl);
                        conn = (HttpURLConnection)url.openConnection();

                        conn.setRequestProperty("Accept", "application/json");
                        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        conn.setRequestMethod(method);

                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setConnectTimeout(CONNECTION_TIME_OUT);
                        conn.setReadTimeout(readTimeOut);

                        Log.d("JsonConnector", "request json: " + json.toString());

                        os = conn.getOutputStream();
                        os.write(json.toString().getBytes());
                        os.flush();
//                        if (method.equals("POST")) {
//                            os.write(json.toString().getBytes());
//                            os.flush();
//                        } else {
//                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
//                            bw.write(json.toString());
////                            bw.write("{\"test\":\"Hansang\"}");
//                            bw.flush();
//                        }
                        os.close();
                    }

                    int resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        is = conn.getInputStream();

                        br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                        StringBuilder sb = new StringBuilder(8912);
                        String line = null;
                        while( (line = br.readLine()) != null ) {
                            sb.append(line);
                            sb.append('\n');
                        }
                        br.close();

                        JSONObject resJson = new JSONObject(sb.toString());
                        Log.d("JsonConnector", "response json: " + resJson.toString());

                        if (os!=null) os.close();
                        is.close();
                        conn.disconnect();
                        return resJson;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("JsonConnector", e.toString());
                }

                return null;
            }

            @Override
            protected void onPreExecute() {
                listener.onReady();
            }

            @Override
            protected void onPostExecute(JSONObject resJson) {
                listener.onDone(resJson);
            }

            @Override
            protected void onCancelled() {
                listener.onCancelled();
            }
        };

        reqTask.execute((Void) null);
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public static abstract class OnConnectListener {
        public void onReady(){}
        public void onCancelled(){}
        public abstract void onDone(JSONObject resJson);
    }
}
