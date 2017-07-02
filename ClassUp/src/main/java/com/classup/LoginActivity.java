package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText userName;
    private EditText password;
    final Context context = this;

    String server_ip;

    private void setUpVariables() {
        userName = (EditText) findViewById(R.id.usernameET);
        password = (EditText) findViewById(R.id.passwordET);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //29/06/2017 - initialize AWS cognito
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context, // get the context for the current activity
                "263985579392 ", // your AWS Account id
                "us-east-1:3c5df3cc-591c-44f1-9624-0fb5fe21cee3", // your identity pool id
                "arn:aws:iam::263985579392:role/Cognito_classupUnauth_Role",// an authenticated role ARN
                "arn:aws:iam::XXXXXXXXXX:role/YourRoleName", // an unauthenticated role ARN
                Regions.US_EAST_1 //Region
        );

        // initialize AWS analytics
        try {
            SessionManager.getInstance().analytics = MobileAnalyticsManager.getOrCreateInstance(
                    this.getApplicationContext(),
                    "175b4dff4d244f67a3b493ca2fbf0904", //Amazon Mobile Analytics App ID
                    "us-east-1:3c5df3cc-591c-44f1-9624-0fb5fe21cee3" //Amazon Cognito Identity Pool ID
            );
        } catch(InitializationException ex) {
            Log.e(this.getClass().getName(), "Failed to initialize Amazon Mobile Analytics", ex);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

        setUpVariables();

        userName.setInputType
                (InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Login").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0,1, 0, "Forgot Password?").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                authenticateLogin();
                break;
            case 1:
                forgotPassword();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void forgotPassword() {
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
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
                    Builder(context);
            String prompt = "Are you sure you want to Reset password?";
            builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                    OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // 06/01/17 - need to show the below message immediately after pressing
                    // the password change button. Because people press it multiple times
                    String message = "Password change initiated. Please wait for upto 15 min for SMS";
                    Toast toast =
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

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
                    String url1 = server_ip + "/auth/forgot_password/";
                    JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                            (Request.Method.POST, url1, jsonObject,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String result = (response.get("forgot_password")).
                                                        toString();
                                                if (result.equals("successful")) {
                                                    String message = "Password reset successful. " +
                                                            "You will soon receive " +
                                                            "new password via SMS";
                                                    Toast.makeText(getApplicationContext(), message,
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(),
                                                            "User does not exist. Please contact " +
                                                                    "ClassUp Support " +
                                                                    "at support@classup.in",
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
                                                    "Slow network connection " +
                                                            "or No internet connectivity",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "Some problem at server end, " +
                                                            "please try after some time",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } else if (error instanceof ServerError) {
                                        Toast.makeText(getApplicationContext(),
                                                "User does not exist. Please contact " +
                                                        "ClassUp Support at support@classup.in",
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
                    int socketTimeout = 300000;//5 minutes
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                            -1,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    jsObjRequest1.setRetryPolicy(policy);
                    com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);
                }
            }).setNegativeButton(R.string.cancel,  new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            builder.show();
        }
    }

    public void authenticateLogin() {
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
            final JSONObject jsonObject = new JSONObject();
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
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            server_ip = MiscFunctions.getInstance().getServerIP(getApplicationContext());
            String url1 =  server_ip + "/auth/login1/";
            JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                    (Request.Method.POST, url1, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    progressDialog.dismiss();
                                    progressDialog.dismiss();
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

                                                // 10/03/17 - send the registration token to server
                                                String refreshedToken =
                                                        FirebaseInstanceId.getInstance().getToken();
                                                Toast toast = Toast.makeText(context,
                                                        refreshedToken, Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                //toast.show();
                                                JSONObject jsonObject1 = new JSONObject();
                                                jsonObject1.put("user",
                                                        userName.getText().toString());
                                                jsonObject1.put("device_token", refreshedToken);
                                                jsonObject1.put("device_type", "Android");

                                                String url2 = server_ip + "/auth/map_device_token/";
                                                JsonObjectRequest jsonObjReq =
                                                        new JsonObjectRequest(Request.Method.POST,
                                                                url2, jsonObject1,
                                                                new Response.Listener<JSONObject>() {

                                                                    @Override
                                                                    public void onResponse(JSONObject response)
                                                                    {
                                                                        Log.d("map_device_token",
                                                                                response.
                                                                                        toString());
                                                                    }
                                                                }, new Response.ErrorListener() {

                                                            @Override
                                                            public void onErrorResponse(VolleyError error) {
                                                                VolleyLog.d("map_device_token",
                                                                        "Error: "
                                                                        + error.getMessage());
                                                            }
                                                        });

                                                com.classup.AppController.getInstance().
                                                        addToRequestQueue(jsonObjReq,
                                                                "map_device_token");

                                                String greetings = "Hello, " + user_name;
                                                Toast toast1 = Toast.makeText(
                                                        getApplicationContext(), greetings,
                                                        Toast.LENGTH_SHORT);
                                                toast1.setGravity(Gravity.CENTER, 0, 0);
                                                toast1.show();
                                                // present the options menu
                                                String is_school_admin =
                                                        response.get("school_admin").toString();

                                                String is_staff =
                                                        (response.get("is_staff")).toString();
                                                if (is_staff.equals("true")) {
                                                    String school_id =
                                                            response.get("school_id").toString();
                                                    SessionManager.getInstance().
                                                            setSchool_id(school_id);
                                                    if (is_school_admin.equals("true")) {
                                                        Toast.makeText(getApplicationContext(),
                                                                "School Admin",
                                                                Toast.LENGTH_SHORT).show();

                                                        startActivity(new Intent
                                                                ("com.classup.SchoolAdmin"));
                                                        finish();
                                                    }
                                                    else
                                                        startActivity(new Intent
                                                            ("com.classup.TeacherMenu"));
                                                    finish();
                                                }
                                                else
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
                            progressDialog.hide();
                            progressDialog.dismiss();
                            System.out.println(error);
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
}