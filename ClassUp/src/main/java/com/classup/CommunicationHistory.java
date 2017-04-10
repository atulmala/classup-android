package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommunicationHistory extends AppCompatActivity {
    String server_ip;
    String url;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_history);

        context = getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(context);

        final ArrayList<CommunicationSource> communication_list = new ArrayList<>();
        ListView listView = (ListView)findViewById(R.id.communication_list);
        final CommunicationHistoryAdapter adapter =
                new CommunicationHistoryAdapter(this, communication_list);
        listView.setAdapter(adapter);

        // retrieve the message history for this user
        server_ip = MiscFunctions.getInstance().getServerIP(context);
        String user = SessionManager.getInstance().getLogged_in_user();

        String url = server_ip + "/operations/retrieve_sms_history/" + user + "/";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length() < 1)   {
                            Toast.makeText(context, "Communication History is blank.",
                                    Toast.LENGTH_LONG).show();
                        }
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);

                                // though we have nothing to do with id it's a good idea to keep it
                                String id = jo.getString("id");
                                // get the name of the student. We need to join first and last names
                                String date = jo.getString("date");
                                String yy = date.substring(0, 4);
                                String month = date.substring(5, 7);
                                String dd = date.substring(8, 10);
                                String ddmmyyyy = dd + "/" + month + "/" + yy;

                                String message = jo.getString("message");

                                communication_list.add(new CommunicationSource(id,
                                        ddmmyyyy, message));
                                adapter.notifyDataSetChanged();
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                        "while trying to fetch the list of students");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the list of students");
                                e.printStackTrace();
                            }
                        }
                        progressDialog.hide();
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error handler");
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            Toast.makeText(context, "Slow network connection, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(context, "Server error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(context, "Network error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, "InventoryList");
    }
}
