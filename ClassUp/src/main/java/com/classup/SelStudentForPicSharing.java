package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SelStudentForPicSharing extends AppCompatActivity {
    private NumberPicker classPicker;
    private NumberPicker sectionPicker;

    String server_ip;
    String school_id;
    String image;

    final Activity a = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sel_student_for_pic_sharing);
        //image = getIntent().getStringExtra("image");
        image = SessionManager.getInstance().getImage();
        //System.out.println("image = " + image);

        if(getIntent().getStringExtra("sender").equals("image_video"))  {
            String description = getIntent().getStringExtra("description");
            TextView bd = findViewById(R.id.brief_descrption);
            bd.setText(description);
        }

        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        System.out.println("school_id=" + school_id);
        String classUrl = server_ip + "/academics/class_list/" + school_id + "/?format=json";
        String sectionUrl = server_ip + "/academics/section_list/" + school_id + "/?format=json";

        classPicker = findViewById(R.id.pick_class2);
        sectionPicker = findViewById(R.id.pick_section2);
        setupPicker(classPicker, classUrl, "standard", "class_api");
        setupPicker(sectionPicker, sectionUrl, "section", "section_api");
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
                            "Error while retrieving " + tag + "list",
                            Toast.LENGTH_LONG).show();
                        //startActivity(new Intent("com.classup.SetSubjects"));
                    } catch (Exception e) {
                        System.out.println("ran into exception during " + tag);
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                            "Error while retrieving " + tag + "list",
                            Toast.LENGTH_LONG).show();
                    }
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
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public void select_students(View view)   {
        TextView bd = findViewById(R.id.brief_descrption);
        String brief_description = bd.getText().toString();
        if(brief_description.equals(""))    {
            Toast toast = Toast.makeText(getApplicationContext(),
                "Please enter brief description", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
        else {
            final String[] classList = classPicker.getDisplayedValues();
            // Get the section
            final String[] sectionList = sectionPicker.getDisplayedValues();
            final Intent intent = new Intent(this, SelectStudent.class);
            intent.putExtra("sender", "share_image");
            //intent.putExtra("image", image);
            intent.putExtra("brief_description", brief_description);
            intent.putExtra("class", classList[(classPicker.getValue())]);
            intent.putExtra("section", sectionList[(sectionPicker.getValue())]);
            startActivity(intent);
        }
    }

    public void share_with_whole_class(View view)   {
        TextView bd = findViewById(R.id.brief_descrption);
        final String brief_description = bd.getText().toString();
        if(brief_description.equals(""))    {
            Toast toast = Toast.makeText(getApplicationContext(),
                "Please enter brief description", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
        else {
            final String[] classList = classPicker.getDisplayedValues();
            // Get the section
            final String[] sectionList = sectionPicker.getDisplayedValues();
            final String the_class = classList[(classPicker.getValue())];
            final String section = sectionList[(sectionPicker.getValue())];
            String prompt = "Are you sure to upload this image to the whole class ";
            prompt += the_class;
            prompt += "-" + section + "?";

            final android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(this);
            builder.setMessage(prompt).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progressDialog = new ProgressDialog(a);
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        String timeStamp =
                            new SimpleDateFormat("yyyyMMdd_HHmmss").
                                format(new Date());
                        String teacher = SessionManager.getInstance().getLogged_in_user();
                        final String imageFileName = teacher + "-" + the_class + "_"
                            + "_" + timeStamp + ".jpg";
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("image", image);
                            jsonObject.put("image_name", imageFileName);
                            jsonObject.put("description", brief_description);
                            jsonObject.put("school_id", SessionManager.
                                getInstance().getSchool_id());
                            jsonObject.put("teacher", teacher);
                            jsonObject.put("class", the_class);
                            jsonObject.put("section", section);
                            jsonObject.put("whole_class", "true");

                        } catch (JSONException je) {
                            System.out.println("unable to create json for HW upload");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            ae.printStackTrace();
                        }
                        String url = server_ip + "/pic_share/upload_pic/";
                        final String tag = "Upload Pic";
                        JsonObjectRequest jsonObjReq = new JsonObjectRequest
                            (Request.Method.POST, url, jsonObject,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        progressDialog.dismiss();
                                        progressDialog.hide();
                                        Log.d(tag, response.toString());
                                        try {
                                            final String status =
                                                response.getString("status");
                                            final String message =
                                                response.getString("message");
                                            if (!status.equals("success")) {
                                                Toast toast =
                                                    Toast.makeText(getApplicationContext(), message,
                                                        Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.CENTER,
                                                    0,
                                                    0);
                                                toast.show();
                                            } else {
                                                Toast toast = Toast.makeText(getApplicationContext(),
                                                    message, Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.CENTER,
                                                    0,
                                                    0);
                                                toast.show();
                                                startActivity(new Intent
                                                    ("com.classup.TeacherMenu").
                                                    setFlags(Intent.
                                                        FLAG_ACTIVITY_NEW_TASK |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                finish();
                                            }
                                        } catch (org.json.JSONException je) {
                                            progressDialog.dismiss();
                                            progressDialog.hide();
                                            je.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.dismiss();
                                    progressDialog.hide();
                                    VolleyLog.d(tag, "Error: " + error.getMessage());
                                }
                            });
                        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0,
                            -1,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        com.classup.AppController.getInstance().
                            addToRequestQueue(jsonObjReq, tag);

                        Toast toast = Toast.makeText(getApplicationContext(),
                            "Image Upload in Progress. " +
                                "It will appear in Image/Video list after a few minutes",
                            Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        Intent intent1 = new Intent(getApplicationContext(),
                            CommunicationCenter.class);
                        intent1.putExtra("sender", "teacher_menu");
                            //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        //Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent1);
                        //finish();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            // Create the AlertDialog object and return it
            builder.show();

        }
    }
}
