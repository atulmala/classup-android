package com.classup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.Date;

public class HWList extends AppCompatActivity {
    final Activity activity = this;
    String tag = "HWList";
    String server_ip;
    String school_id;
    String sender;
    String student_id;

    final int ACTIVITY_SELECT_IMAGE = 200;
    final int ACTIVITY_SELECT_VIDEO = 300;
    String mCurrentPhotoPath;
    String mCurrentVideoPath;
    String dealing_with;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hw_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

        final Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        String logged_in_user = SessionManager.getInstance().getLogged_in_user();

        final ArrayList<HWListSource> hw_list = new ArrayList<>();
        final ArrayList<ImageVideoSource> image_list = new ArrayList<>();

        ListView listView = findViewById(R.id.teacher_hw_list);
        String url1 = "/academics/";
        String retrieval_message = "Please wait...";
        sender = getIntent().getStringExtra("sender");
        switch (sender) {
            case "ParentApp":
                dealing_with = "HW";
                this.setTitle("HW List");
                retrieval_message = "Retrieving Home Work list. Please wait...";
                student_id = getIntent().getStringExtra("student_id");
                url1 = server_ip + "/academics/retrieve_hw/" + student_id + "/?format=json";
                break;
            case "parent_pic_video":
                dealing_with = "image_video";
                student_id = getIntent().getStringExtra("student_id");
                this.setTitle("Image/Video List ");

                retrieval_message = "Retrieving shared Pics/Video list. Please wait...";
                url1 = server_ip + "/pic_share/get_pic_video_list_teacher/";
                url1 +=  student_id + "/?format=json";
                break;
            case "teacher_menu":
                dealing_with = "HW";
                this.setTitle("HW List");
                retrieval_message = "Retrieving Home Work list. Please wait...";
                url1 = server_ip + "/academics/retrieve_hw/" + logged_in_user + "/?format=json";
                break;
            case "share_pic":
                dealing_with = "image_video";
                this.setTitle("Pics/Video List");
                retrieval_message = "Retrieving shared Pics list. Please wait...";
                url1 = server_ip + "/pic_share/get_pic_video_list_teacher/";
                url1 +=  logged_in_user + "/?format=json";
                break;
            case "share_video":
                dealing_with = "image_video";
                this.setTitle("Video List");
                retrieval_message = "Retrieving shared Videos list. Please wait...";
                url1 = server_ip + "/pic_share/get_pic_video_list_teacher/";
                url1 += logged_in_user + "/?format=json";
                break;

        }

        final String url = url1;
        final HWListAdapter hwListAdapter = new HWListAdapter(this, hw_list);
        final ImageVideoListAdapter imageVideoListAdapter =
            new ImageVideoListAdapter(this, image_list);

