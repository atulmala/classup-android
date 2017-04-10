package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atulgupta on 28/02/16.
 */
public class BusAttendanceAdapter extends ArrayAdapter {

    private ArrayList<AttendanceListSource> student_list = new ArrayList<>();
    ArrayList<String> already_absent_students = new ArrayList<>();
    public ArrayList<String> marked_students = new ArrayList<>();

    public ArrayList<String> taken_earlier = new ArrayList<>();

    String server_ip;
    Intent intent;

    Boolean first_time = true;

    public List<AttendanceListSource> getStudent_list() {
        return student_list;
    }

    public BusAttendanceAdapter(Context context, int textViewResourceId,
                                ArrayList<AttendanceListSource> student_list,
                                ArrayList<String> already_absent_students, Intent intent) {
        super(context, textViewResourceId, student_list);
        this.student_list = student_list;
        this.already_absent_students = already_absent_students;
        this.intent = intent;

        final Context c = getContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        String school_id = SessionManager.getInstance().getSchool_id();

        final ProgressDialog progressDialog = new ProgressDialog(c);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        String url = server_ip + "/bus_attendance/attendance_taken_earlier/" + school_id + "/" +
                intent.getStringExtra("rout") + "/" + intent.getStringExtra("date") +
                "/" + intent.getStringExtra("month") + "/"
                + intent.getStringExtra("year") + "/?format=json";
        url = url.replace(" ", "%20");
        JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String te = (response.get("taken_earlier")).
                                            toString();
                                    taken_earlier.add(te);
                                    progressDialog.hide();
                                    progressDialog.dismiss();
                                } catch (org.json.JSONException je) {
                                    je.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error " +
                                "handler(LoginActivity)");
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {

                        } else if (error instanceof ServerError) {
                            Toast.makeText(c,
                                    "Server error, please try later", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(c,
                                    "Network error, please try later", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);
    }

    @Override
    public int getCount() {
        return getStudent_list().size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return student_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearAbsentee_list() {
        student_list.clear();
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();

            switch (student_list.get(position).getEntry_type()) {
                case "bus_stop":
                    convertView = inflater.inflate(R.layout.header_ro, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.textSeparator);
                    break;
                case "student_name":
                    convertView = inflater.inflate(R.layout.select_student_row, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.student_name);
                    break;
            }
            //convertView.setTag(holder);
        }
        else    {
            holder = (ViewHolder) convertView.getTag();
        }
        if (holder.textView != null) {
            switch (student_list.get(position).getEntry_type()) {
                case "bus_stop":
                    holder.textView.setText(student_list.get(position).getBus_stop());
                    break;
                case "student_name":
                    holder.textView.setText(student_list.get(position).getName_rollno());
                    holder.textView1 =
                            (CheckedTextView) convertView.findViewById(R.id.student_name);
                    if (taken_earlier.get(0).equals("true"))
                        if (already_absent_students.contains(student_list.get(position).getId())) {
                            holder.textView.setBackgroundColor(Color.YELLOW);
                            holder.textView1.setChecked(false);
                        } else {
                            holder.textView.setBackgroundColor(Color.WHITE);
                            holder.textView1.setChecked(true);
                        }
                    else {
                        if (marked_students.contains(student_list.get(position).getId())) {
                            holder.textView1.setChecked(true);
                        } else
                            holder.textView1.setChecked(false);
                    }
                    break;
            }
        }
    return  convertView;
    }

    public static class ViewHolder {
        public TextView textView;
        public CheckedTextView textView1;
    }
}