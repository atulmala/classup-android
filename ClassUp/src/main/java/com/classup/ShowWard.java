package com.classup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowWard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ward);

        try {
            AnalyticsEvent event =
                    SessionManager.getInstance().analytics.getEventClient().
                            createEvent("Show Ward");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.getInstance().analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Show Ward");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Show Ward");
        }


        // get the list of wards for this parent
        String tag = "ParentsMenu";
        final ArrayList<AttendanceListSource> ward_list = new ArrayList<>();
        final ArrayList<String> ward_name = new ArrayList<>();
        final ArrayList<String> ward_id = new ArrayList<>();

        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.ward_list_view, ward_name);
        ListView listView = (ListView)findViewById(R.id.ward_list);
        listView.setDivider(new ColorDrawable(0x99F10529));
        listView.setDividerHeight(1);
        listView.setAdapter(adapter);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        final String server_ip = MiscFunctions.getInstance().
                getServerIP(this.getApplicationContext());
        final String parent_mobile = SessionManager.getInstance().getLogged_in_user();
        final String ward_list_url =  server_ip +
                "/student/student_list_for_parents/" + parent_mobile;
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, ward_list_url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                // get the name of the student. We need to join first and last names
                                String f_name = jo.getString("fist_name");
                                String l_name = jo.getString("last_name");
                                String full_name = f_name + " " + l_name;
                                ward_name.add(full_name);

                                // get the erp_id of the student
                                String erp_id = jo.getString("student_erp_id");

                                // get the id of the student
                                String id = jo.getString("id");
                                ward_id.add(id);

                                // get the roll number of the student
                                String roll_no = jo.getString("roll_number");
                                // put all the above details into the adapter
                                ward_list.add(new AttendanceListSource(roll_no,
                                        full_name, id, erp_id));
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
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        }  else if (error instanceof ServerError) {
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
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        // Implement the action when a student is tapped
        final Intent intent = new Intent(this.getApplicationContext(), ParentsMenu.class);
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> view, View v, int position, long id) {
                intent.putExtra("student_id", ward_id.get(position));
                intent.putExtra("student_name", ward_name.get(position));
                startActivity(intent);
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
