package com.classup;


import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    public MyFirebaseInstanceIdService() {
    }

    @Override
    public void onTokenRefresh() {
        // 10/03/17 - send the registration token to server
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String user_name = SessionManager.getInstance().getLogged_in_user();
        try {
            String server_ip = MiscFunctions.getInstance().getServerIP(getApplicationContext());
            JSONObject jsonObject1 = new JSONObject();
            try {
                jsonObject1.put("user", user_name);
                jsonObject1.put("device_token", refreshedToken);
                jsonObject1.put("device_type", "Android");
            } catch (org.json.JSONException je) {
                je.printStackTrace();
            }


            String url2 = server_ip + "/auth/map_device_token/";
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url2, jsonObject1, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("map_device_token", response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("map_device_token", "Error: " + error.getMessage());
                }
            });
            com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, "map_device_token");
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }


}
