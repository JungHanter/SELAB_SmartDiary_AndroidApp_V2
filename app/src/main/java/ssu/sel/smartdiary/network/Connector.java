package ssu.sel.smartdiary.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Class for Server Connecting & Sending JSON
 * Usage:
 * invoke sendJSON()
 *
 * @author LHS
 * @version 0.6 a 2016-09-08
 */
public class Connector {
    /**
     * basic variables for connecting Server
     */
    Context appContext; // for volley API
    private static String url = "http://203.253.23.55:8000/"; // django Server Address
    private JSONObject postResponse;

    /**
     * Constructor
     * @param context   for volley API. (Network, Thread)
     */
    public Connector(Context context)
    {
        this.appContext = context;
        postResponse = new JSONObject();
    }

    /**
     * method for sending JSON by useing volley API
     * @param jObj  JSONObject which will be submitted
     */
    public void sendJSON(JSONObject jObj, String option, final ConnectorCallback callbackMethod)
    {
        String url = this.url + option;
        // Creating JsonObjectRequest (REQUEST form variable)
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("network.Connector", "response : "+response.toString());
                        callbackMethod.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        VolleyEnqueuer.getInstance(this.appContext).addToRequestQueue(jsonObjectRequest);// adding
        //Reqeust to queue
        //then volley will send, automatically
    }

    public interface ConnectorCallback{
        void onSuccess(JSONObject result);
    }
}
