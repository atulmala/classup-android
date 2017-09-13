package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SetSubjects extends AppCompatActivity {
    String tag = "SetSubjects";
    final String server_ip = MiscFunctions.getInstance().getServerIP(this);
    final ArrayList<String>subjects = new ArrayList<>();
    final ArrayList<String>codes = new ArrayList<>();
    final ArrayList<String> selected_subjects = new ArrayList<>();
    final ArrayList<String> subjects_to_remove = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_subjects);

        try {
            AnalyticsEvent event =
                    SessionManager.getInstance().analytics.getEventClient().
                            createEvent("Set Subjects");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.getInstance().analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Set Subjects");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Set Subjects");
        }

        final ArrayList<SubjectListSource> subject_list = new ArrayList<SubjectListSource>();

        // retrieve the list of subjects already set for this teacher. Those subjects will be
        // shown checked in the list
        final String server_ip = MiscFunctions.getInstance().getServerIP(this);
        final String school_id = SessionManager.getInstance().getSchool_id();
        String logged_in_user = SessionManager.getInstance().getLogged_in_user();

        // as we are using singleton pattern to get the logged in user, sometimes the method
        // call returns a blank string. In this case we will retry for 20 times and if not
        // successful even after then we will ask the user to log in again
        int i = 0;
        while (logged_in_user.equals("")) {
            logged_in_user = SessionManager.getInstance().getLogged_in_user();
            if (i++ == 20)  {
                Toast.makeText(this,
                        "There seems to be some problem with network. Please re-login",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this,
                        LoginActivity.class);
                this.startActivity(intent);
            }
        }

        // retrieve the list of subjects already set for this teacher
        String subjectUrl =  server_ip + "/teachers/teacher_subject_list/" +
                logged_in_user + "/?format=json";
        final String tag = "Retrieve_Subjects_For_LoggedinUser";
        final ArrayList<String> already_set_subjects = new ArrayList<String>();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, subjectUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                String an_Item = jo.getString("subject");
                                selected_subjects.add(an_Item);
                                already_set_subjects.add(an_Item);
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
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Server error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),
                                    "Network error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        final SetSubjectsAdapter adapter = new SetSubjectsAdapter(this,
                android.R.layout.simple_list_item_checked, subject_list, already_set_subjects);

        Context c = this.getApplicationContext();

        final ListView listView = (ListView)findViewById(R.id.set_subject_list);

        String url =  server_ip + "/academics/subject_list/"
                + school_id + "/?format=json";
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest1 = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);

                                String subject_name = jo.getString("subject_name");
                                String subject_code = jo.getString("subject_code");

                                subject_list.add(new SubjectListSource(subject_code, subject_name));
                                subjects.add(subject_name);
                                codes.add(subject_code);
                                adapter.notifyDataSetChanged();
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                        "while trying to the subject list");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the marks/grade list");
                                e.printStackTrace();
                            }
                        }
                        progressDialog.hide();
                        progressDialog.dismiss();

                        //listView.setAdapter(arrayAdapter);
                        listView.setAdapter(adapter);

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
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Server error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),
                                    "Network error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest1, tag);

        listView.setDivider(new ColorDrawable(0x99F10529));
        listView.setDividerHeight(1);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.subject_name);

                if (!textView.isChecked()) {
                    textView.setChecked(true);

                    selected_subjects.add(codes.get(i));

                    if (subjects_to_remove.contains(codes.get(i)))
                        subjects_to_remove.remove(codes.get(i));

                    // also add to the selected subjects list of the adapter
                    adapter.selected_subjects.add(subjects.get(i));
                    adapter.notifyDataSetChanged();

                    String message = "Subject " + subjects.get(i) + " "
                            + codes.get(i) + " has been selected.";

                } else {
                    textView.setChecked(false);
                    // this subject has to be removed from the subject list of this teacher
                    subjects_to_remove.add(codes.get(i));

                    // also remove from the selected subjects list of the adapter
                    adapter.selected_subjects.remove(subjects.get(i));
                    adapter.notifyDataSetChanged();

                    if (selected_subjects.contains(codes.get(i))) {
                        selected_subjects.remove(codes.get(i));
                        adapter.selected_subjects.remove((codes.get(i)));
                    }
                }
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

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
        menu.add(0, 0, 0, "Set Subjects").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                if (1 > selected_subjects.size()) {
                    Toast.makeText(getApplicationContext(), "Please select at least one subject",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // first we unset the subjects
                    JSONObject params1 = new JSONObject();
                    for (int i = 0; i < subjects_to_remove.size(); i++) {
                        try {
                            params1.put(subjects.get(codes.indexOf(subjects_to_remove.get(i))),
                                    subjects_to_remove.get(i));
                        } catch (JSONException je) {
                            System.out.println("unable to create json for subjects to be deleted");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            System.out.println("array out of bounds exception");
                            ae.printStackTrace();
                        }
                    }

                    String loggde_in_user = SessionManager.getInstance().getLogged_in_user();

                    String url1 =  server_ip + "/teachers/unset_subjects/" +
                            loggde_in_user + "/";
                    JsonObjectRequest jsonObjReq1 = new JsonObjectRequest(Request.Method.POST,
                            url1, params1,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(tag, response.toString());
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(tag, "Error: " + error.getMessage());
                            //progressDialog.hide();
                            if (error instanceof TimeoutError ||
                                    error instanceof NoConnectionError) {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection, please try later",
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof ServerError) {
                                Toast.makeText(getApplicationContext(),
                                        "Server error, please try later",
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(getApplicationContext(),
                                        "Network error, please try later",
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof ParseError) {
                                //TODO
                            }
                        }
                    });
                    com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq1, tag);

                    JSONObject params = new JSONObject();
                    for (int i = 0; i < selected_subjects.size(); i++) {
                        try {
                            params.put(subjects.get(codes.indexOf(selected_subjects.get(i))),
                                    selected_subjects.get(i));
                        } catch (JSONException je) {
                            System.out.println("unable to create json for selected subjects");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            System.out.println("array out of bounds exception");
                            ae.printStackTrace();
                        }
                    }
                    String url =  server_ip + "/teachers/set_subjects/" +
                            loggde_in_user + "/";
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                            url, params,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(tag, response.toString());
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(tag, "Error: " + error.getMessage());
                            //progressDialog.hide();
                            if (error instanceof TimeoutError ||
                                    error instanceof NoConnectionError) {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection, please try later",
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof ServerError) {
                                Toast.makeText(getApplicationContext(),
                                        "Server error, please try later",
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(getApplicationContext(),
                                        "Network error, please try later",
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof ParseError) {
                                //TODO
                            }
                        }
                    });
                    com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
                    Toast.makeText(getApplicationContext(),
                            "Subjects set. You can now start attendance or scheuling tests",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent("com.classup.TeacherMenu").
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
