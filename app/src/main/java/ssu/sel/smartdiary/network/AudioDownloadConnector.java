package ssu.sel.smartdiary.network;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import ssu.sel.smartdiary.GlobalUtils;

/**
 * Created by hanter on 16. 9. 21..
 */
public class AudioDownloadConnector {
    public static final String SERVER_URL = GlobalUtils.SERVER_URL;
    public static int CONNECTION_TIME_OUT = 3000;
    public static int READ_TIME_OUT = 10000;

    private String apiUrl = "";
    private String method = "";
    private OnConnectListener listener = null;

    public AudioDownloadConnector(String apiUrl, String method, OnConnectListener l) {
        this.apiUrl = apiUrl;
        this.method = method.toUpperCase();
        this.listener = l;
    }

    public void request(final String userId, final int audioDiaryId) {
        final JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("user_id", userId);
            reqJson.put("audio_diary_id", audioDiaryId);
        } catch (JSONException je) {
            je.printStackTrace();
            listener.onDone(false);
            return;
        }

        AsyncTask<Void, Void, Boolean> reqTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                URL url = null;
                HttpURLConnection conn = null;
                OutputStream os = null;
                InputStream is = null;

                try {
                    url = new URL(SERVER_URL + apiUrl);
                    conn = (HttpURLConnection)url.openConnection();

                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(CONNECTION_TIME_OUT);
                    conn.setReadTimeout(READ_TIME_OUT);

                    conn.setRequestMethod(method);
                    conn.setRequestProperty("Accept", "audio/wav");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                    Log.d("DownloadConnector", "request json: " + reqJson.toString());

                    os = conn.getOutputStream();
                    os.write(reqJson.toString().getBytes());
                    os.flush();
                    os.close();

                    int resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        is = conn.getInputStream();

                        File file = GlobalUtils.getAudioDiaryFile(userId, audioDiaryId);
                        if (file.exists()) file.delete();

                        BufferedInputStream bis = new BufferedInputStream(is);
                        BufferedOutputStream bos = new BufferedOutputStream(
                                new FileOutputStream(file));

                        int inByte;
                        while ((inByte = bis.read()) != -1) bos.write(inByte);
                        bis.close();
                        bos.close();

                        Log.d("DownloadConnector", "Download Done");

                        if (os!=null) os.close();
                        is.close();
                        conn.disconnect();
                        return true;
                    } else {
                        Log.e("DownloadConnector", resCode + "");
                        Log.e("DownloadConnector", conn.getResponseMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("DownloadConnector", e.toString());
                }

                return false;
            }

            @Override
            protected void onPreExecute() {
                listener.onReady();
            }

            @Override
            protected void onPostExecute(Boolean success) {
                listener.onDone(success);
            }

            @Override
            protected void onCancelled() {
                listener.onCancelled();
            }
        };

        reqTask.execute((Void) null);
    }

    public static abstract class OnConnectListener {
        public void onReady(){}
        public void onCancelled(){}
        public abstract void onDone(Boolean success);
    }
}
