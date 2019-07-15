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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
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

public class EditStudent extends AppCompatActivity {
    final Context context = this;


    private NumberPicker classPicker;
    private NumberPicker sectionPicker;
    final String tag = "EditStudent";
    String server_ip;
    String school_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

        Intent intent = getIntent();

        final EditText txt_reg_no = findViewById(R.id.reg_no);
        txt_reg_no.setEnabled(false);
        final EditText txt_first_name = findViewById(R.id.first_name);
        final EditText txt_last_name = findViewById(R.id.the_surname);
        final EditText txt_parent_name = findViewById(R.id.parent_name);
        final EditText txt_mobile1 = findViewById(R.id.mobile1);
        final EditText txt_mobile2 = findViewById(R.id.mobile2);
        final EditText txt_roll_no = findViewById(R.id.roll_no);
        ((TextView) findViewById(R.id.tv_roll_no)).setText("Roll No");

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
        // as we are using sihgleton pattern to get the logged in user, sometimes the method
        // call returns a blank string. In this case we will retry for 20 times and if not
        // successful even after then we will ask the user to log in again
        int i = 0;
        while (logged_in_user.equals("")) {
            logged_in_user = SessionManager.getInstance().getLogged_in_user();
            if (i++ == 20) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "There seems to be some problem with network. Please re-login",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Intent intent1 = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(intent1);
            }
        }

        classPicker = findViewById(R.id.class_picker);
        sectionPicker = findViewById(R.id.section_picker);
        setupPicker(classPicker, classUrl, "standard", "class_api");
        setupPicker(sectionPicker, sectionUrl, "section", "section_api");

        final String student_id = intent.getStringExtra("student_id");
        String student_url = server_ip + "/student/get_student_detail/" + student_id;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, student_url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    txt_reg_no.setText(response.getString("erp_id"));
                                    txt_first_name.setText(response.getString("first_name"));
                                    txt_last_name.setText(response.getString("last_name"));
                                    txt_parent_name.setText(response.getString("parent_name"));
                                    txt_mobile1.setText(response.getString("parent_mobile1"));
                                    txt_mobile2.setText(response.getString("parent_mobile2"));
                                    txt_roll_no.setText(response.getString("roll_no"));

                                    String the_class = response.getString("class");
                                    String[] class_array = classPicker.getDisplayedValues();
                                    List<String> class_list = Arrays.asList(class_array);
                                    classPicker.setValue(class_list.indexOf(the_class));

                                    String section = response.getString("section");
                                    String[] section_array =
                                            sectionPicker.getDisplayedValues();
                                    List<String> section_list = Arrays.asList(section_array);
                                    sectionPicker.setValue(section_list.indexOf(section));

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
                                        "Slow network connection or " +
                                                "No internet connectivity",
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

    public void setupPicker(final NumberPicker picker, String url,
                            final String item_to_extract, final String tag) {
        final ArrayList<String> item_list = new ArrayList<>();

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
                        System.out.println("inside volley error handler");
                        // TODO Auto-generated method stub
                    }
                });
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Delete").
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        m.add(0, 1, 0, "Update")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String user = SessionManager.getInstance().getLogged_in_user();
        final String reg_no = ((EditText) findViewById(R.id.reg_no)).getText().toString();
        final String first_name = ((EditText) findViewById(R.id.first_name)).getText().toString();
        final String last_name = ((EditText) findViewById(R.id.the_surname)).getText().toString();
        final String parent_name = ((EditText) findViewById(R.id.parent_name)).getText().toString();
        final String mobile1 = ((EditText) findViewById(R.id.mobile1)).getText().toString();
        final String mobile2 = ((EditText) findViewById(R.id.mobile2)).getText().toString();
        final String roll_no = ((EditText) findViewById(R.id.roll_no)).getText().toString();

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
                String prompt = "Are you sure you want to delete this student?";
                builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                        OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            Intent intent = getIntent();
                            jsonObject.put("student_id", intent.getStringExtra("student_id"));
                        } catch (JSONException je) {
                            System.out.println("unable to create json for " +
                                    "update student");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            ae.printStackTrace();
                        }
                        String url = server_ip + "/setup/delete_student/";

                        JsonObjectRequest jsonObjReq = new JsonObjectRequest
                                (Request.Method.POST, url, jsonObject,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Log.d(tag, response.toString());
                                                // 12/09/17 - Now we are building the custom
                                                // Analysis via AWS
                                                try {
                                                    AnalyticsEvent event =
                                                            SessionManager.
                                                                    analytics.getEventClient().
                                                                    createEvent("Delete Student");
                                                    event.addAttribute("user",
                                                            SessionManager.getInstance().
                                                            getLogged_in_user());
                                                    SessionManager.analytics.
                                                            getEventClient().
                                                            recordEvent(event);
                                                } catch (NullPointerException exception)    {
                                                    System.out.println("flopped in creating " +
                                                            "analytics Delete Student");
                                                } catch (Exception exception)   {
                                                    System.out.println("flopped in " +
                                                            "creating analytics Delete Student");
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
                                "Student Deleted.", Toast.LENGTH_SHORT);
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
                if (first_name.equals("")) {
                    Toast toast = Toast.makeText(this, "First Name is blank",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
                if (last_name.equals("")) {
                    Toast toast = Toast.makeText(this, "Surname/Last Name is blank",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
                if (parent_name.equals("")) {
                    Toast toast = Toast.makeText(this, "Parent Name is blank",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
                if (mobile1.equals("")) {
                    Toast toast = Toast.makeText(this, "Mobile1  is blank",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                // mobile numbers should be of exactly 10 digits
                if (mobile1.length() != 10) {
                    Toast toast = Toast.makeText(this,
                            "Mobile1  is invalid. Should be of 10 digits", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
                if (!mobile2.equals("")) {
                    if (mobile2.length() != 10) {
                        Toast toast = Toast.makeText(this,
                                "Mobile2  is invalid. Should be of 10 digits",
                            Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return super.onOptionsItemSelected(item);
                    }
                }

                // check that the registration number is available
                final String tag = "UpdateStudent";
                final String school_id = SessionManager.getInstance().getSchool_id();

                prompt = "Are you sure to Update ";
                prompt += reg_no + ": ";
                prompt += first_name + " " + last_name;
                prompt += " C/o " + parent_name;
                prompt += " with mobile1: " + mobile1;
                prompt += ", mobile2: " + mobile2;
                prompt += " to class " + the_class + " " + section;
                prompt += ", Roll No: " + roll_no;
                prompt += "?";
                final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.
                        Builder(context);
                builder1.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                        OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("user", user);
                            jsonObject.put("school_id", school_id);
                            Intent intent = getIntent();
                            jsonObject.put("student_id", intent.getStringExtra("student_id"));
                            jsonObject.put("reg_no", reg_no);
                            jsonObject.put("first_name", first_name);
                            jsonObject.put("last_name", last_name);
                            jsonObject.put("parent_name", parent_name);
                            jsonObject.put("mobile1", mobile1);
                            jsonObject.put("mobile2", mobile2);
                            jsonObject.put("the_class", the_class);
                            jsonObject.put("section", section);
                            jsonObject.put("roll_no", roll_no);
                        } catch (JSONException je) {
                            System.out.println("unable to create json for " +
                                    "aupdate student");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            ae.printStackTrace();
                        }
                        String url = server_ip + "/setup/update_student/";

                        JsonObjectRequest jsonObjReq = new JsonObjectRequest
                                (Request.Method.POST, url, jsonObject,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Log.d(tag, response.toString());
                                                // 12/09/17 - Now we are building the custom
                                                // Analysis via AWS
                                                try {
                                                    AnalyticsEvent event =
                                                            SessionManager.
                                                                    analytics.getEventClient().
                                                                    createEvent
                                                                        ("Update Student");
                                                    event.addAttribute("user", SessionManager.
                                                            getInstance().
                                                            getLogged_in_user());
                                                    SessionManager.analytics.
                                                            getEventClient().
                                                            recordEvent(event);
                                                } catch (NullPointerException exception)    {
                                                    System.out.println("flopped in creating " +
                                                            "analytics Updte Student");
                                                } catch (Exception exception)   {
                                                    System.out.println("flopped in " +
                                                            "creating analytics Update Student");
                                                }
                                            }
                                        }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        VolleyLog.d(tag, "Error: " + error.getMessage());
                                    }
                                });
                        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0,
                            -1,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        com.classup.AppController.getInstance().
                                addToRequestQueue(jsonObjReq, tag);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Student Updated.", Toast.LENGTH_SHORT);
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
}



