package com.classup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.TimeoutError;
import com.android.volley.NoConnectionError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText userName;
    private EditText password;
    private Button login;

    String server_ip;

    private void setUpVariables() {
        userName = (EditText) findViewById(R.id.usernameET);
        password = (EditText) findViewById(R.id.passwordET);
        login = (Button) findViewById(R.id.loginBtn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);
        setUpVariables();
        Context c = this.getApplicationContext();

        userName.setInputType
                (InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);
    }

    public void forgotPassword(View view)  {
        Boolean good_to_go = true;
        // check for internet connection
        boolean isConnected = MiscFunctions.getInstance().checkConnection(getApplicationContext());
        if (!isConnected) {
            Context context = getApplicationContext();
            String text = "Looks you are not connected to internet. Please connect and try again";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);

            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            return;
        }
        if (userName.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                    "User Name cannot be blank", Toast.LENGTH_SHORT).show();
            good_to_go = false;
        }

        if (good_to_go) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", userName.getText().toString());
            } catch (JSONException je) {
                System.out.println("unable to jsonobject for forgotPassword functionality ");
                je.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException ae) {
                System.out.println("array out of bounds exception");
                ae.printStackTrace();
            }
            server_ip = MiscFunctions.getInstance().getServerIP(getApplicationContext());
            String url1 =  server_ip + "/auth/forgot_password/";
            JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                    (Request.Method.POST, url1, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String result = (response.get("forgot_password")).
                                                toString();
                                        if(result.equals("successful")) {
                                                String message = "Password reset successful. " +
                                                        "You will soon receive " +
                                                        "new password via SMS";
                                                Toast.makeText(getApplicationContext(), message,
                                                        Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(),
                                                    "User does not exist. Please contact " +
                                                            "ClassUp Support at info@classup.in",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (org.json.JSONException je) {
                                        je.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof TimeoutError ||
                                    error instanceof NoConnectionError) {
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
                                        "User does not exist. Please contact " +
                                                "ClassUp Support at info@classup.in",
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

    public void authenticateLogin(View view) {
        Boolean good_to_go = true;
        // check for internet connection
        boolean isConnected = MiscFunctions.getInstance().checkConnection(getApplicationContext());
        if (!isConnected) {
            Context context = getApplicationContext();
            String text = "Looks you are not connected to internet. Please connect and try again";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);

            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            return;
        }
        if (userName.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                    "User Name is blank", Toast.LENGTH_SHORT).show();
            good_to_go = false;
        }

        if (password.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Password is blank", Toast.LENGTH_SHORT).show();
            good_to_go = false;
        }

        if (good_to_go) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", userName.getText().toString());
                jsonObject.put("password", password.getText().toString());
            } catch (JSONException je) {
                System.out.println("unable to create login ");
                je.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException ae) {
                System.out.println("array out of bounds exception");
                ae.printStackTrace();
            }
            server_ip = MiscFunctions.getInstance().getServerIP(getApplicationContext());
            String url1 =  server_ip + "/auth/login1/";
            JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                    (Request.Method.POST, url1, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String subscription_status =
                                                response.get("subscription").toString();
                                        if(subscription_status.equals("expired"))   {
                                            String message = "Institute/School's subscription ";
                                            message += "has expired. For more information please ";
                                            message += "contact your seniors";
                                            Toast toast = Toast.makeText(getApplicationContext(),
                                                    message, Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();
                                            return;
                                        }
                                        String loginResult = (response.get("login")).toString();
                                        if(loginResult.equals("successful")) {
                                            String userStatus =
                                                    (response.get("user_status")).toString();
                                            if (userStatus.equals("active")) {
                                                String user_name =
                                                        (response.get("user_name").toString());

                                                // set the logged in user
                                                SessionManager.getInstance().setLogged_in_user
                                                        (userName.getText().toString());

                                                String greetings = "Hello, " + user_name;
                                                Toast.makeText(getApplicationContext(), greetings,
                                                        Toast.LENGTH_SHORT).show();
                                                // present the options menu
                                                String is_staff =
                                                        (response.get("is_staff")).toString();
                                                if (is_staff.equals("true")) {
                                                    String school_id =
                                                            response.get("school_id").toString();
                                                    SessionManager.getInstance().
                                                            setSchool_id(school_id);
                                                    startActivity(new Intent
                                                            ("com.classup.TeacherMenu"));
                                                    finish();
                                                } else
                                                    startActivity(new Intent
                                                            ("com.classup.ShowWard"));
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "Login disabled! Please contact your Admin",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(),
                                                    "Login/Password not correct! Please retry.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (org.json.JSONException je) {
                                        je.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof TimeoutError || error instanceof NoConnectionError)
                            {
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
        } else {
            Toast.makeText(getApplicationContext(),
                    "Login/Password not correct! Please retry.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_acitivity, menu);
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
        return super.onOptionsItemSelected(item);
    }
}