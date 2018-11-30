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
import android.widget.NumberPicker;
import android.widget.Toast;

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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectClassSection1 extends AppCompatActivity {
    private NumberPicker classPicker;
    private NumberPicker sectionPicker;

    String server_ip;
    String school_id;

    CheckBox term1;
    CheckBox term2;

    String selected_term;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_class_section1);

        selected_term = "";
        if (getIntent().getStringExtra("sender").equals("co_scholastic")) {
            term1 = (CheckBox) findViewById(R.id.chk_term1);
            term2 = (CheckBox) findViewById(R.id.chk_term2);
            term1.setVisibility(View.VISIBLE);
            term2.setVisibility(View.VISIBLE);

        }

        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        System.out.println("school_id=" + school_id);
        String classUrl = server_ip + "/academics/class_list/" + school_id + "/?format=json";
        String sectionUrl = server_ip + "/academics/section_list/" + school_id + "/?format=json";

        classPicker = (NumberPicker) findViewById(R.id.pick_class1);
        sectionPicker = (NumberPicker) findViewById(R.id.pick_section1);

        setupPicker(classPicker, classUrl, "standard", "class_api");
        setupPicker(sectionPicker, sectionUrl, "section", "section_api");
    }

    public void setTerm(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.chk_term1:
                if (checked) {
                    selected_term = "term1";
                    term2.setChecked(false);
                }
                break;

            case R.id.chk_term2:
                if (checked) {
                    selected_term = "term2";
                    term1.setChecked(false);
                }
                break;
        }
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
                    try {
                        picker.setMaxValue(picker_contents.length - 1);
                        picker.setDisplayedValues(picker_contents);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("there seems to be no data for " + tag);
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                            "It looks that you have not yet set subjects. " +
                                "Please set subjects", Toast.LENGTH_LONG).show();
                        startActivity(new Intent("com.classup.SetSubjects"));
                    } catch (Exception e) {
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
                    } else if (error instanceof ServerError) {
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

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Next").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // Get the class
        final String[] classList = classPicker.getDisplayedValues();
        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();
        switch (id) {
            case 0:
                switch (getIntent().getStringExtra("sender")) {
                    case "school_admin":
                        final Intent intent = new Intent(this, SelectStudent1.class);
                        intent.putExtra("class", classList[(classPicker.getValue())]);
                        intent.putExtra("section", sectionList[(sectionPicker.getValue())]);
                        startActivity(intent);
                        break;
                    case "co_scholastic":
                        if (selected_term.equals("")) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                "Please select a Term", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            break;
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), selected_term,
                                Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            // check whether the teacher is the class teacher of the selected class
                            final String tag = "whether_class_teacher";
                            final ProgressDialog progressDialog = new ProgressDialog(this);
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setCancelable(false);
                            String url = server_ip + "/teachers/whether_class_teacher2/" +
                                SessionManager.getInstance().getLogged_in_user() + "/";
                            progressDialog.show();
                            SessionManager.getInstance().getLogged_in_user();
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.GET, url, null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String whether_class_teacher =
                                                    response.getString("is_class_teacher");
                                                if (whether_class_teacher.equals("true")) {
                                                    String the_class
                                                        = response.getString("the_class");
                                                    String section =
                                                        response.getString("section");
                                                    String selected_class =
                                                        classList[classPicker.getValue()];

                                                    String selected_section =
                                                        sectionList[sectionPicker.getValue()];

                                                    if ((!selected_class.equals(the_class)) ||
                                                        !selected_section.equals(section)) {
                                                        progressDialog.dismiss();
                                                        progressDialog.hide();
                                                        String message = "You are not the";
                                                        message += " Class Teacher of ";
                                                        message += selected_class + "-";
                                                        message += selected_section;
                                                        Toast toast1 = Toast.makeText
                                                            (getApplicationContext(), message,
                                                                Toast.LENGTH_LONG);
                                                        toast1.setGravity(Gravity.CENTER, 0, 0);
                                                        toast1.show();

                                                    } else {
                                                        progressDialog.dismiss();
                                                        progressDialog.hide();
                                                        Intent intent1 = new Intent
                                                            (getApplicationContext(),
                                                                CoScholastic.class);
                                                        intent1.putExtra("teacher",
                                                            SessionManager.getInstance().
                                                                getLogged_in_user());
                                                        intent1.putExtra("the_class",
                                                            selected_class);
                                                        intent1.putExtra("section",
                                                            selected_section);
                                                        intent1.putExtra("term", selected_term);
                                                        startActivity(intent1);
                                                    }

                                                } else {
                                                    progressDialog.dismiss();
                                                    progressDialog.hide();
                                                    Toast toast1 = Toast.makeText
                                                        (getApplicationContext(),
                                                            "You are not a Class Teacher",
                                                            Toast.LENGTH_SHORT);
                                                    toast1.setGravity(Gravity.CENTER, 0, 0);
                                                    toast1.show();

                                                }
                                            } catch (JSONException je) {
                                                System.out.println("Ran into JSON exception " +
                                                    "while dealing with "
                                                    + tag);
                                                je.printStackTrace();
                                            } catch (Exception e) {
                                                System.out.println("Caught General exception " +
                                                    "while dealing with" + tag);
                                                e.printStackTrace();
                                            }
                                            //}
                                            progressDialog.hide();
                                            progressDialog.dismiss();
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
                                                "Slow network connection or No " +
                                                    "internet connectivity",
                                                Toast.LENGTH_LONG).show();
                                        } else if (error instanceof ServerError) {
                                            Toast.makeText(getApplicationContext(),
                                                "Slow network connection or No" +
                                                    " internet connectivity",
                                                Toast.LENGTH_LONG).show();
                                        } else if (error instanceof NetworkError) {
                                            Toast.makeText(getApplicationContext(),
                                                "Slow network connection or No " +
                                                    "internet connectivity",
                                                Toast.LENGTH_LONG).show();
                                        } else if (error instanceof ParseError) {
                                            //TODO
                                        }
                                        // TODO Auto-generated method stub
                                    }
                                });
                            com.classup.AppController.getInstance().
                                addToRequestQueue(jsonObjectRequest, tag);
                        }
                        break;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }
}

