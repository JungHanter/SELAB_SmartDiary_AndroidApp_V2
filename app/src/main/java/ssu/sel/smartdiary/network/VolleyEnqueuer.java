package ssu.sel.smartdiary.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Class for Enqueueing Volley Request
 * Following Singleton Pattern, Because Volley use Single queue for all Request
 * Volley API handles everything : Network Connection, Thread, etc...
 * Usage:
 * (FOR INTERNAL INVOKING ONLY)
 *
 * @author LHS
 * @version 1.0 a 2016-09-08
 */
public class VolleyEnqueuer {
    /**
     * Following Singleton Pattern
     */
    private static VolleyEnqueuer enqueInstance; // only Single Instance
    private RequestQueue enqueRequestQueue;
    private static Context appContext; //for volley API

    /**
     * private Constructor for Singleton Pattern
     * @param context   for volley API
     */
    private VolleyEnqueuer(Context context){
        appContext = context;
        this.enqueRequestQueue = this.getRequestQueue();
    }

    /**
     * for Singleton Pattern
     * @param context   for volley API
     * @return  for Singleton Pattern
     */
    public static synchronized VolleyEnqueuer getInstance(Context context) {
        if (enqueInstance == null) {
            enqueInstance = new VolleyEnqueuer(context);
        }
        return enqueInstance;
    }

    /**
     * for Singleton Pattern
     * @return  RequestQueue for volley API
     */
    public RequestQueue getRequestQueue() {
        if (enqueRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            enqueRequestQueue = Volley.newRequestQueue(appContext.getApplicationContext());
        }
        return enqueRequestQueue;
    }

    /**
     * Real enqueueing REQUEST
     * @param req   variable contains REQUEST
     * @param <T>   Type : JSONObjectRequest, StringRequest, JSONArrayRequest
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
