package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowExamResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_exam_results);

        try {
            AnalyticsEvent event =
                    SessionManager.getInstance().analytics.getEventClient().
                            createEvent("Show Exam Result");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.getInstance().analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Show Exam Result");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Show Exam Result");
        }


        //final GraphView graph = (GraphView) findViewById(R.id.exam_result_graph);
        final BarChart barChart = (BarChart) findViewById(R.id.exam_result_graph);
        barChart.animateY(5000);
        final ArrayList<BarEntry> entries = new ArrayList<>();
        final BarDataSet dataset = new BarDataSet(entries, "");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        final ArrayList<String> labels = new ArrayList<String>();


        // show the name of student on the top
        TableRow header_row = (TableRow)findViewById(R.id.header_row_p_exam_result);
        String header_text = getIntent().getStringExtra("exam_title") +
                " Results for " + getIntent().getStringExtra("student_name");
        ((TextView)header_row.findViewById(R.id.txt_p_exam_result)).setText(header_text);

        final TableLayout tableLayout = (TableLayout)findViewById(R.id.tbl_p_exam_result);
        TableRow title_row = new TableRow(getApplicationContext());
        TableRow.LayoutParams lp =
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        title_row.setLayoutParams(lp);

        TextView subject = new TextView(getApplicationContext());
        subject.setText("Subject");
        subject.setTextColor(Color.BLACK);
        subject.setPadding(5, 5, 5, 5);
        //fn.setHeight(20);
        subject.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(subject);

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
        att.setBackgroundResource(R.drawable.header_cell_shape);
        title_row.addView(att);

        TextView comments = new TextView(getApplicationContext());
        comments.setText("Comments");
        comments.setTextColor(Color.BLACK);
        comments.setPadding(5, 5, 5, 5);
        comments.setBackgroundResource(R.drawable.header_cell_shape);

        tableLayout.addView(title_row);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final ArrayList<TableRow> tableRows = new ArrayList<TableRow>();
        final Context context = getApplicationContext();
        final Intent intent = new Intent(this.getApplicationContext(), ShowExamList.class);
        intent.putExtra("student_id", getIntent().getStringExtra("student_id"));
        intent.putExtra("student_name", getIntent().getStringExtra("student_name"));
        String tag = "ExamResults";
        String url =  MiscFunctions.getInstance().getServerIP(getApplicationContext()) +
                "/parents/get_exam_result/" + getIntent().getStringExtra("student_id")
                + "/" + getIntent().getStringExtra("exam_id");
        url = url.replace(" ", "%20");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            progressDialog.hide();
                            progressDialog.dismiss();
                            String message = "Result for " +
                                    getIntent().getStringExtra("exam_title") + " is not ready yet!";
                            Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                                    0, 0);
                            toast.show();
                        }
                        else {
                            ArrayList<String> subject_list = new ArrayList<>();
                            int j = 0;
                            for (int i = 0; i < response.length(); i++)
                                try {
                                    JSONObject jo = response.getJSONObject(i);

                                    // prepare a new row
                                    TableRow detail_row = new TableRow(getApplicationContext());

                                    TableRow.LayoutParams lp =
                                            new TableRow.LayoutParams(TableRow.
                                                    LayoutParams.MATCH_PARENT);
                                    detail_row.setLayoutParams(lp);

                                    // get max marks
                                    String max_marks = jo.getString("max_marks");
                                    TextView mm = new TextView(getApplicationContext());
                                    mm.setBackgroundResource(R.drawable.cell_shape);
                                    mm.setTextColor(Color.BLACK);
                                    mm.setPadding(5, 5, 5, 5);
                                    mm.setTextSize(18);
                                    mm.setText(max_marks);

                                    // get marks obtained
                                    String marks = jo.getString("marks");
                                    if (marks.equals("-1000.0")) {
                                        marks = "ABS";
                                    }

                                    // get the subject
                                    String subject = jo.getString("subject");
                                    if (!max_marks.equals("Grade Based")) {
                                        if (!marks.equals("ABS"))
                                            if (subject.length() > 4) {
                                                String sub = subject.substring(0, 4);
                                                subject_list.add(sub);
                                                labels.add(sub);
                                        }
                                        else    {
                                            subject_list.add(subject);
                                                labels.add(subject);
                                        }
                                    }
                                    System.out.println(subject_list);

                                    // put the subject subject column
                                    TextView sub = new TextView(getApplicationContext());
                                    sub.setSingleLine(false);
                                    sub.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                                    sub.setText(subject);
                                    sub.setTextColor(Color.BLACK);
                                    sub.setPadding(5, 5, 5, 5);
                                    sub.setTextSize(18);
                                    sub.setBackgroundResource(R.drawable.cell_shape);

                                    detail_row.addView(sub);
                                    detail_row.addView(mm);


                                    // put the marks inside the marks obtained column
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

                                    tableRows.add(detail_row);
                                    tableLayout.addView(detail_row);
                                    if (!max_marks.equals("Grade Based")) {
                                        if (!marks.equals("ABS")) {
                                            DataPoint dataPoint = new DataPoint(i + 1,
                                                    (Float.valueOf(marks) /
                                                            Float.valueOf(max_marks)) * 100);
                                            BarEntry barEntry = new BarEntry((Float.valueOf(marks) /
                                                    Float.valueOf(max_marks)) * 100, j++);
                                            entries.add(barEntry);
                                        }
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
                            if (subject_list.size() > 1) {
                                System.out.println("here");
                                try {
                                    BarData data = new BarData(labels, dataset);
                                    barChart.setData(data);

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
                                progressDialog.hide();
                                progressDialog.dismiss();
                            }
                            progressDialog.hide();
                            progressDialog.dismiss();
                        }
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
                                    "Slow network connection or No internet connectivity",
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

