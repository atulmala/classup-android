package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelStudentForPicSharing extends AppCompatActivity {
    private NumberPicker classPicker;
    private NumberPicker sectionPicker;

    String server_ip;
    String school_id;
    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sel_student_for_pic_sharing);

        image = getIntent().getStringExtra("image");

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
        final String[] classList = classPicker.getDisplayedValues();
        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();
        final Intent intent = new Intent(this, SelectStudent.class);
        intent.putExtra("sender", "share_image");
        intent.putExtra("image", image);
        intent.putExtra("class", classList[(classPicker.getValue())]);
        intent.putExtra("section", sectionList[(sectionPicker.getValue())]);
        startActivity(intent);
    }
}
