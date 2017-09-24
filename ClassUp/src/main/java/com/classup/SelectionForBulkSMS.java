package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectionForBulkSMS extends AppCompatActivity {
    String tag = "SelectionForBulkSMS";
    final ArrayList<String> selection = new ArrayList<>();
    final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_for_bulk_sms);

        // as we are using singleton pattern to get the logged in user, sometimes the method
        // call returns a blank string. In this case we will retry for 20 times and if not
        // successful even after then we will ask the user to log in again
        String logged_in_user = SessionManager.getInstance().getLogged_in_user();
        int i = 0;
        while (logged_in_user.equals("")) {
            logged_in_user = SessionManager.getInstance().getLogged_in_user();
            if (i++ == 20) {
                Toast.makeText(this,
                        "There seems to be some problem with network. Please re-login",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this,
                        LoginActivity.class);
                this.startActivity(intent);
            }
        }

        final ArrayList<ClassListSource> class_list = new ArrayList<>();

        final BulkSMSAdapter bulkSMSAdapter =
                new BulkSMSAdapter(this, android.R.layout.simple_list_item_checked, class_list);
        final ListView listView = (ListView) findViewById(R.id.bulk_sms_selection_list);
        listView.setDivider(new ColorDrawable(0x99F10529));
        listView.setDividerHeight(1);
        listView.setAdapter(bulkSMSAdapter);

        // retrieve the list of classes
        String school_id = SessionManager.getInstance().getSchool_id();
        String server_ip = MiscFunctions.getInstance().getServerIP(activity);
        String url = server_ip + "/academics/class_list/" + school_id + "/?format=json";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                String the_class = jo.getString("standard");
                                class_list.add(new ClassListSource(the_class));
                                bulkSMSAdapter.notifyDataSetChanged();
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception while dealing with "
                                        + tag);
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while dealing with" + tag);
                                e.printStackTrace();
                            }
                        }
                        class_list.add(new ClassListSource("Teachers"));
                        class_list.add(new ClassListSource("Staff"));
                        bulkSMSAdapter.notifyDataSetChanged();
                        progressDialog.hide();
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
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
                        System.out.println("inside volley error handler");
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.the_class);

                if (!textView.isChecked()) {
                    textView.setChecked(true);

                    selection.add(class_list.get(i).getThe_class());

                    // also add to the selected  list of the adapter
                    bulkSMSAdapter.selection.add(class_list.get(i).getThe_class());
                    bulkSMSAdapter.notifyDataSetChanged();
                } else {
                    textView.setChecked(false);

                    // also remove from the selected list of the adapter
                    bulkSMSAdapter.selection.remove(class_list.get(i).getThe_class());
                    bulkSMSAdapter.notifyDataSetChanged();

                    if (selection.contains(class_list.get(i).getThe_class())) {
                        selection.remove(class_list.get(i).getThe_class());
                        bulkSMSAdapter.selection.remove((class_list.get(i).getThe_class()));
                    }
                }
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

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 1, 0, "Done").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent intent = new Intent(this, SchoolAdmin.class);
                this.startActivity(intent);
                break;
            case 1:
                if (selection.size() == 0) {
                    String message = "You have not selected any Class or Teacher or Staff!";
                    Toast toast = Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    System.out.print("selection=");
                    System.out.println(selection);
                    final String message = getIntent().getStringExtra("message");
                    final android.app.AlertDialog.Builder builder =
                            new android.app.AlertDialog.Builder(activity);
                    builder.setMessage("Are you sure you want to send message?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("message_text", message);
                                        jsonObject.put("whole_school", "false");

                                        String school_id =
                                                SessionManager.getInstance().getSchool_id();
                                        jsonObject.put("school_id", school_id);
                                        String user =
                                                SessionManager.getInstance().getLogged_in_user();
                                        jsonObject.put("user", user);
                                        JSONArray array = new JSONArray();
                                        for(int i = 0; i < selection.size(); i++)   {
                                            array.put(selection.get(i));
                                        }
                                        jsonObject.put("classes_array", array);

                                    } catch (JSONException je) {
                                        System.out.println
                                                ("unable to create json for " +
                                                        "compose message functionality");
                                        je.printStackTrace();
                                    } catch (ArrayIndexOutOfBoundsException ae) {
                                        System.out.println("array out of bounds exception");
                                        ae.printStackTrace();
                                    }
                                    String server_ip =
                                            MiscFunctions.getInstance().getServerIP(activity);
                                    String url = server_ip + "/operations/send_bulk_sms/";

                                    JsonObjectRequest jsonObjReq =
                                            new JsonObjectRequest(Request.Method.POST,
                                                    url, jsonObject,
                                                    new Response.Listener<JSONObject>() {

                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            Log.d(tag, response.toString());
                                                        }
                                                    }, new Response.ErrorListener() {

                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    VolleyLog.d(tag, "Error: "
                                                            + error.getMessage());
                                                }
                                            });
                                    jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0, -1,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    com.classup.AppController.getInstance().
                                            addToRequestQueue(jsonObjReq, tag);
                                    Toast.makeText(getApplicationContext(),
                                            "Message(s) sent!",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent("com.classup.SchoolAdmin").
                                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                }
                            }).setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    // Create the AlertDialog object and return it
                    builder.show();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
