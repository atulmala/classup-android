package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectAttendanceSummaryCriteria extends AppCompatActivity {
    private NumberPicker classPicker;
    private NumberPicker sectionPicker;
    private NumberPicker subjectPicker;
    private NumberPicker monthPicker;

    private CheckBox chk_till_date;
    private CheckBox chk_current_year;
    private CheckBox chk_last_year;

    String year = "";

    String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String server_ip;
    String school_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        String classUrl =  server_ip + "/academics/class_list/" + school_id + "/?format=json";
        String sectionUrl =  server_ip + "/academics/section_list/"+  school_id + "/?format=json";

        String logged_in_user = SessionManager.getInstance().getLogged_in_user();
        // as we are using sihgleton pattern to get the logged in user, sometimes the method
        // call returns a blank string. In this case we will retry for 20 times and if not
        // successful even after then we will ask the user to log in again
        int i = 0;
        while (logged_in_user.equals("")) {
            logged_in_user = SessionManager.getInstance().getLogged_in_user();
            if (i++ == 20)  {
                Toast.makeText(getApplicationContext(),
                        "There seems to be some problem with network. Please re-login",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(intent);
            }
        }
        String subjectUrl2 =  server_ip + "/academics/subject_list/?format=json";
        String subjectUrl =  server_ip + "/teachers/teacher_subject_list/" +
                logged_in_user + "/?format=json";

        setContentView(R.layout.activity_select_attendance_summary_criteria);
        this.setTitle("Select Criteria");

        classPicker = (NumberPicker)findViewById(R.id.pick_class_attendance_summary);
        sectionPicker = (NumberPicker)findViewById(R.id.pick_section_attendance_summary);
        subjectPicker = (NumberPicker)findViewById(R.id.pick_subject_attendance_summary);

        setupPicker(classPicker, classUrl, "standard", "class_api");
        setupPicker(sectionPicker, sectionUrl, "section", "section_api");

        setupPicker(subjectPicker, subjectUrl, "subject", "subject_api");

        monthPicker = (NumberPicker)findViewById(R.id.pick_month_attendance_summary);

        monthPicker.setMaxValue(months.length - 1);
        monthPicker.setDisplayedValues(months);

        chk_till_date = (CheckBox)findViewById(R.id.chk_till_date);
        chk_current_year = (CheckBox)findViewById(R.id.chk_current_year);
        chk_last_year = (CheckBox)findViewById(R.id.chk_last_year);
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Show").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                showAttendanceSummary();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setYear(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.chk_current_year:
                if (checked) {
                    year = "current_year";
                    chk_till_date.setChecked(false);
                    chk_last_year.setChecked(false);
                } else
                    year = "";
                //Toast.makeText(getApplicationContext(), year, Toast.LENGTH_SHORT).show();
                break;

            case R.id.chk_last_year:
                if (checked) {
                    year = "last_year";
                    chk_till_date.setChecked(false);
                    chk_current_year.setChecked(false);
                } else
                    year = "";
                //Toast.makeText(getApplicationContext(), year, Toast.LENGTH_SHORT).show();
                break;

            case R.id.chk_till_date:
                if (checked) {
                    year = "till_date";
                    chk_current_year.setChecked(false);
                    chk_last_year.setChecked(false);
                } else
                    year = "";
                //Toast.makeText(getApplicationContext(), year, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void setupPicker(final NumberPicker picker, String url,
                            final String item_to_extract, final String tag) {
        final ArrayList<String> item_list = new ArrayList<String>();

        System.out.println("Going to fetch " + tag);
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
                        String[] picker_contents = item_list.toArray(new String[item_list.size()]);
                        try {
                            picker.setMaxValue(picker_contents.length - 1);
                            picker.setDisplayedValues(picker_contents);
                        }

                        catch (ArrayIndexOutOfBoundsException e)    {
                            System.out.println("there seems to be no data for " + tag);
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "It looks that you have not yet set subjects. " +
                                            "Please set subjects", Toast.LENGTH_LONG).show();
                            startActivity(new Intent("com.classup.SetSubjects"));
                        }
                        catch (Exception e) {
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

    public void showAttendanceSummary()    {
        // Get the class
        final String[] classList = classPicker.getDisplayedValues();
        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();
        // Get the subject
        final String[] subjectList = subjectPicker.getDisplayedValues();

        if (year.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Please select either one - Current Year, Last Year, or Till Date",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0,0);
            toast.show();
            return;
        }

        Intent intent = new Intent(this, ShowAttendanceSummary.class);
        // Collect the values from pickers
        // get the Date. Due to different handling of date by Java and Python
        // we will be using the raw dates, ie, date, month and year separately

        intent.putExtra("month", months[monthPicker.getValue()]);
        intent.putExtra("year", year);
        intent.putExtra("class", classList[(classPicker.getValue())]);
        intent.putExtra("section", sectionList[(sectionPicker.getValue())]);
        intent.putExtra("subject", subjectList[(subjectPicker.getValue())]);

        startActivity(intent);
    }
}