        if (dealing_with.equals("HW"))
            listView.setAdapter(hwListAdapter);
        else
            listView.setAdapter(imageVideoListAdapter);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(retrieval_message);
        progressDialog.setCancelable(true);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response.length() < 1) {
                        if (dealing_with.equals("HW")) {
                            Toast toast = Toast.makeText(c, "No HW created.",
                                Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        else {
                            Toast toast = Toast.makeText(c, "No Image/Video shared.",
                                Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);

                            if(dealing_with.equals("HW")) {
                                // get the id of the hw
                                String id = jo.getString("id");

                                String date = jo.getString("due_date");
                                String yy = date.substring(0, 4);
                                String month = date.substring(5, 7);
                                String dd = date.substring(8, 10);
                                String ddmmyyyy = dd + "/" + month + "/" + yy;

                                String teacher = jo.getString("teacher");

                                String the_class = jo.getString("the_class");

                                String section = jo.getString("section");
                                String subject = jo.getString("subject");
                                String location = jo.getString("location");

                                String notes = jo.getString("notes");

                                // put all the above details into the adapter
                                hw_list.add(new HWListSource(id, teacher, the_class, section,
                                    subject, ddmmyyyy, location, notes));
                                hwListAdapter.notifyDataSetChanged();
                            }
                            else    {
                                // get the id of the hw
                                String id = jo.getString("id");

                                String date = jo.getString("creation_date");
                                String yy = date.substring(0, 4);
                                String month = date.substring(5, 7);
                                String dd = date.substring(8, 10);
                                String ddmmyyyy = dd + "/" + month + "/" + yy;

                                String type = jo.getString("type");

                                String teacher = jo.getString("teacher");

                                String the_class = jo.getString("the_class");

                                String section = jo.getString("section");

                                String description = jo.getString("descrition");
                                String location = jo.getString("location");
                                String short_link = jo.getString("short_link");

                                // put all the above details into the adapter
                                image_list.add(new ImageVideoSource(id, ddmmyyyy, type, teacher,
                                    the_class, section, description, location, short_link));
                                imageVideoListAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException je) {
                            System.out.println("Ran into JSON exception " +
                                "while trying to fetch the HW/Image list");
                            je.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Caught General exception " +
                                "while trying to fetch the HW/Image list");
                            e.printStackTrace();
                        }
                    }
                    // 12/09/17 - Now we are building the custom
                    // Analysis via AWS
                    try {
                        AnalyticsEvent event =
                            SessionManager.analytics.getEventClient().
                                createEvent("Retrieve HW List");
                        event.addAttribute("user", SessionManager.getInstance().
                            getLogged_in_user());
                        SessionManager.analytics.getEventClient().
                            recordEvent(event);
                    } catch (NullPointerException exception)    {
                        System.out.println("flopped in creating analytics Retrieve HW List");
                    } catch (Exception exception)   {
                        System.out.println("flopped in " +
                            "creating analytics Retrieve HW List");
                    }
                    progressDialog.hide();
                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("inside volley error handler");
                    progressDialog.hide();
                    progressDialog.dismiss();
                    if (error instanceof TimeoutError ||
                        error instanceof NoConnectionError) {
                        Toast.makeText(c, "Slow network connection",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(c, "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(c, "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        //TODO
                    }
                    // TODO Auto-generated method stub
                }
            });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (dealing_with) {
                    case "HW":
                        Intent intent1 = new Intent(activity, ReviewHW.class);
                        String location;
                        intent1.putExtra("sender", "hw_list");

                        String hw_id = hw_list.get(i).getId();
                        intent1.putExtra("hw_id", hw_id);
                        location = hw_list.get(i).getLocation();
                        System.out.println("location = " + location);

                        intent1.putExtra("location", location);
                        startActivity(intent1);
                        break;
                }
            }
        });

        // long tap on the list view will delete the homework
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // here i is equivalent of position in OnItemClickListener - wonder why Android
                // designer use a different nomenclature here
                switch (dealing_with) {
                    case "HW":
                        final String hw_id = hw_list.get(i).getId();

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage("Are you sure that you want to delete this HW? ")
                            .setPositiveButton("Delete HW", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String server_ip = MiscFunctions.getInstance().
                                        getServerIP(activity);
                                    String url = server_ip + "/academics/delete_hw/" +
                                        hw_id + "/";
                                    String tag = "TestDeletion";
                                    StringRequest request = new StringRequest(Request.Method.DELETE,
                                        url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Toast toast = Toast.makeText
                                                    (getApplicationContext(), "HW Deleted",
                                                        Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER,
                                                    0, 0);
                                                toast.show();
                                                startActivity(new Intent
                                                    ("com.classup.TeacherMenu").
                                                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast toast = Toast.makeText
                                                    (getApplicationContext(),
                                                        "HW could not be Deleted. " +
                                                            "Please try again",
                                                        Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER,
                                                    0, 0);
                                                toast.show();
                                                error.printStackTrace();
                                            }
                                        });
                                    com.classup.AppController.getInstance().
                                        addToRequestQueue(request, tag);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                        // Create the AlertDialog object and return it
                        builder.show();
                        return true;
                    case "image_video":
                        final String image_id = image_list.get(i).getId();

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                        builder1.setMessage("Are you sure that you want to delete this Media? ")
                            .setPositiveButton("Delete Media",
                                new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String server_ip = MiscFunctions.getInstance().
                                        getServerIP(activity);
                                    String url = server_ip + "/pic_share/delete_media/" +
                                        image_id + "/";
                                    String tag = "TestDeletion";
                                    StringRequest request = new StringRequest(Request.Method.DELETE,
                                        url, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                System.out.println("response = " + response);

                                                Toast toast = Toast.makeText
                                                    (getApplicationContext(), response,
                                                        Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER,
                                                    0, 0);
                                                toast.show();
                                                startActivity(new Intent
                                                    ("com.classup.TeacherMenu").
                                                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast toast = Toast.makeText
                                                    (getApplicationContext(),
                                                        "Media could not be Deleted. " +
                                                            "Please try again",
                                                        Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER,
                                                    0, 0);
                                                toast.show();
                                                error.printStackTrace();
                                            }
                                        });
                                    com.classup.AppController.getInstance().
                                        addToRequestQueue(request, tag);
                                }
                            })
                            .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                        // Create the AlertDialog object and return it
                        if(!sender.equals("parent_pic_video"))
                            builder1.show();
                        return true;
                }
                return true;
            }
        });
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
        Intent intent = getIntent();
        if (intent.getStringExtra("sender").equals("teacher_menu"))
            menu.add(0, 0, 0,
                "Create").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (intent.getStringExtra("sender").equals("share_pic"))
            menu.add(0, 0, 0,
                "Select Pic from Gallery").
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (intent.getStringExtra("sender").equals("share_video"))
            menu.add(0, 0, 0,
                "Select Video from Gallery").
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                switch(sender) {
                    case "teacher_menu":
                        Intent intent = new Intent(this, SelectClass.class);
                        intent.putExtra("sender", "createHW");
                        System.out.println("intent set to createHW");
                        startActivity(intent);
                        break;
                    case "share_pic":
                        Intent intent1 = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent1.putExtra("sender", "share_pic");
                        if (intent1.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(this,
                                    "com.classup.provider",
                                    photoFile);
                                intent1.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(intent1, ACTIVITY_SELECT_IMAGE);
                            }
                        }
                        break;
                    case "share_video":
                        Intent intent2 = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        intent2.putExtra("sender", "share_video");
                        if (intent2.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File videoFile = null;
                            try {
                                videoFile = createVideoFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            // Continue only if the File was successfully created
                            if (videoFile != null) {
                                Uri videoURI = FileProvider.getUriForFile(this,
                                    "com.classup.provider", videoFile);
                                intent2.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                                startActivityForResult(intent2, ACTIVITY_SELECT_VIDEO);
                            }
                        }
                        //startActivityForResult(intent2, ACTIVITY_SELECT_VIDEO);
                        break;
                    default:
                        return super.onOptionsItemSelected(item);
                }
        }
        return super.onOptionsItemSelected(item);
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

    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File video = File.createTempFile(
            videoFileName,  /* prefix */
            ".mp4",         /* suffix */
            storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentVideoPath = video.getAbsolutePath();
        return video;
    }

    public String getPath(Uri uri, String sender) {
        Cursor cursor = getContentResolver().query(uri, null,
            null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        switch (sender) {
            case "share_pic":
                cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ",
                    new String[]{document_id}, null);
                break;
            case "share_video":
                cursor = getContentResolver().query(
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ",
                    new String[]{document_id}, null);
                break;
        }
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_SELECT_IMAGE && resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            Uri selectedImageUri = data.getData();
            mCurrentPhotoPath = getPath(selectedImageUri, sender);
            Intent intent1 = new Intent(this, ReviewHW.class);
            intent1.putExtra("sender", "share_image");
            intent1.putExtra("photo_path", mCurrentPhotoPath);
            startActivity(intent1);
        }
        else    {
            super.onActivityResult(requestCode, resultCode, data);
            Uri selectedVideoUri = data.getData();
            mCurrentVideoPath = getPath(selectedVideoUri, sender);
            System.out.println("mCurrentVideoPath = " + mCurrentVideoPath);
            File file = new File(mCurrentVideoPath);
            System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath().equals(mCurrentVideoPath));
            System.out.println("file = " + file);
            Intent intent1 = new Intent(this, SelStudentForPicSharing.class);
            intent1.putExtra("sender", "share_video");
            intent1.putExtra("video_path", mCurrentVideoPath);
            startActivity(intent1);
        }
    }

    // 11/04/17 As we many arrive at this activity after taking pic and uploading homework, we
    // need to program the back button.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Changes 'back' button action
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch(getIntent().getStringExtra("sender"))    {
                case "teacher_menu":
                    Intent intent1 = new Intent(this, TeacherMenu.class);
                    intent1.putExtra("sender", "createHW");
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    System.out.println("intent set to createHW");
                    startActivity(intent1);
                    break;
                case "ParentApp":
                case "parent_pic_video":
                    String student_id = getIntent().getStringExtra("student_id");
                    String student_name = getIntent().getStringExtra("student_name");
                    Intent intent2 = new Intent(this, ParentsMenu.class);
                    intent2.putExtra("sender", "createHW");
                    intent2.putExtra("student_id", student_id);
                    intent2.putExtra("student_name", student_name);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    System.out.println("intent set to createHW");
                    startActivity(intent2);
                    break;
                case "share_pic":
                case "share_video":
                    Intent intent3 = new Intent(this, CommunicationCenter.class);
                    intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent3);
            }
        }
        return true;
    }
}
