package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
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
import java.util.Arrays;
import java.util.List;

public class EditTeacher extends AppCompatActivity {
    final Context context = this;


    private NumberPicker classPicker;
    private NumberPicker sectionPicker;
    final String tag = "EditTeacher";
    String server_ip;
    String school_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

        school_id = SessionManager.getInstance().getSchool_id();

        Intent intent = getIntent();

        final EditText txt_teacher_name = (EditText) findViewById(R.id.teacher_name);
        txt_teacher_name.setText(intent.getStringExtra("teacher_name"));

        final EditText txt_teacher_login = (EditText) findViewById(R.id.teacher_login);
        txt_teacher_login.setEnabled(false);
        txt_teacher_login.setText(intent.getStringExtra("teacher_login"));

        final EditText txt_teacher_mobile = (EditText) findViewById(R.id.teacher_mobile);
        txt_teacher_mobile.setText(intent.getStringExtra("teacher_mobile"));

        final CheckBox chk_whether_class_teacher = (CheckBox) findViewById(R.id.chk_class_teacher);

        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        System.out.println("school_id=" + school_id);

        String classUrl = server_ip + "/academics/class_list/" +
                school_id + "/?format=json";
        final String sectionUrl = server_ip + "/academics/section_list/" +
                school_id + "/?format=json";

        String logged_in_user = SessionManager.getInstance().getLogged_in_user();

        classPicker = (NumberPicker) findViewById(R.id.class_picker2);
        sectionPicker = (NumberPicker) findViewById(R.id.section_picker2);
        setupPicker(classPicker, classUrl, "standard", "class_api");
        setupPicker(sectionPicker, sectionUrl, "section", "section_api");

