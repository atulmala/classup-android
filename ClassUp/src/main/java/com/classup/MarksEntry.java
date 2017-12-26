package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
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
import java.util.Arrays;
import java.util.List;

public class MarksEntry extends AppCompatActivity {
    MarksEntryListAdapter adapter;
    String tag = "MarksEntryList";
    String server_ip;
    String school_id;

    Activity activity;

    Boolean grade_based;
    String test_type;
    String whether_higher_class;
    String subject;

    List<String> prac_subjects = Arrays.asList("Biology", "Physics", "Chemistry",
        "Accountancy", "Business Studies", "Economics",
        "Information Practices", "Computer Science", "Painting",
        "Physical Education");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        final Intent intent = this.getIntent();
        grade_based = intent.getBooleanExtra("grade_based", false);
        test_type = intent.getStringExtra("test_type");
        whether_higher_class = intent.getStringExtra("higher_class");
        subject = intent.getStringExtra("subject");

        final Context c = this.getApplicationContext();

        setContentView(R.layout.activity_marks_entry);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
            String title = "Marks Entry " + intent.getStringExtra("class") + "-" +
                intent.getStringExtra("section") + " " +
                subject;
            actionBar.setTitle(title);
        }

        final ArrayList<MarksEntryListSource> marks_list = new ArrayList<>();
        final ListView listView = findViewById(R.id.marks_entry_list);
        adapter = new MarksEntryListAdapter(this, marks_list, grade_based, test_type,
            whether_higher_class, subject);

        // get the list of students, roll no and current marks/grade
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        String url = server_ip + "/academics/get_test_marks_list/" +
            intent.getStringExtra("test_id") + "/";
        ;
        final ProgressDialog progressDialog = new ProgressDialog(activity);
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

                            String id = jo.getString("id");
                            String roll_no = jo.getString("roll_no");
                            String name = jo.getString("student");
                            String sr_no = Integer.toString(i + 1);
                            String full_name = sr_no + "    " + name;
                            if (sr_no.length() > 1)
                                full_name = sr_no + "  " + name;
                            String parent = jo.getString("parent"); // added 23/06/2017
                            String marks = jo.getString("marks_obtained");
                            String grade = jo.getString("grade");

                            // 23/09/2017 for term test
                            String pt_marks = jo.getString("periodic_test_marks");
                            String notebook_sub_marks = jo.getString("notebook_marks");
                            String sub_enrich_marks = jo.getString("sub_enrich_marks");

                            String prac_marks = "0.0";
                            if (whether_higher_class.equals("true"))
                                prac_marks = jo.getString("prac_marks");

                            marks_list.add(new MarksEntryListSource(id, roll_no,
                                full_name, parent, marks, grade, pt_marks,
                                notebook_sub_marks, sub_enrich_marks, prac_marks));
                            adapter.notifyDataSetChanged();
                        } catch (JSONException je) {
                            System.out.println("Ran into JSON exception " +
                                "while trying to fetch the marks/grade list");
                            je.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Caught General exception " +
                                "while trying to fetch the marks/grade list");
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

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
        listView.setAdapter(adapter);

        // get max marks and passing marks for this test and communicate to adapter
        if (!grade_based) {
            url = server_ip + "/academics/get_test_type/" + intent.getStringExtra("test_id") + "/";

            JsonArrayRequest jsonArrayRequest1 = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);

                                String mm = jo.getString("max_marks");
                                String pm = jo.getString("passing_marks");
                                adapter.max_marks = mm;
                                adapter.pass_marks = pm;
                                adapter.notifyDataSetChanged();
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                    "while trying to fetch the marks/grade list");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                    "while trying to fetch the marks/grade list");
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error handler");
                        // TODO Auto-generated method stub
                    }
                });

            com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest1, tag);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_marks_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.save_marks:
                saveMarks(adapter);
                break;
            case R.id.submit_marks:
                submitMarks(adapter);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void saveMarks(MarksEntryListAdapter adapter) {
        Toast toast = Toast.makeText(getApplicationContext(),
            "Saving Marks/Grades...", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        List<MarksEntryListSource> marks_entry_list = adapter.getMarks_entry_list();
        JSONObject params = new JSONObject();
        for (int i = 0; i < marks_entry_list.size(); i++)
            if (!grade_based)
                try {
                    JSONObject params1 = new JSONObject();
                    params1.put("marks", marks_entry_list.get(i).getMarks());
                    params1.put("pa", marks_entry_list.get(i).getPeriodic_test_marks());
                    params1.put("notebook", marks_entry_list.get(i).
                        getNotebook_submission_marks());
                    params1.put("subject_enrich",
                        marks_entry_list.get(i).getSubject_enrichment_marks());

                    // 25/12/2017 practical marks for higher classes
                    if (whether_higher_class.equals("true"))
                        params1.put ("prac_marks", marks_entry_list.get(i).getPrac_marks());
                    params.put(marks_entry_list.get(i).getId(), params1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            else
                try {
                    params.put(marks_entry_list.get(i).getId(), marks_entry_list.get(i).getGrade());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        System.out.println(params);
        String url = server_ip + "/academics/save_marks/";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
            url, params,
            new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(tag, response.toString());
                    try {
                        if (response.get("status").toString().equals("success")) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                "Marks/Grades successfully saved",
                                Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL
                                | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                            // 12/09/17 - Now we are building the custom
                            // Analysis via AWS
                            try {
                                AnalyticsEvent saveMarksEvent = SessionManager.getInstance().
                                        analytics.getEventClient().
                                    createEvent("Saved Marks");
                                saveMarksEvent.addAttribute("user",
                                    SessionManager.getInstance().getLogged_in_user());
                                SessionManager.getInstance().analytics.getEventClient().
                                    recordEvent(saveMarksEvent);
                            } catch (NullPointerException exception) {
                                System.out.println("flopped in creating analytics Save Marks");
                            } catch (Exception exception) {
                                System.out.println("flopped in creating analytics Save Marks");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(tag, "Error: " + error.getMessage());
            }
        });
        com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
    }

    void submitMarks(MarksEntryListAdapter adapter) {
        List<MarksEntryListSource> marks_entry_list = adapter.getMarks_entry_list();
        JSONObject params = new JSONObject();

        Boolean good_to_submit = true;
        for (int i = 0; i < marks_entry_list.size(); i++) {
            if (!grade_based) {
                if (marks_entry_list.get(i).getMarks().equals("-5000.00")) {
                    String message = "Please enter marks for " +
                        marks_entry_list.get(i).getFull_name() + " or mark absence";
                    Toast toast = Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL
                        | Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    good_to_submit = false;
                    break;
                }

                if (marks_entry_list.get(i).getPeriodic_test_marks().equals("-5000.0") ||
                    marks_entry_list.get(i).getPeriodic_test_marks().equals("-5000.00")) {
                    String message = "Please enter Periodic Test Marks for " +
                        marks_entry_list.get(i).getFull_name();
                    Toast toast = Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER , 0, 0);
                    toast.show();
                    good_to_submit = false;
                    break;
                }

                if (marks_entry_list.get(i).getNotebook_submission_marks().equals("-5000.0") ||
                    marks_entry_list.get(i).getNotebook_submission_marks().equals("-5000.00")) {
                    String message = "Please enter Notebook Submission Marks for " +
                        marks_entry_list.get(i).getFull_name();
                    Toast toast = Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL
                        | Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    good_to_submit = false;
                    break;
                }

                if (marks_entry_list.get(i).getSubject_enrichment_marks().equals("-5000.0") ||
                    marks_entry_list.get(i).getSubject_enrichment_marks().equals("-5000.00")) {
                    String message = "Please enter Subject Enrichment Marks for " +
                        marks_entry_list.get(i).getFull_name();
                    Toast toast = Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL
                        | Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    good_to_submit = false;
                    break;
                }

                if (prac_subjects.contains(subject))    {
                    if (marks_entry_list.get(i).getPrac_marks().equals("-5000.0") ||
                        marks_entry_list.get(i).getPrac_marks().equals("-5000.00")) {
                        String message = "Please enter Practical Marks for " +
                            marks_entry_list.get(i).getFull_name();
                        Toast toast = Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL
                            | Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                        good_to_submit = false;
                        break;
                    }
                }

                try {
                    JSONObject params1 = new JSONObject();
                    params1.put("marks", marks_entry_list.get(i).getMarks());
                    params1.put("pa", marks_entry_list.get(i).getPeriodic_test_marks());
                    params1.put("notebook", marks_entry_list.get(i).
                        getNotebook_submission_marks());
                    params1.put("subject_enrich",
                        marks_entry_list.get(i).getSubject_enrichment_marks());

                    // 25/12/2017 practical marks for higher classes
                    if (whether_higher_class.equals("true"))
                        params1.put ("prac_marks", marks_entry_list.get(i).getPrac_marks());

                    params.put(marks_entry_list.get(i).getId(), params1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (grade_based) {
                if (marks_entry_list.get(i).getGrade().equals("-5000.00") ||
                    marks_entry_list.get(i).getGrade().equals("")) {
                    String message = "Please enter grade for " +
                        marks_entry_list.get(i).getFull_name() + " or mark absence";
                    Toast toast = Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL
                        | Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    good_to_submit = false;
                    break;
                }
                try {
                    params.put(marks_entry_list.get(i).getId(),
                        marks_entry_list.get(i).getGrade());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (good_to_submit) {
            String url = server_ip + "/academics/submit_marks/" + school_id + "/";
            System.out.println(params);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(tag, response.toString());
                        // 12/09/17 - Now we are building the custom
                        // Analysis via AWS
                        try {
                            AnalyticsEvent event =
                                SessionManager.getInstance().analytics.getEventClient().
                                    createEvent("Submit Marks");
                            event.addAttribute("user", SessionManager.getInstance().
                                getLogged_in_user());
                            SessionManager.getInstance().analytics.getEventClient().
                                recordEvent(event);
                        } catch (NullPointerException exception) {
                            System.out.println("flopped in creating analytics Submit Marks");
                        } catch (Exception exception) {
                            System.out.println("flopped in creating analytics Submit Marks");
                        }
                    }
                }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(tag, "Error: " + error.getMessage());
                    error.printStackTrace();
                }
            });
            int socketTimeout = 300000;//5 minutes
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjReq.setRetryPolicy(policy);

            com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
            Toast.makeText(getApplicationContext(),
                "Marks/Grades successfully submitted",
                Toast.LENGTH_SHORT).show();
            startActivity(new Intent("com.classup.TeacherMenu").
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    }
}
