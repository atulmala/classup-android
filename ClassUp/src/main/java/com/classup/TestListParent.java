package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class TestListParent extends AppCompatActivity {
    String tag = "Pending Test for Parent";
    String server_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list_parent);
        this.setTitle("Upcoming tests for " + getIntent().getStringExtra("student_name"));

        try {
            AnalyticsEvent event =
                    SessionManager.analytics.getEventClient().
                            createEvent("Upcoming Tests");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Upcoming Tests");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Upcoming Tests");
        }


        final Context c = this;
        server_ip = MiscFunctions.getInstance().getServerIP(c);

        final ArrayList<TestListSource> test_list = new ArrayList<>();
        ListView listView = findViewById(R.id.p_pending_test_list);

        final TestListParentAdapter adapter = new TestListParentAdapter(this, test_list);
        listView.setAdapter(adapter);

        String student = getIntent().getStringExtra("student_id");
        final String url =  server_ip + "/academics/pending_test_list_parents/" +
                student + "/?format=json";
        final ProgressDialog progressDialog = new ProgressDialog(c);
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

                                String date = jo.getString("date_conducted");
                                String yy = date.substring(0, 4);
                                String month = date.substring(5, 7);
                                String dd = date.substring(8, 10);
                                String ddmmyyyy = dd + "/" + month + "/" + yy;

                                String the_class = jo.getString("the_class");

                                String section = jo.getString("section");
                                String subject = jo.getString("subject");
                                String max_marks = jo.getString("max_marks");
                                String grade_based = jo.getString("grade_based");

                                if (grade_based.equals("true")) {
                                    max_marks = "Grade Based";
                                }
                                else    {
                                    String mm = max_marks;
                                    // remove the decimal and last two 0s
                                    String mm1 = mm.substring(0, mm.length()-3);
                                    max_marks = mm1;
                                }

                                // get the id of the test
                                String id = jo.getString("id");

                                String test_topics = jo.getString("syllabus");

                                // put all the above details into the adapter
                                test_list.add(new TestListSource(ddmmyyyy, the_class,
                                        section, subject, max_marks, id, test_topics));
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
                            Toast.makeText(c, "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(c, "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(c, "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().pauseSession();
            SessionManager.analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().resumeSession();
        }
    }
}
