package com.classup;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;



public class SelectClass extends AppCompatActivity {
    static final int REQUEST_TAKE_PHOTO = 1;

    // Various pickers to be shown on the screen
    private NumberPicker classPicker;
    private NumberPicker sectionPicker;
    private NumberPicker subjectPicker;
    private DatePicker  datePicker;

    String mCurrentPhotoPath;

    MenuItem take_pic;

    String server_ip;
    String school_id;

    String sender;
    String menu = "Go";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the server ip to make api calls
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        System.out.println("school_id=" + school_id);
        String classUrl =  server_ip + "/academics/class_list/" +
                school_id + "/?format=json";
        String sectionUrl =  server_ip + "/academics/section_list/" +
                school_id + "/?format=json";

        String logged_in_user = SessionManager.getInstance().getLogged_in_user();
        // as we are using singleton pattern to get the logged in user, sometimes the method
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
                        LoginActivity.class);
                startActivity(intent);
            }
        }
        String subjectUrl =  server_ip + "/teachers/teacher_subject_list/" +
                logged_in_user + "/?format=json";

        setContentView(R.layout.activity_select_class1);
        classPicker = (NumberPicker)findViewById(R.id.pick_class);
        sectionPicker = (NumberPicker)findViewById(R.id.pick_section);
        subjectPicker = (NumberPicker)findViewById(R.id.pick_subject);
        datePicker = (DatePicker)findViewById(R.id.pick_date_attendance);


        setupPicker(classPicker, classUrl, "standard", "class_api");
        setupPicker(sectionPicker, sectionUrl, "section", "section_api");

        setupPicker(subjectPicker, subjectUrl, "subject", "subject_api");

        // 05/07/2017 - recently it was found at Lord Krishna Public School that some teachers
        // use old devices having smaller size and low resolution. So the pickers overlap each other
        // We need to fix this by reducing the scale factor.
        int density= getResources().getDisplayMetrics().densityDpi;
        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                subjectPicker.setScaleX(0.6f);
                break;
            case DisplayMetrics.DENSITY_HIGH:
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                break;
        }

        int screenSize = getResources().getConfiguration().
                screenLayout &Configuration.SCREENLAYOUT_SIZE_MASK;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                break;
            default:
                break;

        }
        // if teacher has not yet set subjects, then we need to get all subjects
        //if (subjectPicker.getMaxValue() < 1)
            //setupPicker(subjectPicker, subjectUrl2, "subject_name", "subject_api");

        // check method from previous activity has fired this activity. Accordingly we change
        // behoviour of this activity
        Intent intent = getIntent();
        sender = intent.getStringExtra("sender");
        Calendar calendar =  Calendar.getInstance();
        switch(sender)  {
            case "takeAttendance":
                // Future dated attendance is not allowed

                datePicker.setMaxDate(calendar.getTimeInMillis());
                menu = "Take Attendance";
                //submit_button.setText("Take Attendance");
                break;
            case "scheduleTest":
                //submit_button.setText("Schedule Test");
                menu = "Schedule Test";
                break;
            case "createHW":
                datePicker.setMinDate(System.currentTimeMillis() - 1000);
                datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH) + 1);
                menu = "Take Pic";
                TextView textView = (TextView)findViewById(R.id.txtAttendanceHeading);
                textView.setText(R.string.hw_due_date);
                break;
            default:
                //submit_button.setText("Go Ahead");
                break;
        }
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

    public void setupPicker(final NumberPicker picker, String url,
                            final String item_to_extract, final String tag) {
        final ArrayList<String> item_list = new ArrayList<String>();

        final ProgressDialog progressDialog = new ProgressDialog(this);
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
                                String an_Item = jo.getString(item_to_extract);
                                item_list.add(an_Item);
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception while dealing with "
                                        + tag);
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while dealing with" + tag);
                                e.printStackTrace();
                            }
                        }
                        progressDialog.hide();
                        progressDialog.dismiss();
                        String[] picker_contents = item_list.toArray(new String[item_list.size()]);
                        try {
                            picker.setMaxValue(picker_contents.length - 1);
                            picker.setDisplayedValues(picker_contents);
                        }
                        catch (ArrayIndexOutOfBoundsException e)    {
                            System.out.println("there seems to be no data for " + tag);
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "It looks that you have not yet set subjects. " +
                                            "Please set subjects", Toast.LENGTH_LONG).show();
                            startActivity(new Intent("com.classup.SetSubjects"));
                        }
                        catch (Exception e) {
                            System.out.println("ran into exception during " + tag);
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "It looks that you have not yet set subjects. " +
                                            "Please set subjects", Toast.LENGTH_LONG).show();
                            startActivity(new Intent("com.classup.SetSubjects"));
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection, please try later",
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
                        System.out.println("inside volley error handler");
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (sender.equals("createHW")) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                menu.getItem(0).setEnabled(false);
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
        return true;
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, menu).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        take_pic = m.getItem(0);
        return true;
    }

    public void takeAttendaneOrScheduleTest () {
        final Integer d = datePicker.getDayOfMonth();
        final Integer m = datePicker.getMonth() + 1;  // because index start from 0
        final Integer y = datePicker.getYear();
        // Get the class
        final String[] classList = classPicker.getDisplayedValues();
        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();
        // Get the subject
        final String[] subjectList = subjectPicker.getDisplayedValues();
        switch (sender) {
            case "takeAttendance":
                Intent intent = new Intent(this, AttendanceList.class);
                // Collect the values from pickers
                // get the Date. Due to different handling of date by Java and Python
                // we will be using the raw dates, ie, date, month and year separately

                intent.putExtra("date", d.toString());
                intent.putExtra("month", m.toString());
                intent.putExtra("year", y.toString());
                intent.putExtra("class", classList[(classPicker.getValue())]);
                intent.putExtra("section", sectionList[(sectionPicker.getValue())]);
                intent.putExtra("subject", subjectList[(subjectPicker.getValue())]);

                startActivity(intent);
                break;

            case "createHW":
                Intent intent1 = new Intent(this, ReviewHW.class);
                // Collect the values from pickers
                // get the Date. Due to different handling of date by Java and Python
                // we will be using the raw dates, ie, date, month and year separately
                intent1.putExtra("sender_type", "teacher");
                intent1.putExtra("date", d.toString());
                intent1.putExtra("month", m.toString());
                intent1.putExtra("year", y.toString());
                intent1.putExtra("class", classList[(classPicker.getValue())]);
                intent1.putExtra("section", sectionList[(sectionPicker.getValue())]);
                intent1.putExtra("subject", subjectList[(subjectPicker.getValue())]);

                startActivity(intent1);
                break;

            case "scheduleTest":
                String subject = subjectList[(subjectPicker.getValue())];
                if (subject.equals("Main")) {
                    String message = "Test cannot be scheduled for Main." +
                            " Please choose another Subject";
                    Toast toast = Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                }

                final Dialog dialog = new Dialog(SelectClass.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_test_details);

                // set default values for max_marks and passing_marks
                ((EditText)dialog.findViewById(R.id.txt_max_marks)).setText("0");
                ((EditText)dialog.findViewById(R.id.txt_passing_marks)).setText("0");
                ((EditText)dialog.findViewById(R.id.txt_comments)).setText(" ");

                // initially the check box for Grade Based is unchecked. This means the test is
                // marks based
                final CheckBox checkBox =
                        (CheckBox)dialog.findViewById(R.id.chk_whether_grade_based);
                GradeBased.getInstance().setGrade_based("False");
                //checkBox.setEnabled(false);
                dialog.show();

                checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view1) {
                        if (checkBox.isChecked()) {
                            GradeBased.getInstance().setGrade_based("True");
                            (dialog.findViewById(R.id.txt_max_marks)).setEnabled(false);
                            ((EditText) dialog.findViewById(R.id.txt_passing_marks)).
                                    setEnabled(false);
                        } else {
                            GradeBased.getInstance().setGrade_based("False");
                            ((EditText) dialog.findViewById(R.id.txt_max_marks)).setEnabled(true);
                            ((EditText) dialog.findViewById(R.id.txt_passing_marks)).
                                    setEnabled(true);
                        }
                    }
                });
                Button btn_submit = (Button)dialog.findViewById(R.id.btn_test_confirm);
                btn_submit.setOnClickListener(new View.OnClickListener()    {
                    @Override
                    public void onClick(View view)  {
                        Boolean good_to_go = true;
                        String comments = ((EditText)dialog.findViewById(R.id.txt_comments)).
                                getText().toString();
                        if (comments.equals(" "))
                            comments = "No_Comments";
                        String mm = ((EditText)dialog.findViewById(R.id.txt_max_marks)).
                                getText().toString();
                        String pm = ((EditText)dialog.findViewById(R.id.txt_passing_marks)).
                                getText().toString();
                        if(GradeBased.getInstance().getGrade_based().equals("False"))  {
                            if (mm.equals("") || mm.equals("0") || pm.equals(""))    {
                                good_to_go = false;
                                String message;
                                switch (mm) {
                                    case "":
                                        message = "Max Marks cannot be blank.";
                                        break;
                                    case "0":
                                        message = "Max Marks cannot be 0.";
                                        break;
                                    default:
                                        message = "Passing Marks cannot be blank.";
                                }

                                Toast.makeText(getApplicationContext(),
                                        message,
                                        Toast.LENGTH_LONG).show();
                            }
                            else
                                good_to_go = true;
                        }

                        if (good_to_go) {
                            String g = "1";
                            switch (GradeBased.getInstance().getGrade_based())  {
                                case "True":
                                    g = "0";
                                    break;
                                case "False":
                                    g = "1";

                            }
                            // in case of grade based test, teacher will leave the max marks
                            // and pass marks blank. In this case the url will not be formed
                            // correctly. Hence we need to set these to zero explicitly
                            if (mm.equals(""))
                                mm = "0";
                            if (pm.equals(""))
                                pm = "0";
                            String logged_in_user = SessionManager.getInstance().getLogged_in_user();
                            // as we are using sihgleton pattern to get the logged in user, sometimes the method
                            // call returns a blank string. In this case we will retry for 20 times and if not
                            // successful even after then we will ask the user to log in again
                            int i = 0;
                            while (logged_in_user.equals("")) {
                                logged_in_user = SessionManager.getInstance().getLogged_in_user();
                                if (i++ == 20)  {
                                    Toast.makeText(getApplicationContext(),
                                            "There seems to be some problem with network. " +
                                                    "Please re-login",
                                            Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(),
                                            LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                            // now we have to send a POST request to backend to create the test

                            String url =  server_ip + "/academics/create_test/" + school_id
                                    + "/" + classList[(classPicker.getValue())]
                                    + "/" + sectionList[(sectionPicker.getValue())]
                                    + "/" + subjectList[(subjectPicker.getValue())]
                                    + "/" + logged_in_user
                                    + "/" + d.toString() + "/" + m.toString() + "/" + y.toString()
                                    + "/" + mm + "/" + pm
                                    + "/" + g + "/" + comments
                                    + "/";
                            url = url.replace(" ", "%20");
                            String tag = "Create Test";
                            StringRequest request = new StringRequest(Request.Method.POST, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            // 11/09/17 - Now we are building the custom
                                            // Analysis via AWS
                                            try {
                                                AnalyticsEvent scheduleTestEvent =
                                                        SessionManager.getInstance().
                                                        analytics.getEventClient().
                                                                createEvent("Schedule Test");
                                                scheduleTestEvent.addAttribute("user",
                                                        SessionManager.getInstance().
                                                                getLogged_in_user());
                                                SessionManager.getInstance().analytics.
                                                        getEventClient().
                                                        recordEvent(scheduleTestEvent);
                                            } catch (NullPointerException exception)    {
                                                System.out.println("flopped in creating " +
                                                        "analytics Schedule Test");
                                            } catch (Exception exception)   {
                                                System.out.println("flopped in " +
                                                        "creating analytics Schedule Test");
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();

                                            if (error instanceof TimeoutError ||
                                                    error instanceof NoConnectionError) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Slow network connection, please try later",
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
                                        }
                                    });
                            com.classup.AppController.getInstance().addToRequestQueue(request, tag);
                            // as GradeBased is a singleton class, it is necessary to set
                            // grade_based to No
                            GradeBased.getInstance().setGrade_based("False");

                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Test Scheduled",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent("com.classup.TeacherMenu").
                                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                    }
                });

                Button btn_cancel = (Button)dialog.findViewById(R.id.btn_test_cancel);
                btn_cancel.setOnClickListener(new View.OnClickListener()    {
                    @Override
                    public void onClick(View view)  {
                        dialog.dismiss();
                        startActivity(new Intent("com.classup.TeacherMenu"));
                    }
                });
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case 0:
                if (!sender.equals("createHW")) {
                    takeAttendaneOrScheduleTest();
                } else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(this, "com.classup.provider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                        }
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Integer d = datePicker.getDayOfMonth();
        final Integer m = datePicker.getMonth() + 1;  // because index start from 0
        final Integer y = datePicker.getYear();
        // Get the class
        final String[] classList = classPicker.getDisplayedValues();
        // Get the section
        final String[] sectionList = sectionPicker.getDisplayedValues();
        // Get the subject
        final String[] subjectList = subjectPicker.getDisplayedValues();

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            Intent intent1 = new Intent(this, ReviewHW.class);
            intent1.putExtra("sender", "select_class");

            // Collect the values from pickers
            // get the Date. Due to different handling of date by Java and Python
            // we will be using the raw dates, ie, date, month and year separately

            intent1.putExtra("date", d.toString());
            intent1.putExtra("month", m.toString());
            intent1.putExtra("year", y.toString());
            intent1.putExtra("class", classList[(classPicker.getValue())]);
            intent1.putExtra("section", sectionList[(sectionPicker.getValue())]);
            intent1.putExtra("subject", subjectList[(subjectPicker.getValue())]);
            intent1.putExtra("photo_path", mCurrentPhotoPath);
            startActivity(intent1);
        }
        else    {
            Toast toast = Toast.makeText(this, "Error creating Home Work. Plase try again",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                take_pic.setEnabled(true);
            }
        }
    }
}