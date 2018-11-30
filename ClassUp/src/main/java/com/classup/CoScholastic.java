package com.classup;

import android.app.Activity;
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
import java.util.List;

public class CoScholastic extends AppCompatActivity {
    CoScholasticAdapter adapter;
    String tag = "CoScholastics";
    String server_ip;
    String school_id;

    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_scholastic);

        activity = this;

        final Intent intent = this.getIntent();

        ActionBar actionBar = getSupportActionBar();
        String the_class = intent.getStringExtra("the_class");
        String section = intent.getStringExtra("section");
        final String term = intent.getStringExtra("term");
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
            String title = the_class + "-" + section + " Term: " + term;
            actionBar.setTitle(title);
        }

        final ArrayList<CoScholasticSource> grade_list = new ArrayList<>();
        final ListView listView = (ListView) findViewById(R.id.list_coscholastic);
        adapter = new CoScholasticAdapter(activity, grade_list);
        listView.setAdapter(adapter);

        server_ip = MiscFunctions.getInstance().getServerIP(activity);
        String teacher = SessionManager.getInstance().getLogged_in_user();

        String url = server_ip + "/academics/get_co_cscholastics/";
        url += teacher + "/" + the_class + "/" + section + "/" + term;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);

                            String id = jo.getString("id");
                            String name = jo.getString("student");
                            String sr_no = Integer.toString(i + 1);
                            String full_name = sr_no + "    " + name;
                            if (sr_no.length() > 1)
                                full_name = sr_no + "  " + name;
                            String parent = jo.getString("parent");
                            String grade_work_ed = jo.getString("work_education");
                            String grade_art_ed = jo.getString("art_education");
                            String grade_health_ed = jo.getString("health_education");
                            String grade_dscpln = jo.getString("discipline");
                            String remarks = jo.getString("teacher_remarks");
                            String promoted = jo.getString("promoted_to_class");

                            grade_list.add(new CoScholasticSource(id, term, sr_no, full_name,
                                parent, grade_work_ed, grade_art_ed, grade_health_ed,
                                grade_dscpln, remarks, promoted));
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
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }

    void save(CoScholasticAdapter adapter) {
        Toast toast = Toast.makeText(getApplicationContext(),
            "Saving CoScholastics Grades...", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL |
            Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
        List<CoScholasticSource> list = adapter.getCoscholasticList();
        JSONObject params = new JSONObject();
        for (int i = 0; i < list.size(); i++) {
            JSONObject params1 = new JSONObject();
            try {
                params1.put("term", list.get(i).getTerm());
                params1.put("work_education", list.get(i).getGrade_work_ed());
                params1.put("art_education", list.get(i).getGrade_art_ed());
                params1.put("health_education", list.get(i).getGrade_health());
                params1.put("discipline", list.get(i).getGrade_dscpln());
                params1.put("teacher_remarks", list.get(i).getRemarks_class_teacher());
                params1.put("promoted_to_class", list.get(i).getPromoted_to_class());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                params.put(list.get(i).getId(), params1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(params);
        }

        System.out.println(params);
        String url = server_ip + "/academics/save_co_scholastics/";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
            url, params,
            new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(tag, response.toString());
                    try {
                        if (response.get("status").toString().equals("success")) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                "CoScholastic Grades Successfully Saved/Submitted  ",
                                Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL
                                | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();

                            try {
                                AnalyticsEvent saveMarksEvent =
                                    SessionManager.getInstance().analytics.getEventClient().
                                        createEvent("Saved Coscholastics");
                                saveMarksEvent.addAttribute("user",
                                    SessionManager.getInstance().getLogged_in_user());
                                SessionManager.getInstance().analytics.getEventClient().
                                    recordEvent(saveMarksEvent);
                            } catch (NullPointerException exception) {
                                System.out.println("flopped in creating " +
                                    "analytics Save Coscholastics");
                            } catch (Exception exception) {
                                System.out.println("flopped in " +
                                    "creating analytics Save Coscholastics");
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

    void submit(CoScholasticAdapter adapter)    {
        Boolean good_to_submit = true;
        List<CoScholasticSource> list = adapter.getCoscholasticList();
        for (CoScholasticSource item : list)    {
            if (item.getGrade_work_ed().equals(" "))    {
                String message = "Please enter Work Education Grades for ";
                message += item.getFull_name();
                Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                good_to_submit = false;
                break;
            }

            if (item.getGrade_art_ed().equals(" "))    {
                String message = "Please enter Art Education Grades for ";
                message += item.getFull_name();
                Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                good_to_submit = false;
                break;
            }

            if (item.getGrade_health().equals(" "))    {
                String message = "Please enter Health & Physical Education Grades for ";
                message += item.getFull_name();
                Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                good_to_submit = false;
                break;
            }

            if (item.getGrade_dscpln().equals(" "))    {
                String message = "Please enter Discipline Grades for ";
                message += item.getFull_name();
                Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                good_to_submit = false;
                break;
            }
        }

        if (good_to_submit) {
            Toast toast = Toast.makeText(getApplicationContext(),
                "Submitting CoScholastics Grades...", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL |
                Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            save(adapter);
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
                save(adapter);
                break;
            case R.id.submit_marks:
                submit(adapter);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
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

}
