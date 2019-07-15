package com.classup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class ReportBusDelay extends AppCompatActivity {
    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_bus_delay);
        // get the server ip to make api calls
        c = this.getApplicationContext();

        // 08/10/2016 - setting the below property to prevent duplicate post requests
        //System.setProperty("http.keepAlive", "false");
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
        menu.add(0, 0, 0, "Submit").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 1, 0, "Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                reportDelay(getIntent(), c);
                break;
            case 1:
                startActivity(new Intent("com.classup.TeacherMenu").
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    void reportDelay(Intent intent, Context c)  {
        final String server_ip = MiscFunctions.getInstance().getServerIP(c);
        final String school_id = SessionManager.getInstance().getSchool_id();
        final String teacher = SessionManager.getInstance().getLogged_in_user();

        final String d = intent.getStringExtra("date");
        final String m = intent.getStringExtra("month");
        final String y = intent.getStringExtra("year");
        final String rout = intent.getStringExtra("rout");
        EditText editText = findViewById(R.id.txt_bus_delay);
        final String message = editText.getText().toString();
        // check to see if message is empty

        if (message.equals("")) {
            Toast.makeText(getApplicationContext(), "Message is empty!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // create the dialog box for confirmation
        final Dialog dialog = new Dialog(ReportBusDelay.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_bus_delay);

        // show the date of attendance
        TextView txt_date =
                dialog.findViewById(R.id.txt_bus_delay_date);

        String formatted_date = d + "/" + m + "/" + y;
        txt_date.setText(formatted_date);
        txt_date.setTypeface(Typeface.DEFAULT_BOLD);

        // show the rout
        TextView txt_rout =
                dialog.findViewById((R.id.txt_bus_delay_rout));

        txt_rout.setText(rout);
        txt_rout.setTypeface(Typeface.DEFAULT_BOLD);

        // now, show the dialog
        dialog.show();

        Button btn_cancel = dialog.findViewById(R.id.btn_bus_delay_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)  {
                dialog.dismiss();
            }
        });

        Button btn_ok = dialog.findViewById(R.id.btn_bus_delay_confirm);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.equals("")) {
                    Toast.makeText(getApplicationContext(), "Message is empty!",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("school_id", school_id);
                        jsonObject.put("rout", rout);
                        jsonObject.put("teacher", teacher);
                        jsonObject.put("message", message);
                    } catch (JSONException je) {
                        System.out.println("unable to create json for bus delay message");
                        je.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException ae) {
                        System.out.println("array out of bounds exception");
                        ae.printStackTrace();
                    }
                    String url =  server_ip + "/bus_attendance/report_delay/";
                    url = url.replace(" ", "%20");
                    final String tag = "ReportBusDelay";
                    JsonObjectRequest jsonObjReq =
                            new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                                    new Response.Listener<JSONObject>() {

                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.d(tag, response.toString());
                                            // 12/09/17 - Now we are building the custom
                                            // Analysis via AWS
                                            try {
                                                AnalyticsEvent event =
                                                        SessionManager.analytics.
                                                                getEventClient().
                                                                createEvent("Bus Delay");
                                                event.addAttribute("user",
                                                        SessionManager.getInstance().
                                                        getLogged_in_user());

                                                SessionManager.analytics.
                                                        getEventClient().
                                                        recordEvent(event);
                                            } catch (NullPointerException exception)    {
                                                System.out.println("flopped in creating " +
                                                        "analytics Bus Delay");
                                            } catch (Exception exception)   {
                                                System.out.println("flopped in " +
                                                        "creating analytics Bus Delay");
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    VolleyLog.d(tag, "Error: " + error.getMessage());
                                }
                            });
                    int socketTimeout = 300000;//5 minutes
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                            -1,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    jsonObjReq.setRetryPolicy(policy);
                    
                    com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
                    Toast.makeText(getApplicationContext(),
                            "Message(s) sent!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    startActivity(new Intent("com.classup.TeacherMenu"));

                }
            }
        });
    }
}
