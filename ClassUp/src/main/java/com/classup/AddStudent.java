package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
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

public class AddStudent extends AppCompatActivity {
    final Context context = this;

    private NumberPicker classPicker;
    private NumberPicker sectionPicker;
    String server_ip;
    String school_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        System.out.println("school_id=" + school_id);
        String classUrl = server_ip + "/academics/class_list/" +
                school_id + "/?format=json";
        String sectionUrl = server_ip + "/academics/section_list/" +
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
                Intent intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(intent);
            }
        }

        classPicker = (NumberPicker) findViewById(R.id.class_picker);
        sectionPicker = (NumberPicker) findViewById(R.id.section_picker);
        setupPicker(classPicker, classUrl, "standard", "class_api");
        setupPicker(sectionPicker, sectionUrl, "section", "section_api");
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
                        System.out.println("inside volley error handler");
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Add").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

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
                // check for blanks
                if (reg_no.equals("")) {
                    Toast toast = Toast.makeText(this, "Registration number is blank",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
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
                                "Mobile2  is invalid. Should be of 10 digits", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return super.onOptionsItemSelected(item);
                    }
                }

                // check that the registration number is available
                final String tag = "AddStudent";
                final String school_id = SessionManager.getInstance().getSchool_id();
                String url1 = server_ip + "/setup/check_reg_no/?school_id=" + school_id;
                url1 += "&reg_no=" + reg_no;
                url1 += "&the_class=" + the_class;
                url1 += "&section=" + section;
                url1 += "&roll_no=" + roll_no;
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url1, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            // check that this registration number is not
                                            // associated with any other student
                                            final String status = response.getString("status");
                                            if (status.equals("error")) {
                                                progressDialog.hide();
                                                progressDialog.dismiss();
                                                String message =
                                                        response.getString("error_message");
                                                Toast toast = Toast.makeText(context, message,
                                                        Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                            } else {
                                                progressDialog.hide();
                                                progressDialog.dismiss();
                                                String prompt = "Are you sure to add ";
                                                prompt += reg_no + ": ";
                                                prompt += first_name + " " + last_name;
                                                prompt += " C/o " + parent_name;
                                                prompt += " with mobile1: " + mobile1;
                                                prompt += ", mobile2: " + mobile2;
                                                prompt += " to class " + the_class + " " + section;
                                                prompt += ", Roll No: " + roll_no;
                                                prompt += "?";
                                                final android.app.AlertDialog.Builder builder =
                                                        new android.app.AlertDialog.
                                                                Builder(context);
                                                builder.setMessage(prompt).setPositiveButton("Yes",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick
                                                                    (DialogInterface dialog, int id) {
                                                                JSONObject jsonObject =
                                                                        new JSONObject();
                                                                try {
                                                                    jsonObject.put("user", user);
                                                                    jsonObject.put("school_id",
                                                                            school_id);
                                                                    jsonObject.put
                                                                            ("reg_no", reg_no);
                                                                    jsonObject.put
                                                                            ("first_name",
                                                                                    first_name);
                                                                    jsonObject.put("last_name",
                                                                            last_name);
                                                                    jsonObject.put("parent_name",
                                                                            parent_name);
                                                                    jsonObject.put("mobile1",
                                                                            mobile1);
                                                                    jsonObject.put("mobile2",
                                                                            mobile2);
                                                                    jsonObject.put("the_class",
                                                                            the_class);
                                                                    jsonObject.put("section",
                                                                            section);
                                                                    jsonObject.put("roll_no",
                                                                            roll_no);

                                                                } catch (JSONException je) {
                                                                    System.out.println
                                                                            ("unable to create json"
                                                                                    + " for " +
                                                                                    "add student");
                                                                    je.printStackTrace();
                                                                } catch (ArrayIndexOutOfBoundsException ae) {
                                                                    ae.printStackTrace();
                                                                }
                                                                String url =
                                                                        server_ip +
                                                                                "/setup/add_student/";

                                                                JsonObjectRequest jsonObjReq =
                                                                        new JsonObjectRequest
                                                                                (Request.Method.POST,
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
                                                                Toast toast = Toast.makeText(getApplicationContext(),
                                                                        "Student Added.",
                                                                        Toast.LENGTH_SHORT);
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
                                            }
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
                                                "Some problem at server end, please " +
                                                        "try after some time",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else if (error instanceof ServerError) {
                                    Toast.makeText(getApplicationContext(),
                                            "Server error, please try later",
                                            Toast.LENGTH_LONG).show();
                                } else if (error instanceof NetworkError) {

                                }
                            }
                        });
                // here we can sort the attendance list as per roll number

                com.classup.AppController.getInstance().
                        addToRequestQueue(jsonObjectRequest, tag);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

}
