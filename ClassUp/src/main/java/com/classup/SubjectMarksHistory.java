package com.classup;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SubjectMarksHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_marks_history);

        final GraphView graph = (GraphView) findViewById(R.id.graph);
        final LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        // show the name of student on the top
        TableRow header_row = (TableRow)findViewById(R.id.header_row_p_marks_history);
        String header_text = getIntent().getStringExtra("subject") +
                " Marks History for " + getIntent().getStringExtra("student_name");
        ((TextView)header_row.findViewById(R.id.txt_p_subject_marks)).setText(header_text);

        final TableLayout tableLayout = (TableLayout)findViewById(R.id.tbl_p_stu_marks_history);
        TableRow title_row = new TableRow(getApplicationContext());
        TableRow.LayoutParams lp =
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        title_row.setLayoutParams(lp);

        TextView month = new TextView(getApplicationContext());
        month.setText("Date");
        month.setTextColor(Color.BLACK);
        month.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        month.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(month);

        TextView td = new TextView(getApplicationContext());
        td.setText("Max Marks");
        td.setTextColor(Color.BLACK);
        td.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        td.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(td);

        TextView att = new TextView(getApplicationContext());
        att.setText("Marks/Grade");
        att.setTextColor(Color.BLACK);
        att.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        att.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(att);



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
        String tag = "SubjectMarksHistory";
        String url =  MiscFunctions.getInstance().getServerIP(getApplicationContext()) +
                "/parents/retrieve_stu_sub_marks_history/" + getIntent().getStringExtra("subject")
                + "/?student=" + getIntent().getStringExtra("student_id");
        url = url.replace(" ", "%20");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<String> date_list = new ArrayList<>();
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
                                String date = jo.getString("date");
                                String yy = date.substring(0, 4);
                                String month = date.substring(5, 7);
                                String dd = date.substring(8, 10);
                                String ddmmyyyy = dd + "/" + month + "/" + yy;


                                // put the month/year inside row column
                                TextView dt = new TextView(getApplicationContext());

                                dt.setText(ddmmyyyy);
                                dt.setTextColor(Color.BLACK);
                                dt.setPadding(5, 5, 5, 5);
                                //fn.setHeight(20);
                                dt.setTextSize(18);
                                dt.setBackgroundResource(R.drawable.cell_shape);

                                // get the number of working days in the month
                                String max_marks = jo.getString("max_marks");
                                // put the working days inside row column
                                TextView mm = new TextView(getApplicationContext());
                                mm.setBackgroundResource(R.drawable.cell_shape);
                                mm.setTextColor(Color.BLACK);
                                mm.setPadding(5, 5, 5, 5);
                                //rn.setHeight(100);
                                mm.setTextSize(18);
                                mm.setText(max_marks);

                                detail_row.addView(dt);
                                detail_row.addView(mm);

                                // get the marks obtained
                                String marks = jo.getString("marks");
                                if (marks.equals("-1000.0"))
                                    marks = "ABS";
                                // put the days inside the row column
                                TextView mrks = new TextView(getApplicationContext());
                                mrks.setBackgroundResource(R.drawable.cell_shape);
                                mrks.setTextColor(Color.BLACK);
                                mrks.setPadding(5, 5, 5, 5);
                                mrks.setTextSize(18);
                                mrks.setText(marks);

                                detail_row.addView(mrks);



                                TextView cmts = new TextView(getApplicationContext());
                                cmts.setBackgroundResource(R.drawable.cell_shape);
                                cmts.setText("   ");
                                cmts.setPadding(5, 5, 5, 5);
                                cmts.setTextSize(18);
                                //detail_row.addView(cmts);

                                tableRows.add(detail_row);
                                tableLayout.addView(detail_row);

                                // Add to the graph
                                if (!max_marks.equals("Grade Based"))
                                    if (!marks.equals("ABS")) {
                                        date_list.add(dd + "/" + month);
                                        DataPoint dataPoint = new DataPoint(i + 1,
                                                (Float.valueOf(marks) /
                                                        Float.valueOf(max_marks)) * 100);
                                        series.appendData(dataPoint, false, 50);
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
                        // show the graph only if at least two tests have been conducted
                        if (date_list.size() > 1) {
                            try {

                                graph.setPadding(5, 5, 5, 5);
                                graph.getViewport().setYAxisBoundsManual(true);

                                graph.getViewport().setMinY(0);
                                graph.getViewport().setMaxY(100);
                                graph.getViewport().setScrollable(true);
                                graph.setTitle("Progress in %");
                                series.setDrawDataPoints(true);
                                series.setDataPointsRadius(10);
                                String[] dl = new String[date_list.size()];
                                dl = date_list.toArray(dl);
                                StaticLabelsFormatter staticLabelsFormatter =
                                        new StaticLabelsFormatter(graph, dl, null);

                                staticLabelsFormatter.setVerticalLabels
                                        (new String[]{"0%", "20%", "40%", "60%", "80%", "100%"});
                                staticLabelsFormatter.setHorizontalLabels(dl);
                                graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                                graph.addSeries(series);
                                progressDialog.hide();
                                progressDialog.dismiss();
                            } catch (Exception e) {
                                progressDialog.hide();
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Graph will be generated after next test",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        else    {
                            ((ViewManager)graph.getParent()).removeView(graph);
                            progressDialog.hide();
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
                        error.printStackTrace();
                        // TODO Auto-generated method stub
                    }
                });

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }
}