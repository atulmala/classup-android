package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Target;

public class PasswordChange extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);
        Button btn_perform_password_chnge =
                (Button)findViewById(R.id.btn_perform_password_change);
        /*Button btn_cancel_password_change =
                (Button)findViewById(R.id.btn_cancel_password_change);*/
        final EditText ed1  = ((EditText)findViewById((R.id.new_password)));
        final EditText ed2 = (EditText)findViewById(R.id.new_password1);

        btn_perform_password_chnge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean good_to_go = true;
                if (ed1.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter password", Toast.LENGTH_SHORT).show();
                    good_to_go = false;
                }

                if (ed2.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Please re-enter new password", Toast.LENGTH_SHORT).show();
                    good_to_go = false;
                }

                if (ed1.getText().toString().contains(" ")) {
                    Toast.makeText(getApplicationContext(),
                            "Password cannot contain blank. Please enter again",
                            Toast.LENGTH_SHORT).show();
                    good_to_go = false;
                }

                if (!ed1.getText().toString().equals(ed2.getText().toString()))    {
                    Toast.makeText(getApplicationContext(),
                            "Passwords doesn't match. Plese enter again", Toast.LENGTH_SHORT).show();
                    good_to_go = false;
                }

                if(good_to_go)  {
                    final String tag = "PasswodChange";
                    String server_ip = MiscFunctions.getInstance().
                            getServerIP(getApplicationContext());
                    String new_password = ed1.getText().toString();

                    String url =   server_ip + "/auth/change_password/";

                    JSONObject params = new JSONObject();
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
                                    LoginActivity.class).
                                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                    try {
                        params.put("user", logged_in_user);
                        params.put("new_password", new_password);
                    }
                    catch (JSONException je)    {
                        System.out.println("unable to prepare POST parameters for Password change");
                        je.printStackTrace();
                    }
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                            url, params,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.get("password_change").equals("Successful"))
                                            Toast.makeText(getApplicationContext(),
                                                    "Password change successful. " +
                                                            "Re-Login with new password.",
                                                    Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(),
                                                LoginActivity.class).
                                                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                    } catch (JSONException je)  {
                                        System.out.println("unable to change password for " +
                                            SessionManager.getInstance().getLogged_in_user());
                                        je.printStackTrace();
                                    }
                                    Log.d(tag, response.toString());
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("Volley error during password change...");
                            if (error instanceof TimeoutError ||
                                    error instanceof NoConnectionError) {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection",
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
                            error.printStackTrace();
                            VolleyLog.d(tag, "Error: " + error.getMessage());
                        }
                    });
                    com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
                }
            }
        });
    }
}
