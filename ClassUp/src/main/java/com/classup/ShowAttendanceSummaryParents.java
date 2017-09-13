package com.classup;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowAttendanceSummaryParents extends AppCompatActivity {
    String tag = "ShowAttendanceSummaryParent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attendance_summary_parents);

        try {
            AnalyticsEvent event =
                    SessionManager.getInstance().analytics.getEventClient().
                            createEvent("Attendance Summary Parents");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.getInstance().analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Attendance Summary Parents");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Attendance Summary Parents");
        }


        // show the name of student on the top
        TableRow header_row = (TableRow)findViewById(R.id.header_row_p_att_summary);
        String header_text = "Attendance Summary for " + getIntent().getStringExtra("student_name");
        ((TextView)header_row.findViewById(R.id.txt_p_att_summary_stu_name)).setText(header_text);

        final TableLayout tableLayout = (TableLayout)findViewById(R.id.tbl_p_stu_att_summary);
        TableRow title_row = new TableRow(getApplicationContext());
        TableRow.LayoutParams lp =
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        title_row.setLayoutParams(lp);

        TextView month = new TextView(getApplicationContext());
        month.setText("Month");
        month.setTextColor(Color.BLACK);
        month.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        month.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(month);

        TextView td = new TextView(getApplicationContext());
        td.setText("Total Days");
        td.setTextColor(Color.BLACK);
        td.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        td.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(td);

        TextView att = new TextView(getApplicationContext());
        att.setText("Present Days");
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

        TextView comments = new TextView(getApplicationContext());
        comments.setText("Comments");
        comments.setTextColor(Color.BLACK);
        comments.setPadding(5, 5, 5, 5);
        comments.setBackgroundResource(R.drawable.cell_shape);
        //title_row.addView(comments);

        tableLayout.addView(title_row);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final ArrayList<TableRow> tableRows = new ArrayList<TableRow>();
        String tag = "AttendanceSummaryStudent";
        String url =  MiscFunctions.getInstance().getServerIP(getApplicationContext()) +
                "/parents/retrieve_stu_att_summary/?student_id=" +
                getIntent().getStringExtra("student_id");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int tot_working_days = 0;
                        int tot_present_days = 0;
                        for (int i = 0; i < response.length(); i++)
                            try {
                                JSONObject jo = response.getJSONObject(i);

                                // prepare a new row
                                TableRow detail_row = new TableRow(getApplicationContext());

                                TableRow.LayoutParams lp =
                                        new TableRow.LayoutParams(TableRow.
                                                LayoutParams.MATCH_PARENT);
                                detail_row.setLayoutParams(lp);

                                // get the month/year
                                String month_year = jo.getString("month_year");

                                // put the month/year inside row column
                                TextView my = new TextView(getApplicationContext());
                                my.setText(month_year);
                                my.setTextColor(Color.BLACK);
                                my.setPadding(5, 5, 5, 5);
                                //fn.setHeight(20);
                                my.setTextSize(18);
                                my.setBackgroundResource(R.drawable.cell_shape);

                                // get the number of working days in the month
                                String work_days = jo.getString("work_days");
                                tot_working_days += Integer.parseInt(work_days);
                                // put the working days inside row column
                                TextView wd = new TextView(getApplicationContext());
                                wd.setBackgroundResource(R.drawable.cell_shape);
                                wd.setTextColor(Color.BLACK);
                                wd.setPadding(5, 5, 5, 5);
                                //rn.setHeight(100);
                                wd.setTextSize(18);
                                wd.setText(work_days);

                                detail_row.addView(my);
                                detail_row.addView(wd);

                                // get the no of days present
                                String present_days = jo.getString("present_days");
                                tot_present_days += Integer.parseInt(present_days);
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

                                TextView cmts = new TextView(getApplicationContext());
                                cmts.setBackgroundResource(R.drawable.cell_shape);
                                cmts.setText("   ");
                                cmts.setPadding(5, 5, 5, 5);
                                cmts.setTextSize(18);
                                //detail_row.addView(cmts);

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
                        TableRow final_row = new TableRow(getApplicationContext());

                        TextView overall_md = new TextView(getApplicationContext());
                        overall_md.setText("Overall");
                        overall_md.setBackgroundResource(R.drawable.header_cell_shape);
                        overall_md.setTextColor(Color.BLACK);
                        overall_md.setPadding(5, 5, 5, 5);
                        //overall_md.setTextSize(20);
                        final_row.addView(overall_md);

                        TextView overall_tot_days = new TextView(getApplicationContext());
                        overall_tot_days.setText(Integer.toString(tot_working_days));
                        overall_tot_days.setBackgroundResource(R.drawable.header_cell_shape);
                        overall_tot_days.setTextColor(Color.BLACK);
                        overall_tot_days.setPadding(5, 5, 5, 5);
                        //overall_tot_days.setTextSize(20);
                        final_row.addView(overall_tot_days);

                        TextView overall_present_days = new TextView((getApplicationContext()));
                        overall_present_days.setText(Integer.toString(tot_present_days));
                        overall_present_days.setBackgroundResource(R.drawable.header_cell_shape);
                        overall_present_days.setTextColor(Color.BLACK);
                        overall_present_days.setPadding(5, 5, 5, 5);
                        //overall_present_days.setTextSize(20);
                        final_row.addView(overall_present_days);

                        TextView overall_percentage = new TextView(getApplicationContext());
                        float overall_perc = 0;
                        if (tot_working_days > 0) {
                            overall_perc =
                                    Math.round(((float) tot_present_days /
                                            (float) tot_working_days) * 100);
                            overall_percentage.setText(String.valueOf(overall_perc) + "%");
                        }   else    {
                            overall_percentage.setText("N/A");
                        }
                        overall_percentage.setBackgroundResource(R.drawable.header_cell_shape);
                        overall_percentage.setTextColor(Color.BLACK);
                        overall_percentage.setPadding(5, 5, 5, 5);
                        //overall_percentage.setTextSize(20);
                        final_row.addView(overall_percentage);

                        TextView overall_cmts = new TextView(getApplicationContext());
                        overall_cmts.setText("  ");
                        overall_cmts.setBackgroundResource(R.drawable.cell_shape);
                        overall_cmts.setPadding(5, 5, 5, 5);
                        //overall_cmts.setTextSize(20);

                        //final_row.addView(overall_cmts);
                        final_row.setBackgroundColor(Color.YELLOW);
                        tableLayout.addView(final_row);
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
}
