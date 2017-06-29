package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
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

public class SelectCriteriaBusAttendance extends AppCompatActivity {

    private NumberPicker routPicker;
    private DatePicker datePicker;
    private String rout_type = "";
    private CheckBox chk_to_school;
    private CheckBox chk_from_school;

    String server_ip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_criteria_bus_attendance);

        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        String school_id = SessionManager.getInstance().getSchool_id();

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

        String url =  server_ip + "/bus_attendance/retrieve_bus_routs/"
                + school_id + "/?format=json";
        routPicker = (NumberPicker)findViewById(R.id.pick_bus_root);
        datePicker = (DatePicker)findViewById(R.id.pick_date_bus_attendance);
        chk_to_school = (CheckBox)findViewById(R.id.chk_to_school);
        chk_from_school = (CheckBox)findViewById(R.id.chk_from_school);
        setupPicker(routPicker, url, "bus_root", "rerieve_bus_routs");
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

    public void reportDelay()  {
        final String[] rout_list = routPicker.getDisplayedValues();
        final Integer d = datePicker.getDayOfMonth();
        final Integer m = datePicker.getMonth() + 1;  // because index start from 0
        final Integer y = datePicker.getYear();

        Intent intent = new Intent(this, ReportBusDelay.class);
        intent.putExtra("date", d.toString());
        intent.putExtra("month", m.toString());
        intent.putExtra("year", y.toString());
        intent.putExtra("rout", rout_list[(routPicker.getValue())]);
        startActivity(intent);
    }

    public void takeBusAttendance()    {
        if (rout_type.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Please select either one - To School or From School",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0,0);
            toast.show();
            return;
        }
        final Integer d = datePicker.getDayOfMonth();
        final Integer m = datePicker.getMonth() + 1;  // because index start from 0
        final Integer y = datePicker.getYear();
        // Get the class
        final String[] rout_list = routPicker.getDisplayedValues();

        Intent intent = new Intent(this, TakeBusAttendance.class);
        intent.putExtra("date", d.toString());
        intent.putExtra("month", m.toString());
        intent.putExtra("year", y.toString());
        intent.putExtra("rout", rout_list[(routPicker.getValue())]);
        intent.putExtra("rout_type", rout_type);
        startActivity(intent);
    }

    public void setRoute(View view)  {
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId())   {
            case R.id.chk_to_school:
                if(checked) {
                    rout_type = "to_school";
                    chk_from_school.setChecked(false);
                }
                else rout_type = "";
                break;
            case R.id.chk_from_school:
                if(checked) {
                    rout_type = "from_school";
                    chk_to_school.setChecked(false);
                }
                else rout_type = "";
                break;
        }
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
                        //picker_contents =
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_criteria_bus_attendance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.take_bus_attendance:
                takeBusAttendance();
                break;
            case R.id.report_bus_delay:
                reportDelay();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
