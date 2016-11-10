package ssu.sel.smartdiary.network;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;

import ssu.sel.smartdiary.GlobalUtils;

/**
 * Created by hanter on 16. 9. 30..
 */
public class MultipartRestConnectorOld {
    public static final String SERVER_URL = GlobalUtils.SERVER_URL;
    public static int CONNECTION_TIME_OUT = 3000;
    public static int READ_TIME_OUT = 10000;

    private String apiUrl = "";
    private String method = "";
    private OnConnectListener listener = null;

    public MultipartRestConnectorOld(String apiUrl, String method, OnConnectListener l) {
        this.apiUrl = apiUrl;
        this.method = method.toUpperCase();
        this.listener = l;
    }

    public void request(final File fileDir, final JSONObject json) {
        AsyncTask<Void, Void, JSONObject> reqTask = new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                        .setCharset(Charset.forName("UTF-8"))
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                if (fileDir != null && fileDir.exists()) {
                    if (fileDir.isDirectory()) {
                        String[] fileList = fileDir.list();
                        if (fileList != null && fileList.length > 0) {
                            for (int i = 0; i < fileList.length; i++) {
                                String filename = fileList[i];
                                File file = new File(fileDir.getPath() + "/" + filename);
                                Log.d("MultipartConnector", file.getPath());
                                if (file.exists()) {
                                    builder.addPart("file" + i, new FileBody(file));
                                }
                            }
                        }
                    } else {
                        Log.d("MultipartConnector", fileDir.getPath());
                        if (fileDir.exists()) {
                            builder.addPart("file0", new FileBody(fileDir));
                        }
                    }
                }
//                builder.addPart("json", new StringBody(json.toString(), ContentType.TEXT_PLAIN));
                builder.addPart("json", new StringBody(json.toString(), ContentType.APPLICATION_JSON));

//                RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
//                        .setConnectTimeout(CONNECTION_TIME_OUT)
//                        .setSocketTimeout(READ_TIME_OUT)
//                        .build();

                AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
//                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpEntityEnclosingRequestBase httpRequest = null;

                if (method.equals("POST")) {
                    httpRequest = new HttpPost(SERVER_URL + apiUrl);
                    httpRequest.setEntity(builder.build());
                } else if (method.equals("PUT")) {
                    httpRequest = new HttpPut(SERVER_URL + apiUrl);
                    httpRequest.setEntity(builder.build());
                } else {
                    throw new NullPointerException("Not support method!");
                }
//                httpRequest.setConfig(requestConfig);
//                httpRequest.addHeader("content-type", "application/json; charset=utf-8");
//                httpRequest.addHeader("accept", "application/json");

                try {
                    HttpResponse response = httpClient.execute(httpRequest);

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        String jsonString = EntityUtils.toString(response.getEntity());
                        JSONObject resJson = new JSONObject(jsonString);
                        Log.d("MultipartConnector", "response json: " + resJson.toString());
                        return resJson;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MultipartConnector", e.toString());
                    return null;
                } finally {
                    httpClient.close();
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


    public static abstract class OnConnectListener {
        public void onReady(){}
        public void onCancelled(){}
        public abstract void onDone(JSONObject resJson);
    }
}
