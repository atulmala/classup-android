package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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

public class TestExams extends AppCompatActivity {
    private String student_id;
    private String student_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_exams);
        student_id = getIntent().getStringExtra("student_id");
        student_name = getIntent().getStringExtra("student_name");
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
                                    case R.id.btn_term_test_results:
                                        Intent intent = new Intent(getApplicationContext(),
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
                                    case R.id.btn_pending_test_list_parent:
                                        intent = new Intent(getApplicationContext(),
                                            TestListParent.class);
                                        intent.putExtra("sender", "ParentApp");
                                        intent.putExtra("student_id", student_id);
                                        intent.putExtra("student_name", student_name);
                                        startActivity(intent);
                                        break;
                                    case R.id.btn_online_tests:
                                        intent = new Intent(getApplicationContext(),
                                            OnlineTestList.class);
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
}

