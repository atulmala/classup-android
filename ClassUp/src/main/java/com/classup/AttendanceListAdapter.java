package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by atulgupta on 07/08/15.
 */
public class AttendanceListAdapter extends BaseAdapter  {
    private Activity activity;
    private List<AttendanceListSource> roll_no_and_name_list;

    private static ArrayList<String> absentee_list = new ArrayList<>();

    // a list to hold the student who were absent earlier but now marked as present
    private static List<String> correction_list = new ArrayList<>();

    Intent intent;

    String d, m, y, the_class, section, subject;
    String tag = "AbsenteesList";
    String server_ip;
    String school_id;

    int green = R.color.clover_green;
    int amber = R.color.amber;

    public AttendanceListAdapter(Activity a, List<AttendanceListSource> l, Intent intent) {
        super();
        this.activity = a;
        this.roll_no_and_name_list = l;
        this.intent = intent;
        final Context c = a.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();

        // decompose the date. It will be used to show on summary and call the API
        d = intent.getStringExtra("date");
        m = intent.getStringExtra("month");
        y = intent.getStringExtra("year");

        // get class, section and subject from the intent
        the_class = intent.getStringExtra("class");
        section = intent.getStringExtra("section");
        subject = intent.getStringExtra("subject");

        // retrieve the absentee list for this class, section, subject and date
        // clear the absentee list
        absentee_list.clear();
        String url =  server_ip + "/attendance/retrieve/" + school_id + "/" +
                the_class + "/" + section + "/" + subject +
                "/" + d + "/" + m + "/" + y + "/?format=json";
        url = url.replace(" ", "%20");
        final ProgressDialog progressDialog = new ProgressDialog(activity);
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
                                // get the id of of the absent student and
                                // add it to absentee list
                                absentee_list.add(jo.getString("student"));
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                        "while trying to fetch the list of absentees");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the list of absentees");
                                e.printStackTrace();
                            }
                        }
                        absentee_list = removeDuplicates(absentee_list);
                        System.out.print("first time absentee lists = ");
                        System.out.println(absentee_list);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error handler");
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            if(!MiscFunctions.getInstance().checkConnection(c)) {
                                Toast.makeText(c,
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            } else  {
                                Toast.makeText(c,
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            }
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(activity,
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(activity,
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

        // We also need to get the absentee list in the main attendance
        // (only if this subject is not Main)
        if (!subject.equals("Main"))
            url =  server_ip + "/attendance/retrieve/" + school_id + "/" +
                    the_class + "/" + section + "/Main"  +
                    "/" + d + "/" + m + "/" + y + "/?format=json";
            url = url.replace(" ", "%20");

            JsonArrayRequest jsonArrayRequest1 = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject jo = response.getJSONObject(i);
                                    // get the id of of the absent student and
                                    // add it to absentee list
                                    if (!absentee_list.contains(jo.getString("student")))
                                        absentee_list.add(jo.getString("student"));
                                } catch (JSONException je) {
                                    System.out.println("Ran into JSON exception " +
                                            "while trying to fetch the list of absentees");
                                    je.printStackTrace();
                                } catch (Exception e) {
                                    System.out.println("Caught General exception " +
                                            "while trying to fetch the list of absentees");
                                    e.printStackTrace();
                                }
                            }
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
                                if(!MiscFunctions.getInstance().checkConnection(c)) {
                                    Toast.makeText(c,
                                            "Slow network connection or No internet connectivity",
                                            Toast.LENGTH_LONG).show();
                                } else  {
                                    Toast.makeText(c,
                                            "Slow network connection or No internet connectivity",
                                            Toast.LENGTH_LONG).show();
                                }
                            }  else if (error instanceof ServerError) {
                                /*Toast.makeText(activity, "Server error, please try later",
                                        Toast.LENGTH_LONG).show();*/
                            } else if (error instanceof NetworkError) {

                            } else if (error instanceof ParseError) {
                                //TODO
                            }
                            // TODO Auto-generated method stub
                        }
                    });
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest1, tag);
    }

    public static List<String> getAbsentee_list() {
        return absentee_list;
    }

    public static List<String> getCorrection_list() {return correction_list;}

    public void clearAbsentee_list()    {
        absentee_list.clear();
    }

    public void clearCorrectionList()   {
        correction_list.clear();
    }

    @Override
    public int getCount()  {
        return roll_no_and_name_list.size();
    }

    @Override
    public Object getItem(int position) {
        return roll_no_and_name_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stubhttps://github.com/atulmala/classup-android.git
        //return roll_no_and_name_list[position];
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_attendance_list, null);
        }
        final View conVertViewRef = convertView;
        TextView full_name_col = convertView.findViewById(R.id.full_name);
        TextView roll_no_col = convertView.findViewById(R.id.roll_no);

        // 01/06/2017 We are now showing the parent name also
        TextView parent_name_col = convertView.findViewById(R.id.parent_name);
        final RadioButton radioButton_present = convertView.findViewById(R.id.radio_present);
        final RadioButton radioButton_absent = convertView.findViewById(R.id.radio_absent);
        radioButton_absent.bringToFront();
        radioButton_present.bringToFront();

        final ImageView imageView = convertView.findViewById(R.id.img_absent);
        //imageView.setRotation(10);
        roll_no_col.setText(roll_no_and_name_list.get(position).getRoll_number());
        full_name_col.setText(roll_no_and_name_list.get(position).getFull_name());
        parent_name_col.setText(roll_no_and_name_list.get(position).getParent_name());


        // If this student was absent, the name will be shown in amber background,
        // present radio button enabled, and absent radio button disabled
        if (absentee_list.contains(roll_no_and_name_list.get(position).getId()))  {
            // present radio button should be shown enabled & unchecked
            radioButton_present.setEnabled(true);
            radioButton_present.setChecked(false);
            // absent radio button should be shown disabled & checked
            radioButton_absent.setEnabled(false);
            radioButton_absent.setChecked(true);
            // this row should be shown in amber background
            //conVertViewRef.setBackgroundColor(parent.getResources().getColor(amber));
            imageView.setVisibility(View.VISIBLE);
        }
        else {
            // present radio button should be shown as disabled
            radioButton_present.setEnabled(false);
            // absent radio button should be shown as enabled
            radioButton_absent.setEnabled(true);
            // present radio button should be shown as checked
            radioButton_present.setChecked(true);
            // absent radio button should be shown as unchecked
            radioButton_absent.setChecked(false);
            // this row should be shown in green background
            //conVertViewRef.setBackgroundColor(parent.getResources().getColor(green));
            imageView.setVisibility(View.INVISIBLE);
        }

        // what happens when the teacher clicks the absent radio button
        radioButton_absent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // present radio button will be unchecked
                radioButton_present.setChecked(false);
                // the row should turn amber
                //conVertViewRef.setBackgroundColor(parent.getResources().getColor(amber));
                imageView.setVisibility(View.VISIBLE);
                // absent radio button should be disabled to receive any further clicks
                radioButton_absent.setEnabled(false);
                // present radio button should be enabled
                radioButton_present.setEnabled(true);

                // add the id of this student to the absentees list
                if (!absentee_list.contains(roll_no_and_name_list.get(position).getId()))
                    absentee_list.add(roll_no_and_name_list.get(position).getId());
                System.out.print("Absentee lists before removing duplicates = ");
                System.out.println(absentee_list);
                absentee_list = removeDuplicates(absentee_list);

                // remove the id of this student from the correction list
                correction_list.remove(roll_no_and_name_list.get(position).getId());
                System.out.print("Absentee lists = ");
                System.out.println(absentee_list);
                System.out.print("correction lists = ");
                System.out.println(correction_list);
            }
        });

        // what happens when the teacher clicks the present radio button
        radioButton_present.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // absent radion button will be unchecked
                radioButton_absent.setChecked(false);
                // the row should turn green
                //conVertViewRef.setBackgroundColor(parent.getResources().getColor(green));
                imageView.setVisibility(View.INVISIBLE);
                // present radio button should be disabled to receive any further clicks
                radioButton_present.setEnabled(false);
                // absent radio button should be enabled
                radioButton_absent.setEnabled(true);

                // remove this student from the absentees list and also from database
                absentee_list.remove(roll_no_and_name_list.get(position).getId());


                // add this student to correction list. Means if this student was marked as
                // absent earlier, he/she will now be marked as present
                if (!correction_list.contains(roll_no_and_name_list.get(position).getId())) {
                    correction_list.add(roll_no_and_name_list.get(position).getId());
                }

                System.out.print("Absentee lists = ");
                System.out.println(absentee_list);
                System.out.print("correction lists = ");
                System.out.println(correction_list);
            }
        });
        return convertView;
    }

    // Function to remove duplicates from an ArrayList
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();

        // Add the elements to set
        set.addAll(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }


}