package com.classup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SchoolAttendanceSummary extends AppCompatActivity {
    String tag = "ShowAttendanceSummary";
    String school_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_attendance_summary);
        // 12/09/17 - Now we are building the custom
        // Analysis via AWS
        try {
            AnalyticsEvent event =
                    SessionManager.analytics.getEventClient().
                            createEvent("School Attendance Summary");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics School Attendance Summary");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics School Attendance Summary");
        }

        school_id = SessionManager.getInstance().getSchool_id();
        // set up the header rows
        final TableRow header_row = findViewById(R.id.header_row_school_att_summary);

        Intent intent = getIntent();
        String d = intent.getStringExtra("date");
        String m = intent.getStringExtra("month");
        String y = intent.getStringExtra("year");
        String date = d + "/" + m + "/" + y;
        ((TextView) header_row.findViewById(R.id.txt_date_school_att_summary)).setText(date);

        final TableLayout tableLayout = findViewById(R.id.tbl_school_att_summary);
        TableLayout tbl_title = findViewById(R.id.tbl_att_header_school);
        // set the title row
        TableRow title_row = new TableRow(getApplicationContext());
        TableRow.LayoutParams lp =
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        title_row.setLayoutParams(lp);

        TextView the_class = new TextView(getApplicationContext());
        the_class.setText("  Class");
        the_class.setTextColor(Color.BLACK);
        the_class.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        the_class.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(the_class);

        TextView attendance = new TextView(getApplicationContext());

        attendance.setText("  Attendance");
        attendance.setTextColor(Color.BLACK);
        attendance.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        attendance.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(attendance);

        TextView percentage = new TextView(getApplicationContext());
        percentage.setText("    %    ");
        percentage.setTextColor(Color.BLACK);
        percentage.setPadding(5, 5, 5, 5);
        title_row.addView(percentage);
        percentage.setBackgroundResource(R.drawable.header_cell_shape);

        tableLayout.addView(title_row);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final ArrayList<TableRow> tableRows = new ArrayList<>();
        String server_ip = MiscFunctions.getInstance().getServerIP(this);
        String url = server_ip + "/operations/att_summary_school_device/?school_id=" + school_id  +
                "&date=" + d + "&month=" + m + "&year=" + y;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) // last row is different
                            try {
                                JSONObject jo = response.getJSONObject(i);

                                // prepare a new row
                                TableRow detail_row = new TableRow(getApplicationContext());

                                TableRow.LayoutParams lp =
                                        new TableRow.LayoutParams(TableRow.
                                                LayoutParams.MATCH_PARENT);
                                detail_row.setLayoutParams(lp);

                                // get the class & sectioon
                                String the_class = jo.getString("class");
                                TextView cls = new TextView(getApplicationContext());
                                cls.setText(the_class);
                                cls.setTextColor(Color.BLACK);
                                cls.setPadding(20, 5, 5, 5);
                                //fn.setHeight(20);
                                cls.setTextSize(18);
                                cls.setBackgroundResource(R.drawable.cell_shape);
                                detail_row.addView(cls);

                                // get the attendance
                                String attendance = jo.getString("attendance");
                                // put the roll inside row column
                                TextView att = new TextView(getApplicationContext());
                                att.setBackgroundResource(R.drawable.cell_shape);
                                if(!attendance.equals("Not Taken"))
                                    att.setTextColor(Color.BLACK);
                                else
                                    att.setTextColor(Color.RED);
                                att.setPadding(20, 5, 5, 5);
                                //rn.setHeight(100);
                                att.setTextSize(18);
                                att.setText(attendance);

                                detail_row.addView(att);

                                // get the percentage attendance
                                String percentage = jo.getString("percentage");
                                // put the days inside the row column
                                TextView pct = new TextView(getApplicationContext());
                                pct.setBackgroundResource(R.drawable.cell_shape);
                                pct.setTextColor(Color.BLACK);
                                pct.setPadding(20, 5, 5, 5);

                                pct.setTextSize(18);
                                pct.setText(percentage);

                                detail_row.addView(pct);

                                tableRows.add(detail_row);
                                tableLayout.addView(detail_row);

                                if(i == response.length() - 1)  {
                                    cls.setBackgroundResource(R.drawable.header_cell_shape);
                                    att.setBackgroundResource(R.drawable.header_cell_shape);
                                    pct.setBackgroundResource(R.drawable.header_cell_shape);
                                }

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
                                    "Slow network connection, please try later",
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
                5000,
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
}
