package com.classup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class ParentsMenu extends AppCompatActivity {

    private String student_id;
    private String student_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_menu);
        student_id = getIntent().getStringExtra("student_id");
        student_name = getIntent().getStringExtra("student_name");

        TextView textView = findViewById(R.id.txt_parent_menu_Heading);
        textView.setText(student_name);
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

    public void take_action(final View view)  {
        String server_ip = MiscFunctions.getInstance().getServerIP(getApplicationContext());
        String url1 = server_ip + "/auth/check_subscription/" + student_id + "/";
        JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                (Request.Method.GET, url1, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String subscription_status =
                                            response.get("subscription").toString();
                                    if (subscription_status.equals("expired")) {
                                        String message = response.get("error_message").toString();
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                message, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                        return;
                                    } else {
                                        switch (view.getId())   {
                                            case R.id.btn_parent_Attendance:
                                                Intent intent = new Intent(getApplicationContext(),
                                                        ShowAttendanceSummaryParents.class);
                                                intent.putExtra("student_id", student_id);
                                                intent.putExtra("student_name", student_name);
                                                startActivity(intent);
                                                break;
                                            case R.id.btn_time_table1:
                                                intent = new Intent(getApplicationContext(),
                                                        DaysofWeek.class);
                                                intent.putExtra("student_id", student_id);
                                                intent.putExtra("coming_from",
                                                    "student");
                                                startActivity(intent);
                                                break;
                                            case R.id.btn_term_test_results:
                                                intent = new Intent(getApplicationContext(),
                                                        ShowExamList.class);
                                                intent.putExtra("student_id", student_id);
                                                intent.putExtra("student_name", student_name);
                                                startActivity(intent);
                                                break;
                                            case R.id.btn_subject_wise_marks:
                                                intent = new Intent(getApplicationContext(),
                                                        ParentsSelectSubject.class);
                                                intent.putExtra("student_id", student_id);
                                                intent.putExtra("student_name", student_name);
                                                startActivity(intent);
                                                break;
                                            case R.id.btn_communicate_with_school:
                                                intent = new Intent(getApplicationContext(),
                                                        ParentCommunication.class);
                                                intent.putExtra("student_id", student_id);
                                                intent.putExtra("student_name", student_name);
                                                startActivity(intent);
                                                break;
                                            case R.id.btn_communicate_history:
                                                intent = new Intent(getApplicationContext(),
                                                        CommunicationHistory.class);
                                                intent.putExtra("student_id", student_id);

                                                // 14/03/2018 - we will be using the same screen to
                                                // show the communication history for teachers
                                                intent.putExtra("coming_from",
                                                    "parent");

                                                startActivity(intent);
                                                break;
                                            case R.id.bth_hw_list_parent2:
                                                intent = new Intent(getApplicationContext(),
                                                        HWList.class);
                                                intent.putExtra("sender", "ParentApp");
                                                intent.putExtra("student_id", student_id);
                                                intent.putExtra("student_name", student_name);
                                                startActivity(intent);
                                                break;
                                            case R.id.btn_pending_test_list_parent:
                                                intent = new Intent(getApplicationContext(),
                                                        TestListParent.class);
                                                intent.putExtra("sender", "ParentApp");
                                                intent.putExtra("student_id", student_id);
                                                intent.putExtra("student_name", student_name);
                                                startActivity(intent);
                                                break;
                                        }
                                    }
                                } catch (org.json.JSONException je) {
                                    je.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            if (!MiscFunctions.getInstance().checkConnection
                                    (getApplicationContext())) {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Some problem at server end, please try after " +
                                                "some time",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Some problem with server! Please retry. If still doesn't " +
                                            "work, please reinstall the app from Play Store",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),
                                    "Network error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        System.out.println("inside volley error handler(LoginActivity)");
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);
    }

    public void p_logout(View view) {
        Intent intent = new Intent(this, LoginActivity.class).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void p_changePassword(View view) {
        Intent intent = new Intent(this, PasswordChange.class);
        startActivity(intent);
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Logout").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case 0:
                SessionManager.getInstance().logout();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
