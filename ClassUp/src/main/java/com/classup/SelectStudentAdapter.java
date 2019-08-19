package com.classup;

/**
 * Created by atulgupta on 12/01/16.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atulgupta on 07/10/15.
 */
public class SelectStudentAdapter extends BaseAdapter {
    private ArrayList<AttendanceListSource> student_list;
    public ArrayList<String> selected_students = new ArrayList<>();

    String server_ip;
    Context context;
    String sender;

   public List<AttendanceListSource> getStudent_list() {
        return student_list;
    }

    public SelectStudentAdapter(Context context, ArrayList<AttendanceListSource> student_list,
                                ArrayList<String > selected_students, String sender) {
        super();
        this.student_list = student_list;
        this.selected_students = selected_students;
        this.sender = sender;

        this.context = context;
    }


    @Override
    public int getCount()   {
        return  getStudent_list().size();
    }

    @Override
    public Object getItem(int position) {
        return student_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_select_student1, null);
            convertView.setBackgroundColor(Color.WHITE);
        }

        TextView student_name = convertView.findViewById(R.id.lbl_roll_no);
        student_name.setText(student_list.get(position).getFull_name());

        TextView roll_no = convertView.findViewById(R.id.roll_no);
        roll_no.setVisibility(View.INVISIBLE);
        roll_no.setText(student_list.get(position).getRoll_number());

        TextView parent_name = convertView.findViewById(R.id.parent_name);
        parent_name.setText(student_list.get(position).getParent_name());

        final CheckBox chk = convertView.findViewById(R.id.chk_select);
        if (selected_students.contains(student_list.get(position).getId())) {
            chk.setChecked(true);
        }
        else    {
            chk.setChecked(false);
        }

        chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chk.setChecked(!chk.isChecked());
                if (!chk.isChecked()) {
                    selected_students.add(student_list.get(position).getId());
                    System.out.println(selected_students);
                    notifyDataSetChanged();
                } else {
                    selected_students.remove(student_list.get(position).getId());
                    notifyDataSetChanged();
                }
            }
        });

        ImageView call_parent = convertView.findViewById(R.id.icon_call);
        if(sender.equals("share_image"))
            call_parent.setVisibility(View.INVISIBLE);
        call_parent.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder =
                    new android.app.AlertDialog.Builder(context);
                final String student_name = student_list.get(position).getFull_name();
                final String message = "Do you want to call the parent of " + student_name +
                    "? Your number will be displayed on Parent's phone.";
                builder.setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // 12/09/17 - Now we are building the custom Analysis via AWS
                            try {
                                AnalyticsEvent callParentEvent =
                                    SessionManager.
                                        analytics.getEventClient().
                                        createEvent("Call Parent");
                                callParentEvent.addAttribute("user",
                                    SessionManager.getInstance().getLogged_in_user());
                                SessionManager.analytics.getEventClient().
                                    recordEvent(callParentEvent);
                            } catch (NullPointerException exception) {
                                System.out.println("flopped in creating " +
                                    "analytics Call Parent");
                            } catch (Exception exception) {
                                System.out.println("flopped in creating " +
                                    "analytics Call Parent");
                            }

                            final String student_id = student_list.get(position).getId();
                            String server_ip = MiscFunctions.getInstance().
                                getServerIP(context);
                            String url = server_ip + "/student/get_parent/" + student_id + "/";
                            url = url.replace(" ", "%20");
                            //progressDialog.show();
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.GET, url, null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String p_m1 = response.get
                                                    ("parent_mobile1").toString();
                                                System.out.println("mobile=" + p_m1);
                                                Intent intent = new Intent(Intent.ACTION_CALL);
                                                intent.setData(Uri.parse("tel:" + p_m1));
                                                System.out.println("going to make call");
                                                // check to see if dialler permssion exist
                                                int permissionCheck =
                                                    ContextCompat.checkSelfPermission
                                                        (context, android.Manifest
                                                                .permission.CALL_PHONE);
                                                if (permissionCheck == PackageManager.
                                                        PERMISSION_GRANTED)
                                                    context.startActivity(intent);
                                                else
                                                    Toast.makeText(context,
                                                        "Dialling permission not granted",
                                                        Toast.LENGTH_LONG).show();

                                            } catch (JSONException je) {
                                                System.out.println("Ran into " +
                                                    "JSON exception " +
                                                    "while trying to make call");
                                                je.printStackTrace();
                                            } catch (Exception e) {
                                                System.out.println("Caught " +
                                                    "General exception " +
                                                    "while trying make call ");
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //progressDialog.hide();
                                        //progressDialog.dismiss();
                                        if (error instanceof TimeoutError ||
                                            error instanceof NoConnectionError) {
                                            if (!MiscFunctions.getInstance().checkConnection
                                                (context)) {
                                                Toast.makeText(context,
                                                    "Slow network connection or " +
                                                        "No internet connectivity",
                                                    Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(context,
                                                    "Slow network connection or " +
                                                        "No internet connectivity",
                                                    Toast.LENGTH_LONG).show();
                                            }
                                        } else if (error instanceof ServerError) {
                                            Toast.makeText(context,
                                                "Slow network connection or " +
                                                    "No internet connectivity",
                                                Toast.LENGTH_LONG).show();
                                        } else if (error instanceof NetworkError) {

                                        } else if (error instanceof ParseError) {

                                            Toast.makeText(context,
                                                "Error in parsing of number",
                                                Toast.LENGTH_LONG).show();
                                            System.out.println(error);
                                        }
                                    }
                                });
                            // here we can sort the attendance list as per roll number

                            com.classup.AppController.getInstance().
                                addToRequestQueue(jsonObjectRequest, "Get Student Parent");
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                // Create the AlertDialog object and return it
                builder.show();
            }
        });

        return convertView;
    }
}

