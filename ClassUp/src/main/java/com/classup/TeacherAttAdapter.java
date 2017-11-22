package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
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

/**
 * Created by atulgupta on 07/11/17.
 */

public class TeacherAttAdapter extends ArrayAdapter {
    public ArrayList<TeacherListSource> teacher_list = new ArrayList<>();
    public ArrayList<String> absent_teachers = new ArrayList<>();
    public Activity activity;
    Intent intent;
    String server_ip;
    String school_id;
    String d, m, y;

    TeacherAttAdapter(Activity activity, int textViewResourceId, Intent intent)   {
        super(activity, textViewResourceId);
        this.intent = intent;

        final Context c = activity.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();

        // decompose the date. It will be used to show on summary and call the API
        d = intent.getStringExtra("date");
        m = intent.getStringExtra("month");
        y = intent.getStringExtra("year");

        absent_teachers.clear();
        String url =  server_ip + "/teachers/retrieve_attendance/" + school_id +
            "/" + d + "/" + m + "/" + y + "/?format=json";
        url = url.replace(" ", "%20");
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String tag = "Teacher Attendance";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);
                            // get the id of of the absent teacher and
                            // add it to absentee list
                            absent_teachers.add(jo.getString("teacher"));
                        } catch (JSONException je) {
                            System.out.println("Ran into JSON exception " +
                                "while trying to fetch the list of absent teachers");
                            je.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Caught General exception " +
                                "while trying to fetch the list of absent teachers");
                            e.printStackTrace();
                        }
                    }
                    System.out.println ("adapter initialization absentee teachers list = ");
                    System.out.println (absent_teachers);
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
                        Toast.makeText(c,
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(c,
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
    public int getCount()   {
        return  teacher_list.size();
    }

    @Override
    public Object getItem(int position) {
        return teacher_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_teacher_att, null);
        }
        final CheckedTextView textView = convertView.findViewById(R.id.txt_teacher_name);

        textView.setText(teacher_list.get(position).getFull_name());
        if (absent_teachers.contains(teacher_list.get(position).getId()))
            textView.setChecked(false);
        else
            textView.setChecked(true);
        return convertView;
    }
}
