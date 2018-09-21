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

public class ShowExamList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_exam_list);

        try {
            AnalyticsEvent event =
                    SessionManager.getInstance().analytics.getEventClient().
                            createEvent("Show Exam List");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.getInstance().analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Show Exam List");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Show Exam List");
        }


        // get the list of subjects for which at least one test has been conducted
        String tag = "ExamList";
        final ArrayList<String> exam_title_list = new ArrayList<>();
        final ArrayList<String> exam_id_list = new ArrayList<>();

        final ArrayAdapter adapter =
                new ArrayAdapter(this, R.layout.exam_list_view, exam_title_list);
        ListView listView = findViewById(R.id.exam_list);
        listView.setDivider(new ColorDrawable(0x99F10529));
        listView.setDividerHeight(1);
        listView.setAdapter(adapter);

        final Intent intent = getIntent();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        final String server_ip = MiscFunctions.getInstance().
                getServerIP(this.getApplicationContext());
        final String parent_mobile = SessionManager.getInstance().getLogged_in_user();
        final String ward_list_url =  server_ip +
                "/academics/get_exam_list/" + intent.getStringExtra("student_id");
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
                                String title = jo.getString("title");
                                exam_title_list.add(title);

                                String id = jo.getString("id");
                                exam_id_list.add(id);

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
        final Intent intent1 = new Intent(this.getApplicationContext(), ShowExamResults.class);
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> view, View v, int position, long id) {
                intent1.putExtra("student_id", intent.getStringExtra("student_id"));
                intent1.putExtra("student_name", intent.getStringExtra("student_name"));
                intent1.putExtra("exam_id", exam_id_list.get(position));
                intent1.putExtra("exam_title", exam_title_list.get(position));
                startActivity(intent1);
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

