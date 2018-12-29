package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

public class TestDetails extends AppCompatActivity {
    EditText max_marks;
    EditText pass_marks;
    EditText comments;
    CheckBox grade_based;

    String exam_id;
    String exam_title;
    String the_class;
    String section;
    String subject;
    String date;
    String month;
    String year;

    String title;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);
        context = this;

        Intent intent = getIntent();
        exam_id = intent.getStringExtra("exam_id");
        exam_title = intent.getStringExtra("exam_title");
        the_class = intent.getStringExtra("the_class");
        section = intent.getStringExtra("section");
        subject = intent.getStringExtra("subject");
        date = intent.getStringExtra("date");
        month = intent.getStringExtra("month");
        year = intent.getStringExtra("year");

        title = exam_title + " test for " + subject + " class: " + the_class + "-" + section;
        title += " Date: " + date + "/" + month + "/" + year;
        TextView txt_title = findViewById(R.id.title);
        txt_title.setText(title);


        max_marks = findViewById(R.id.txt_max_marks);
        pass_marks = findViewById(R.id.txt_passing_marks);
        comments = findViewById(R.id.txt_comments);
        grade_based = findViewById(R.id.chk_whether_grade_based);

        max_marks.setText("0");
        pass_marks.setText("0");
        comments.setText(" ");

        grade_based.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view1) {
                if (grade_based.isChecked()) {
                    GradeBased.getInstance().setGrade_based("True");
                    max_marks.setEnabled(false);
                    pass_marks.setEnabled(false);
                } else {
                    GradeBased.getInstance().setGrade_based("False");
                    max_marks.setEnabled(true);
                    pass_marks.setEnabled(true);
                }
            }
        });
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Done").
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean good_to_go = true;
        if (max_marks.getText().toString().equals("0") || max_marks.getText().toString().equals(""))
        {
            Toast toast = Toast.makeText(this,
                "Max marks cannot be Zero/Blank", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            good_to_go = false;
        }
        if (pass_marks.getText().toString().equals("")) {
            Toast toast = Toast.makeText(this,
                "Passing marks cannot be Blank", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            good_to_go = false;
        }
        if(good_to_go) {
            final String mm = max_marks.getText().toString();
            final String pm = pass_marks.getText().toString();
            title += " Max Marks: " + mm + ", Pass Marks: " + pm;
            String prompt = "Are you sure you want to schedule this test?\n\n" + title;

            new AlertDialog.Builder(this)
                .setTitle("Please confirm")
                .setMessage(prompt)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final ProgressDialog progressDialog =
                            new ProgressDialog(context);
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        String g = "1";
                        switch (GradeBased.getInstance().getGrade_based()) {
                            case "True":
                                g = "0";
                                break;
                            case "False":
                                g = "1";

                        }
                        // in case of grade based test, teacher will leave the max marks
                        // and pass marks blank. In this case the url will not be formed
                        // correctly. Hence we need to set these to zero explicitly
                        String _mm = "mm";
                        String _pm = "pm";
                        if (mm.equals(""))
                            _mm = "0";
                        else
                            _mm = mm;
                        if (pm.equals(""))
                            _pm = "0";
                        else
                            _pm = pm;
                        String teacher = SessionManager.getInstance().getLogged_in_user();
                        String server_ip = MiscFunctions.getInstance().
                            getServerIP(getApplicationContext());
                        String school_id = SessionManager.getInstance().getSchool_id();

                        // now we have to send a POST request to backend to create the test
                        String cmnts = comments.getText().toString();
                        if (cmnts.equals(" "))
                            cmnts = "no comments";
                        String url = server_ip + "/academics/create_test1/" + school_id
                            + "/" + the_class + "/" + section + "/" + subject
                            + "/" + teacher
                            + "/" + date + "/" + month + "/" + year
                            + "/" + _mm + "/" + _pm
                            + "/" + g + "/" + cmnts + "/"
                            + exam_id + "/";
                        url = url.replace(" ", "%20");
                        String tag = "Create Test";
                        JSONObject jsonObject = new JSONObject();
                        final JsonObjectRequest request =
                            new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // 11/09/17 - Now we are building the custom
                                    // Analysis via AWS

                                    AnalyticsEvent scheduleTestEvent =
                                        SessionManager.getInstance().analytics.getEventClient().
                                            createEvent("Schedule Test");
                                    scheduleTestEvent.addAttribute("user",
                                        SessionManager.getInstance().getLogged_in_user());
                                    SessionManager.getInstance().analytics.getEventClient().
                                        recordEvent(scheduleTestEvent);
                                    try {
                                        progressDialog.hide();;
                                        progressDialog.cancel();
                                        final String outcome = response.getString("outcome");
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                            outcome, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                        startActivity(new Intent("com.classup.TeacherMenu").
                                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    } catch (org.json.JSONException je) {
                                        progressDialog.dismiss();
                                        progressDialog.hide();
                                        je.printStackTrace();
                                    }
                                }
                            },


                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();

                                    if (error instanceof TimeoutError ||
                                        error instanceof NoConnectionError) {
                                        Toast.makeText(getApplicationContext(),
                                            "Slow network connection or" +
                                                " No internet connectivity",
                                            Toast.LENGTH_LONG).show();
                                    } else if (error instanceof ServerError) {
                                        Toast.makeText(getApplicationContext(),
                                            "Slow network connection or " +
                                                "No internet connectivity",
                                            Toast.LENGTH_LONG).show();
                                    } else if (error instanceof NetworkError) {
                                        Toast.makeText(getApplicationContext(),
                                            "Slow network connection or " +
                                                "No internet connectivity",
                                            Toast.LENGTH_LONG).show();
                                    } else if (error instanceof ParseError) {
                                        //TODO
                                    }
                                }
                            });
                        com.classup.AppController.getInstance().addToRequestQueue(request, tag);
                        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));



                    }})
                .setNegativeButton(android.R.string.no, null).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