        // check whether this teacher is a class teacher or not
        String url = server_ip + "/teachers/whether_class_teacher/" +
                intent.getStringExtra("teacher_id");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String is_class_teacher =
                                            response.getString("is_class_teacher");
                                    if (is_class_teacher.equals("true")) {
                                        chk_whether_class_teacher.setChecked(true);
                                        String the_class = response.getString("the_class");
                                        String[] class_array = classPicker.getDisplayedValues();
                                        List<String> class_list = Arrays.asList(class_array);
                                        classPicker.setValue(class_list.indexOf(the_class));

                                        String section = response.getString("section");
                                        String[] section_array =
                                                sectionPicker.getDisplayedValues();
                                        List<String> section_list = Arrays.asList(section_array);
                                        sectionPicker.setValue(section_list.indexOf(section));
                                    } else {
                                        chk_whether_class_teacher.setChecked(false);
                                        classPicker.setEnabled(false);
                                        sectionPicker.setEnabled(false);
                                    }
                                    progressDialog.hide();
                                    progressDialog.dismiss();
                                } catch (JSONException je) {
                                    System.out.println("Ran into JSON exception " +
                                            "while trying to check registration number");
                                    je.printStackTrace();
                                } catch (Exception e) {
                                    System.out.println("Caught General exception " +
                                            "while trying to check registration number ");
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            if (!MiscFunctions.getInstance().checkConnection
                                    (getApplicationContext())) {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {

                        }
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag);

        chk_whether_class_teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chk_whether_class_teacher.isChecked()) {
                    classPicker.setEnabled(true);
                    sectionPicker.setEnabled(true);
                } else {
                    classPicker.setEnabled(false);
                    sectionPicker.setEnabled(false);
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

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Delete").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        m.add(0, 1, 0, "Update").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String teacher_name =
                ((EditText) findViewById(R.id.teacher_name)).getText().toString();

        final String teacher_login =
                ((EditText) findViewById(R.id.teacher_login)).getText().toString();

        final String teacher_mobile =
                ((EditText) findViewById(R.id.teacher_mobile)).getText().toString();

        final CheckBox chk_whether_class_teacher = (CheckBox) findViewById(R.id.chk_class_teacher);

        // Get the class
        final String[] classList = classPicker.getDisplayedValues();
        final String the_class = classList[(classPicker.getValue())];

        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();
        final String section = sectionList[(sectionPicker.getValue())];

        int id = item.getItemId();

        switch (id) {
            case 0:
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
                        Builder(context);
                String prompt = "Are you sure you want to delete this teacher?";
                builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                        OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            Intent intent = getIntent();
                            jsonObject.put("teacher_id", intent.getStringExtra("teacher_id"));
                        } catch (JSONException je) {
                            System.out.println("unable to create json for update teacher");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            ae.printStackTrace();
                        }
                        String url = server_ip + "/teachers/delete_teacher/";

                        JsonObjectRequest jsonObjReq = new JsonObjectRequest
                                (Request.Method.POST, url, jsonObject,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                // 12/09/17 - Now we are building the custom
                                                // Analysis via AWS
                                                try {
                                                    AnalyticsEvent event =
                                                            SessionManager.getInstance().
                                                                    analytics.getEventClient().
                                                                    createEvent("Delete Teacher");
                                                    event.addAttribute("user", SessionManager.
                                                            getInstance().
                                                            getLogged_in_user());
                                                    SessionManager.getInstance().analytics.
                                                            getEventClient().
                                                            recordEvent(event);
                                                } catch (NullPointerException exception)    {
                                                    System.out.println("flopped in creating " +
                                                            "analytics Delete Teacher");
                                                } catch (Exception exception)   {
                                                    System.out.println("flopped in " +
                                                            "creating analytics Delete Teacher");
                                                }
                                                Log.d(tag, response.toString());
                                            }
                                        }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        VolleyLog.d(tag, "Error: " + error.getMessage());
                                    }
                                });
                        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0, -1,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        com.classup.AppController.getInstance().
                                addToRequestQueue(jsonObjReq, tag);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Teacher Deleted.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
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
                return super.onOptionsItemSelected(item);

            case 1:
                // check for blanks
                if (teacher_name.equals("")) {
                    Toast toast = Toast.makeText(this, "Teacher Name is blank", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                if (teacher_mobile.equals("")) {
                    Toast toast = Toast.makeText(this, "Mobile1  is blank", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                if (!MiscFunctions.getInstance().isValidEmailAddress(teacher_login)) {
                    Toast toast = Toast.makeText(this, "Login id is invalid.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                // mobile numbers should be of exactly 10 digits
                if (teacher_mobile.length() != 10) {
                    Toast toast = Toast.makeText(this,
                            "Mobile  is invalid. Should be of 10 digits", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                // check that the registration number is available
                final String tag = "UpdateTeacher";
                final String school_id = SessionManager.getInstance().getSchool_id();

                prompt = "Are you sure to update this Teacher? ";

                final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.
                        Builder(context);
                builder1.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                        OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = getIntent();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("teacher_id", intent.getStringExtra("teacher_id"));
                            jsonObject.put("teacher_name", teacher_name);
                            jsonObject.put("teacher_login", teacher_login);
                            jsonObject.put("teacher_mobile", teacher_mobile);

                            if (chk_whether_class_teacher.isChecked()) {
                                jsonObject.put("is_class_teacher", "true");
                                jsonObject.put("school_id", school_id);
                                jsonObject.put("the_class", the_class);
                                jsonObject.put("section", section);
                            } else {
                                jsonObject.put("is_class_teacher", "false");
                            }
                        } catch (JSONException je) {
                            System.out.println("unable to create json for update teacher");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            ae.printStackTrace();
                        }
                        String url = server_ip + "/teachers/update_teacher/";

                        JsonObjectRequest jsonObjReq = new JsonObjectRequest
                                (Request.Method.POST, url, jsonObject,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Log.d(tag, response.toString());
                                                try {
                                                    String message = response.getString("message");
                                                    Toast toast = Toast.makeText
                                                            (getApplicationContext(),
                                                                    message, Toast.LENGTH_LONG);
                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                    toast.show();
                                                    // 12/09/17 - Now we are building the custom
                                                    // Analysis via AWS
                                                    try {
                                                        AnalyticsEvent event =
                                                                SessionManager.getInstance().
                                                                        analytics.getEventClient().
                                                                        createEvent
                                                                                ("Update Teacher");
                                                        event.addAttribute("user", SessionManager.
                                                                getInstance().
                                                                getLogged_in_user());
                                                        SessionManager.getInstance().analytics.
                                                                getEventClient().
                                                                recordEvent(event);
                                                    } catch (NullPointerException exception)    {
                                                        System.out.println("flopped in creating " +
                                                                "analytics Update Teacher");
                                                    } catch (Exception exception)   {
                                                        System.out.println("flopped in " +
                                                                "creating analytics Update Teacher");
                                                    }
                                                } catch (JSONException je) {
                                                    je.printStackTrace();
                                                }


                                            }
                                        }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        VolleyLog.d(tag, "Error: " + error.getMessage());
                                    }
                                });
                        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0, -1,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        com.classup.AppController.getInstance().
                                addToRequestQueue(jsonObjReq, tag);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Teacher Updated.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
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
                builder1.show();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setupPicker(final NumberPicker picker, String url,
                            final String item_to_extract, final String tag) {
        final ArrayList<String> item_list = new ArrayList<String>();

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
                                String an_Item = jo.getString(item_to_extract);
                                item_list.add(an_Item);
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
                        String[] picker_contents = item_list.toArray(new String[item_list.size()]);
                        try {
                            picker.setMaxValue(picker_contents.length - 1);
                            picker.setDisplayedValues(picker_contents);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("there seems to be no data for " + tag);
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "It looks that you have not yet set subjects. " +
                                            "Please set subjects", Toast.LENGTH_LONG).show();
                            startActivity(new Intent("com.classup.SetSubjects"));
                        } catch (Exception e) {
                            System.out.println("ran into exception during " + tag);
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "It looks that you have not yet set subjects. " +
                                            "Please set subjects", Toast.LENGTH_LONG).show();
                            startActivity(new Intent("com.classup.SetSubjects"));
                        }
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
    }
}
