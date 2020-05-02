package com.classup;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.NetworkResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateOnlineClass extends AppCompatActivity {
    final Context context = this;
    public static final int PICKFILE_RESULT_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private NumberPicker classPicker;
    private NumberPicker subjectPicker;
    String server_ip;
    String school_id;
    String teacher;

    private Uri uri;
    private String filePath;
    //    String file_name;
    private TextView brief_description;
    private TextView youtube_link;
    private TextView pdf_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check for storage access permissions
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted
            setContentView(R.layout.activity_create_online_class);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
            this.setTitle("Create Online Class");

            // get the server ip to make api calls
            Context c = this.getApplicationContext();
            server_ip = MiscFunctions.getInstance().getServerIP(c);
            school_id = SessionManager.getInstance().getSchool_id();
            System.out.println("school_id=" + school_id);
            String classUrl = server_ip + "/academics/class_list/" +
                school_id + "/?format=json";
            String subjectUrl = server_ip + "/academics/subject_list/" +
                school_id + "/?format=json";

            String logged_in_user = SessionManager.getInstance().getLogged_in_user();
            int i = 0;
            while (logged_in_user.equals("")) {
                logged_in_user = SessionManager.getInstance().getLogged_in_user();
                if (i++ == 20) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                        "There seems to be some problem with network. Please re-login",
                        Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Intent intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                    startActivity(intent);
                }
            }
            teacher = logged_in_user;

            classPicker = findViewById(R.id.pick_class);
            subjectPicker = findViewById(R.id.pick_subject);
            setupPicker(classPicker, classUrl, "standard", "class_api");
            setupPicker(subjectPicker, subjectUrl, "subject_name", "subject_api");

            brief_description = findViewById(R.id.brief_description);
            youtube_link = findViewById(R.id.you_tube_link);
            pdf_path = findViewById(R.id.pdf_doc);

            Button choose_file = findViewById(R.id.pick_file);
            choose_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("application/pdf");
                    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                    startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String message = "Thanks for granting the permission, " +
                    "this functionality will now be available";
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                finish();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                String message = "As you have not granted the required permission, " +
                    "this functionality will not be available";
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String file_name = "";
        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == -1) {
                uri = data.getData();
                String uriString = uri.toString();
                File myFile = new File(uriString);
                filePath = uri.getPath();
                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, null,
                            null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            file_name = cursor.getString(cursor
                                .getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    file_name = myFile.getName();
                }
                pdf_path.setText(file_name);
            }
        }
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Upload")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (brief_description.getText().toString().equals("")) {
            Toast toast = Toast.makeText(this,
                "Brief Description cannot be blank", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return super.onOptionsItemSelected(item);
        }
        if (youtube_link.getText().toString().equals("") && pdf_path.getText().toString().equals("")) {
            Toast toast = Toast.makeText(this,
                "You have neiter put Video Link nor Document", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return super.onOptionsItemSelected(item);
        }

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
            Builder(this);
        String prompt = "Are you sure you want to Upload this Online Class?";
        builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
            OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait while Upload in Progress");
                progressDialog.setCancelable(false);
                progressDialog.show();
                String URL = server_ip + "/lectures/share_lecture/";
                final InputStream[] iStream = {null};
                final String file_name = pdf_path.getText().toString();

                VolleyMultipartRequest volleyMultipartRequest =
                    new VolleyMultipartRequest(Request.Method.POST, URL,
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                progressDialog.hide();
                                progressDialog.cancel();
                                Toast.makeText(getApplicationContext(),
                                    "Online Class uploaded", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, OnlineClasses.class);
                                intent.putExtra("sender", "teacher");
                                startActivity(intent);
                                finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            }
                        }) {


                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            final String[] classList = classPicker.getDisplayedValues();
                            final String the_class = classList[(classPicker.getValue())];
                            final String[] subjectList = subjectPicker.getDisplayedValues();
                            final String subject = subjectList[(subjectPicker.getValue())];

                            Map<String, String> params = new HashMap<>();
                            params.put("teacher", teacher);
                            params.put("school_id", school_id);
                            params.put("the_class", the_class);
                            params.put("section", "all_sections");
                            params.put("subject", subject);
                            params.put("all_sections", "true");
                            params.put("youtube_link", youtube_link.getText().toString());
                            params.put("lesson_topic", brief_description.getText().toString());

                            if (!file_name.equals(""))
                                params.put("file_included", "true");
                            else
                                params.put("file_included", "false");
                            params.put("file_name", pdf_path.getText().toString());

                            return params;
                        }

                        /*
                         *pass files using below method
                         * */
                        @Override
                        protected Map<String, DataPart> getByteData() {
                            Map<String, DataPart> params = new HashMap<>();
                            if (!file_name.equals("")) {
                                try {
                                    iStream[0] = getContentResolver().openInputStream(uri);
                                    assert iStream[0] != null;
                                    byte[] inputData = getBytes(iStream[0]);
                                    params.put("file", new DataPart(file_name, inputData));
                                } catch (IOException ignored) {

                                }
                            }
                            return params;
                        }
                    };
                volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                AppController.getInstance().addToRequestQueue(volleyMultipartRequest,
                    "tag");
            }
        }).setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        // Create the AlertDialog object and return it
        builder.show();

        return super.onOptionsItemSelected(item);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void setupPicker(final NumberPicker picker, String url,
                            final String item_to_extract, final String tag) {
        final ArrayList<String> item_list = new ArrayList<>();

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
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("there seems to be no data for " + tag);
                        e.printStackTrace();
                    } catch (Exception e) {
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
                    } else if (error instanceof ServerError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or " +
                                "No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or " +
                                "No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        //TODO
                    }
                    System.out.println("inside volley error handler");
                    // TODO Auto-generated method stub
                }
            });
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
            5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }
}
