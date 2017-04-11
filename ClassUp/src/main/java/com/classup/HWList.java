package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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

public class HWList extends AppCompatActivity {
    String tag = "HWList";
    String server_ip;
    String school_id;
    final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hw_list);
        this.setTitle("HW List");

        final Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        String logged_in_user = SessionManager.getInstance().getLogged_in_user();

        final ArrayList<HWListSource> hw_list = new ArrayList<>();
        ListView listView = (ListView) findViewById(R.id.teacher_hw_list);
        final String url = server_ip + "/academics/retrieve_hw/" +
                logged_in_user + "/?format=json";

        final HWListAdapter hwListAdapter = new HWListAdapter(this, hw_list);
        listView.setAdapter(hwListAdapter);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Home Work list. Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length() < 1)   {
                            Toast toast = Toast.makeText(c, "No HW created.", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                // get the id of the hw
                                String id = jo.getString("id");

                                String date = jo.getString("due_date");
                                String yy = date.substring(0, 4);
                                String month = date.substring(5, 7);
                                String dd = date.substring(8, 10);
                                String ddmmyyyy = dd + "/" + month + "/" + yy;

                                String teacher = jo.getString("teacher");

                                String the_class = jo.getString("the_class");

                                String section = jo.getString("section");
                                String subject = jo.getString("subject");
                                String notes = jo.getString("notes");

                                // put all the above details into the adapter
                                hw_list.add(new HWListSource(id, teacher, the_class, section,
                                        subject, ddmmyyyy, notes));
                                hwListAdapter.notifyDataSetChanged();
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                        "while trying to fetch the HW list");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the HW list");
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
                            Toast.makeText(c, "Slow network connection",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(c, "Server error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(c, "Network error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent intent = getIntent();
        if (intent.getStringExtra("sender").equals("teacher_menu"))
            menu.add(0, 0, 0, "Create").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent intent = new Intent(this, SelectClass.class);
                intent.putExtra("sender", "createHW");
                System.out.println("intent set to createHW");
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    // 11/04/17 As we many arrive at this activity after taking pic and uploading homework, we
    // need to program the back button.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        //Changes 'back' button action
        if(keyCode== KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(this, TeacherMenu.class);
            intent.putExtra("sender", "createHW");
            System.out.println("intent set to createHW");
            startActivity(intent);
        }
        return true;
    }
}
