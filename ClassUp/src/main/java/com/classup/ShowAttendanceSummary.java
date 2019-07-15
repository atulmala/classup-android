package com.classup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.util.Calendar;

public class ShowAttendanceSummary extends AppCompatActivity {
    String tag = "ShowAttendanceSummary";
    String school_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attendance_summary);

        try {
            AnalyticsEvent event =
                    SessionManager.analytics.getEventClient().
                            createEvent("Teacher Attendance Summary");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Teacher Attendance Summary");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Teacher Attendance Summary");
        }


        school_id = SessionManager.getInstance().getSchool_id();
        // set up the header rows
        final TableRow header_row = findViewById(R.id.header_row_att_summary);
        ((TextView) header_row.findViewById(R.id.txt_class_sec_att_summary)).
                setText((getIntent().getStringExtra("class") + "-" +
                        getIntent().getStringExtra("section")));
        ((TextView) header_row.findViewById(R.id.txt_subjec_att_summary)).
                setText((getIntent().getStringExtra("subject")));

        String url = MiscFunctions.getInstance().getServerIP(getApplicationContext()) +
                "/academics/get_working_days1/?school_id=" + school_id;

        // showing month/year is slightly tricky. As we are passing strings like "current year",
        // "last year", or "till date", we need get the year value
        Integer yr = Calendar.getInstance().get(Calendar.YEAR);
        System.out.print("year=" + Integer.toString(yr));
        //Toast.makeText(getApplicationContext(), Integer.toString(yr), Toast.LENGTH_SHORT).show();
        switch (getIntent().getStringExtra("year")) {
            case "current_year":
                ((TextView) header_row.findViewById(R.id.txt_time_period_att_summary)).
                        setText((getIntent().getStringExtra("month") + "-" +
                                Integer.toString(yr - 2000)));
                // side by side keep building the url
                url += "&year=" + Integer.toString(yr);
                break;

            case "last_year":

                ((TextView) header_row.findViewById(R.id.txt_time_period_att_summary)).
                        setText((getIntent().getStringExtra("month") + "-" +
                                Integer.toString(yr - 2001)));
                url += "&year=" + Integer.toString(yr- 1);
                yr--;
                break;

            case "till_date":
                ((TextView) header_row.findViewById(R.id.txt_time_period_att_summary)).
                        setText("Till Date");
                url += "&year=till_date";
                break;
        }

        // get working days
        url += "&month=" + getIntent().getStringExtra("month");
        url += "&class=" + getIntent().getStringExtra("class");
        url += "&section=" + getIntent().getStringExtra("section");
        url += "&subject=" + getIntent().getStringExtra("subject");
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(tag, response.toString());
                        try {
                            String working_days = response.get("working_days").toString();
                            System.out.println("working_days=" + working_days);
                            ((TextView)header_row.findViewById(R.id.txt_working_days_att_summary)).
                                    setText(working_days);
                        }
                        catch (org.json.JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(tag, "Error: " + error.getMessage());
            }
        });
        com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);

        final TableLayout tableLayout = findViewById(R.id.tbl_att_summary);
        TableLayout tbl_title = findViewById(R.id.tbl_att_header);
        // set the title row
        TableRow title_row = new TableRow(getApplicationContext());
        TableRow.LayoutParams lp =
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        title_row.setLayoutParams(lp);

        TextView rn = new TextView(getApplicationContext());
        rn.setText("Roll #");
        rn.setTextColor(Color.BLACK);
        rn.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        rn.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(rn);

        TextView fn = new TextView(getApplicationContext());

        fn.setText("Name");
        fn.setTextColor(Color.BLACK);
        fn.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        fn.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(fn);

        TextView att = new TextView(getApplicationContext());
        att.setText("Att  ");
        att.setTextColor(Color.BLACK);
        att.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        att.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(att);

        TextView perc = new TextView(getApplicationContext());
        perc.setText("    %    ");
        perc.setTextColor(Color.BLACK);
        perc.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        perc.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(perc);

        tableLayout.addView(title_row);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final ArrayList<TableRow> tableRows = new ArrayList<>();

        // get the list of students
        String tag = "AttendanceListSummary";
        url =
                MiscFunctions.getInstance().getServerIP(getApplicationContext())
                + "/academics/get_attendance_summary/?school_id=" + school_id +
                        "&class=" + getIntent().getStringExtra("class")
                + "&section=" + getIntent().getStringExtra("section")
                + "&subject=" + getIntent().getStringExtra("subject")
                + "&month=" + getIntent().getStringExtra("month");
        if (getIntent().getStringExtra("year").equals("till_date"))
                url += "&year=till_date";
        else
            url += "&year=" +  Integer.toString(yr);
        // if url contains space like in Social Science
        url = url.replace(" ", "%20");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (Integer i = 0; i < response.length(); i++)
                            try {
                                JSONObject jo = response.getJSONObject(i);

                                // prepare a new row
                                TableRow detail_row = new TableRow(getApplicationContext());

                                TableRow.LayoutParams lp =
                                        new TableRow.LayoutParams(TableRow.
                                                LayoutParams.MATCH_PARENT);
                                detail_row.setLayoutParams(lp);

                                // get the name of the student. We need to join first and last names
                                String full_name = jo.getString("name");

                                // put the name inside row column
                                TextView fn = new TextView(getApplicationContext());
                                fn.setText(full_name);
                                fn.setTextColor(Color.BLACK);
                                fn.setPadding(5, 5, 5, 5);
                                //fn.setHeight(20);
                                fn.setTextSize(18);
                                fn.setBackgroundResource(R.drawable.cell_shape);

                                // get the roll number of the student
                                //String roll_no = jo.getString("roll_number");
                                Integer roll_no = i + 1;
                                // put the roll inside row column
                                TextView rn = new TextView(getApplicationContext());
                                rn.setBackgroundResource(R.drawable.cell_shape);
                                rn.setTextColor(Color.BLACK);
                                rn.setPadding(5, 5, 5, 5);
                                //rn.setHeight(100);
                                rn.setTextSize(18);
                                rn.setText(roll_no.toString());

                                detail_row.addView(rn);
                                detail_row.addView(fn);

                                // get the no of days present
                                String present_days = jo.getString("present_days");
                                // put the days inside the row column
                                TextView pd = new TextView(getApplicationContext());
                                pd.setBackgroundResource(R.drawable.cell_shape);
                                pd.setTextColor(Color.BLACK);
                                pd.setPadding(5, 5, 5, 5);
                                pd.setTextSize(18);
                                pd.setText(present_days);

                                detail_row.addView(pd);

                                // get the % od days present
                                String percentage = jo.getString("percentage");
                                TextView perc = new TextView((getApplicationContext()));
                                perc.setBackgroundResource(R.drawable.cell_shape);
                                perc.setTextColor(Color.BLACK);
                                perc.setPadding(5, 5, 5, 5);
                                perc.setTextSize(18);
                                perc.setText(percentage);

                                detail_row.addView(perc);
                                tableRows.add(detail_row);
                                tableLayout.addView(detail_row);

                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                        "while trying to fetch the list of students");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the list of students");
                                e.printStackTrace();
                            }
                        progressDialog.hide();
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error handler");
                        error.printStackTrace();
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            Toast.makeText(getApplicationContext(),
                                    "Time-out error. ",
                                    Toast.LENGTH_LONG).show();
                        }  else if (error instanceof ServerError) {
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
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Done").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                done();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
    public void done() {
        startActivity(new Intent("com.classup.TeacherMenu").
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }


}
