package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityGroup extends AppCompatActivity {
    final Activity activity = this;
    String server_ip;
    String url;

    final ArrayList<ActivityGroupSource> activity_groups_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

        server_ip = MiscFunctions.getInstance().getServerIP(this);

        this.setTitle("Activity Groups List");


        ListView listView = findViewById(R.id.list_activity_group);
        registerForContextMenu(listView);
        final ActivityGroupAdapter adapter = new ActivityGroupAdapter(
            this, activity_groups_list);
        listView.setAdapter(adapter);

        String school_id = SessionManager.getInstance().getSchool_id();

        String url = server_ip + "/activity_groups/get_activity_group_list/" + school_id + "/";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if(response.length() < 1)   {
                        Toast toast = Toast.makeText(getApplicationContext(),
                            "No Activity Groups created",
                            Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);

                            // though we have nothing to do with id it's a good idea to keep it
                            String id = jo.getString("id");
                            String group = jo.getString("group_name");
                            String incharge = jo.getString("group_incharge");
                            String incharge_email = jo.getString("incharge_email");

                            activity_groups_list.add (new ActivityGroupSource(id, group,
                                incharge, incharge_email));
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
                    // 12/09/17 - Now we are building the custom
                    // Analysis via AWS
                    try {
                        AnalyticsEvent event = SessionManager.getInstance().
                            analytics.getEventClient().
                            createEvent("Activity Groups");
                        event.addAttribute("user", SessionManager.getInstance().
                            getLogged_in_user());
                        SessionManager.getInstance().analytics.getEventClient().
                            recordEvent(event);
                    } catch (NullPointerException exception)    {
                        System.out.println("flopped in creating " +
                            "analytics Activity Groups");
                    } catch (Exception exception)   {
                        System.out.println("flopped in " +
                            "creating analytics Activity Groups");
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
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection, please try later",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        //TODO
                    }
                    // TODO Auto-generated method stub
                }
            });
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, "Activity Group List");

        final Intent intent = new Intent(this, ComposeMessage.class);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String logged_in_user = SessionManager.getInstance().getLogged_in_user();
                String incharge_email = activity_groups_list.get(i).getIncharge_email();

                // only the group incharge can send message to the group
                if (logged_in_user.equals(incharge_email)) {
                    intent.putExtra("coming_from", "ActivityGroup");
                    String group_name = activity_groups_list.get(i).getActivity_group();
                    String group_id = activity_groups_list.get(i).getId();

                    String message = "Sending message to " + group_name;
                    intent.putExtra("group_id", group_id);
                    Toast toast = Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    startActivity(intent);
                }
                else    {
                    Toast toast = Toast.makeText(getApplicationContext(),
                        "You are not the incharge of this group. " +
                            "Hence, you cannot send message to this group.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();;
                }
            }
        });

        // long tapping on student name will initiate call to parent
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int ii = i;

                final String group_name = activity_groups_list.get(i).getActivity_group();
                android.app.AlertDialog.Builder builder =
                    new android.app.AlertDialog.Builder(activity);
                builder.setMessage("Do you want to see the list of members in group:   " +
                    group_name + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                AnalyticsEvent callParentEvent =
                                    SessionManager.getInstance().
                                        analytics.getEventClient().
                                        createEvent("Activity Group Members");
                                callParentEvent.addAttribute("user",
                                    SessionManager.getInstance().getLogged_in_user());
                                SessionManager.getInstance().analytics.getEventClient().
                                    recordEvent(callParentEvent);
                            } catch (NullPointerException exception)    {
                                System.out.println("flopped in creating " +
                                    "analytics Activity Group Members");
                            } catch (Exception exception)   {
                                System.out.println("flopped in creating " +
                                    "analytics Activity Group Members");
                            }
                            Intent intent1 = new Intent(activity, ActivityMembers.class);
                            intent1.putExtra("group_id",
                                activity_groups_list.get(ii).getId());
                            intent1.putExtra("group_name", group_name);
                            startActivity (intent1);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                // Create the AlertDialog object and return it
                builder.show();
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }

}
