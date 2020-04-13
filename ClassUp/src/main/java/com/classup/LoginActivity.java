package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.InitializationException;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;
import com.amazonaws.regions.Regions;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText userName;
    private EditText password;
    final Context context = this;

    String server_ip;

    private void setUpVariables() {
        userName = findViewById(R.id.usernameET);
        password = findViewById(R.id.passwordET);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        OneSignal.startInit(this)
            .setNotificationOpenedHandler(new ExampleNotificationOpenedHandler())
            .setNotificationReceivedHandler(new ExampleNotificationReceivedHandler())
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init();

        // initialize AWS analytics
        try {
            SessionManager.analytics = MobileAnalyticsManager.getOrCreateInstance(
                    this.getApplicationContext(),
                    "175b4dff4d244f67a3b493ca2fbf0904", //Amazon Mobile Analytics App ID
                    "us-east-1:3c5df3cc-591c-44f1-9624-0fb5fe21cee3" //Amazon Cognito Identity Pool ID
            );
        } catch(InitializationException ex) {
            Log.e(this.getClass().getName(),
                "Failed to initialize Amazon Mobile Analytics", ex);
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

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Login").
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0,1, 0, "Forgot Password?").
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                    0, 0);
            toast.show();
            return;
        }
        if (userName.getText().toString().equals("")) {
            String prompt = "Username cannot be blank. ";
            prompt += "For parent, Username is mobile number registered with school. ";
            prompt += "For teachers, Username has been provided by school";
            Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_LONG).show();
            good_to_go = false;
        }
        if (good_to_go) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
                    Builder(context);
            String prompt = "Are you sure you want to Reset password?";
            builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                    OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // 11/09/17 - Now we are building the custom Analysis via AWS
                    try {
                        AnalyticsEvent forgotPasswordEvent = SessionManager.
                                analytics.getEventClient().createEvent("Forgot Password");
                        forgotPasswordEvent.addAttribute("user",
                            userName.getText().toString());
                        SessionManager.analytics.getEventClient().
                                recordEvent(forgotPasswordEvent);
                    } catch (NullPointerException exception)    {
                        System.out.println("flopped in creating analytics");
                    }
                    // 06/01/17 - need to show the below message immediately after pressing
                    // the password change button. Because people press it multiple times
                    String message =
                            "Password change initiated. Please wait for upto 15 min for SMS";
                    Toast toast =
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        OSPermissionSubscriptionState status =
                            OneSignal.getPermissionSubscriptionState();
                        String player_id = status.getSubscriptionStatus().getUserId();

                        jsonObject.put("user", userName.getText().toString());
                        jsonObject.put("player_id", player_id);
                    } catch (JSONException je) {
                        System.out.println("unable to create json object for " +
                                "forgotPassword functionality ");
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

            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                0, 0);
            toast.show();
            return;
        }
        if (userName.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                    "User Name is blank", Toast.LENGTH_SHORT).show();
            good_to_go = false;
        }

        if (password.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Password is blank",
                Toast.LENGTH_SHORT).show();
            good_to_go = false;
        }

        if (good_to_go) {
            try {
                AnalyticsEvent loginAttemptEvent = SessionManager.
                        analytics.getEventClient().createEvent("Login Attempt");
                loginAttemptEvent.addAttribute("user", userName.getText().toString());
                SessionManager.analytics.getEventClient().
                        recordEvent(loginAttemptEvent);
            } catch (NullPointerException exception)    {
                Toast.makeText(this, "Analytics", Toast.LENGTH_SHORT).show();
                System.out.println("flopped in creating analytics");
            }
            // 10/07/2017 - Get the manufacturer, model & OS of the device
            String model = getDeviceName();
            Integer version = Build.VERSION.SDK_INT;
            String versionRelease = Build.VERSION.RELEASE;
            String os = "API Level: " + version.toString() + ", Release: " + versionRelease;

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", userName.getText().toString());
                jsonObject.put("password", password.getText().toString());
                jsonObject.put("device_type", "Android");
                jsonObject.put("model", model);
                jsonObject.put("os", os);
                jsonObject.put("size", getScreenSize());
                jsonObject.put("resolution", getDensity());
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
                                    progressDialog.hide();
                                    progressDialog.dismiss();
                                    try {
                                        String subscription_status =
                                                response.get("subscription").toString();
                                        if(subscription_status.equals("expired"))   {
                                            String message = "Institute/School's subscription ";
                                            message += "has expired. For more information please ";
                                            message += "contact the School Management";
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
                                                FirebaseInstanceId.getInstance().getInstanceId()
                                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                            if (!task.isSuccessful()) {
                                                                Log.w("TAG",
                                                                    "getInstanceId failed",
                                                                    task.getException());
                                                                return;
                                                            }
                                                            OSPermissionSubscriptionState status =
                                                                OneSignal.
                                                                    getPermissionSubscriptionState();
                                                            String player_id =
                                                                status.getSubscriptionStatus().
                                                                    getUserId();

                                                            // Get new Instance ID token
                                                            String token =
                                                                task.getResult().getToken();
                                                            JSONObject jsonObject1 =
                                                                new JSONObject();
                                                            try {
                                                                jsonObject1.put("user",
                                                                    userName.getText().toString());
                                                                jsonObject1.put("device_token",
                                                                    token);
                                                                jsonObject1.put("device_type",
                                                                    "Android");
                                                                jsonObject1.put("player_id",
                                                                    player_id);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

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
                                                        }
                                                    });
                                                String greetings =
                                                        response.getString("welcome_message");
                                                Toast toast1 = Toast.makeText(
                                                        getApplicationContext(), greetings,
                                                        Toast.LENGTH_SHORT);
                                                toast1.setGravity(Gravity.CENTER, 0,
                                                    0);
                                                toast1.show();
                                                try {
                                                    AnalyticsEvent loginResultEvent =
                                                            SessionManager.
                                                            analytics.getEventClient().
                                                                    createEvent(
                                                                            "Login Result");
                                                    loginResultEvent.addAttribute
                                                            ("user",
                                                                    userName.getText().toString());
                                                    loginResultEvent.addAttribute("Login Result",
                                                            "Success");
                                                    SessionManager.analytics.
                                                            getEventClient().
                                                            recordEvent(loginResultEvent);
                                                } catch (NullPointerException exception)    {
                                                    System.out.println
                                                            ("flopped in creating analytics");
                                                }
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
                                            try {
                                                AnalyticsEvent loginResultEvent =
                                                        SessionManager.
                                                                analytics.getEventClient().
                                                                createEvent("Login Result");
                                                loginResultEvent.addAttribute
                                                        ("Login", userName.getText().toString());
                                                loginResultEvent.addAttribute("Login Result",
                                                        "Failed");
                                                SessionManager.analytics.
                                                        getEventClient().
                                                        recordEvent(loginResultEvent);
                                            } catch (NullPointerException exception)    {
                                                System.out.println
                                                        ("flopped in creating analytics");
                                            }
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
                            System.out.println(error.getMessage());
                            if (error instanceof TimeoutError || error instanceof NoConnectionError)
                            {
                                if (!MiscFunctions.getInstance().checkConnection
                                        (getApplicationContext())) {
                                    Toast.makeText(getApplicationContext(),
                                            "Slow network connection or No internet connectivity",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    if (error instanceof TimeoutError)
                                        Toast.makeText(getApplicationContext(),
                                            "Timeout errors", Toast.LENGTH_LONG).show();
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
            int socketTimeout = 300000;//5 minutes
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsObjRequest1.setRetryPolicy(policy);
            com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Login/Password not correct! Please retry.", Toast.LENGTH_SHORT).show();
        }
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        return capitalize(manufacturer) + " " + model;

    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private String getScreenSize () {
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        String screen_size;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                screen_size = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                screen_size = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                screen_size = "Small screen";
                break;
            default:
                screen_size = "Undetermined";
        }

        return screen_size;
    }

    private String getDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;

        return String.valueOf(density);
    }

    private class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject data = result.notification.payload.additionalData;
            String launchUrl = result.notification.payload.launchURL; // update docs launchUrl

            String customKey;
            String openURL = null;

            Object activityToLaunch = DummyActivity.class;

            if (data != null) {
                customKey = data.optString("customkey", null);
                openURL = data.optString("openURL", null);

                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);

                if (openURL != null)
                    Log.i("OneSignalExample", "openURL to webview with URL value: " + openURL);
            }

            if (actionType == OSNotificationAction.ActionType.ActionTaken) {
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

                if (result.action.actionID.equals("id1")) {
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
                    activityToLaunch = LoginActivity.class;
                } else
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
            }
            // The following can be used to open an Activity of your choice.
            // Replace - getApplicationContext() - with any Android Context.
            // Intent intent = new Intent(getApplicationContext(), YourActivity.class);
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activityToLaunch);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//             intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("openURL", openURL);
            Log.i("OneSignalExample", "openURL = " + openURL);
            // startActivity(intent);
            startActivity(intent);

            // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
            //   if you are calling startActivity above.
        /*
           <application ...>
             <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
           </application>
        */
        }


    }

    private class ExampleNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            System.out.println("Inside notificationReceivedHandler");
            JSONObject data = notification.payload.additionalData;
            String notificationID = notification.payload.notificationID;
            String title = notification.payload.title;
            String body = notification.payload.body;
            String smallIcon = notification.payload.smallIcon;
            String largeIcon = notification.payload.largeIcon;
            String bigPicture = notification.payload.bigPicture;
            String smallIconAccentColor = notification.payload.smallIconAccentColor;
            String sound = notification.payload.sound;
            String ledColor = notification.payload.ledColor;
            int lockScreenVisibility = notification.payload.lockScreenVisibility;
            String groupKey = notification.payload.groupKey;
            String groupMessage = notification.payload.groupMessage;
            String fromProjectNumber = notification.payload.fromProjectNumber;
            String rawPayload = notification.payload.rawPayload;


            String customKey;

            Log.i("OneSignalExample", "NotificationID received: " + notificationID);

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }
        }


    }


}