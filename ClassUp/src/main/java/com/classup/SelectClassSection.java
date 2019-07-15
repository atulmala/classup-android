package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.NumberPicker;
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

public class SelectClassSection extends AppCompatActivity {
    private NumberPicker classPicker;
    private NumberPicker sectionPicker;

    String server_ip;
    String school_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_class_section);

        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        System.out.println("school_id=" + school_id);
        String classUrl =  server_ip + "/academics/class_list/" + school_id + "/?format=json";
        String sectionUrl =  server_ip + "/academics/section_list/" + school_id + "/?format=json";

        classPicker = findViewById(R.id.pick_class);
        sectionPicker = findViewById(R.id.pick_section);

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

    public void composeMessage(View view)   {
        Intent intent = new Intent(this, SelectStudent.class);
        // Get the class
        final String[] classList = classPicker.getDisplayedValues();
        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();

        // 26/11/2017 - now we will be reaching the compose message screen from Activity Group
        // communication also. Hence, the compose message screen should know its trigger
        intent.putExtra("coming_from", "TeacherCommunication");
        intent.putExtra("class", classList[(classPicker.getValue())]);
        intent.putExtra("section", sectionList[(sectionPicker.getValue())]);
        intent.putExtra("whole_class", "false");
        startActivity(intent);
    }

    public void composeMessageForWholeClass(View view)   {
        // 12/09/17 - Now we are building the custom Analysis via AWS
        try {
            AnalyticsEvent sendMessageEvent =
                    SessionManager.
                            analytics.getEventClient().
                            createEvent("Send Message Whole Class");
            sendMessageEvent.addAttribute("user",
                    SessionManager.getInstance().getLogged_in_user());
            SessionManager.analytics.getEventClient().
                    recordEvent(sendMessageEvent);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating " +
                    "analytics Call Parent");
        } catch (Exception exception)   {
            System.out.println("flopped in creating " +
                    "analytics Call Parent");
        }

        Intent intent = new Intent(this, ComposeMessage.class);
        // Get the class
        final String[] classList = classPicker.getDisplayedValues();
        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();
        // 26/11/2017 - now we will be reaching the compose message screen from Activity Group
        // communication also. Hence, the compose message screen should know its trigger
        intent.putExtra("coming_from", "TeacherCommunication");
        intent.putExtra("class", classList[(classPicker.getValue())]);
        intent.putExtra("section", sectionList[(sectionPicker.getValue())]);
        intent.putExtra("whole_class", "true");
        startActivity(intent);
    }

}
