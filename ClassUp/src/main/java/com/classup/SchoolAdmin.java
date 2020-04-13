package com.classup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SchoolAdmin extends AppCompatActivity {
    final int ACTIVITY_SELECT_IMAGE = 200;
    final int ACTIVITY_SELECT_VIDEO = 300;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;

    String mCurrentPhotoPath;
    String mCurrentVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_admin);
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

    public void teacherAttendance (View view)   {
        Intent intent = new Intent(this, SelectDate.class);
        intent.putExtra("comingFrom", "teacherAttendance");
        startActivity(intent);
    }

    public void attendanceSummary(View view)    {
        Intent intent = new Intent(this, SelectDate.class);
        intent.putExtra("comingFrom", "attendanceSummary");
        startActivity(intent);
    }

    public void sendBulkSMS(View view)  {
        Intent intent = new Intent(this, SendBulkSMS.class);
        intent.putExtra("sender", "send_bulk_sms");
        startActivity(intent);
    }

    public void shareVideo(View view)  {
        // check for storage access permissions
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
       } else {
            // Permission has already been granted
            Intent intent2 = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent2.putExtra("sender", "share_pic");

            startActivityForResult(intent2, ACTIVITY_SELECT_VIDEO);
        }
    }

    public void sharePic(View view) {
        // check for storage access permissions
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted
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

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null,
            null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, MediaStore.Images.Media._ID + " = ? ",
            new String[]{document_id}, null);
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
            mCurrentPhotoPath = getPath(selectedImageUri);
            Intent intent1 = new Intent(this, ReviewHW.class);
            intent1.putExtra("sender", "admin_share_image");
            intent1.putExtra("photo_path", mCurrentPhotoPath);
            startActivity(intent1);
        }
        else    {
            Toast toast = Toast.makeText(this,
                "Error Selecting Image/video. Plase try again", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

    }

    public void addStudent(View view)  {
        Intent intent = new Intent(this, AddStudent.class);
        startActivity(intent);
    }

    public void updateStudent(View view)    {
        Intent intent = new Intent(this, SelectClassSection1.class);
        intent.putExtra("sender", "school_admin");
        startActivity(intent);
    }

    public void addTeacher(View view)   {
        Intent intent = new Intent(this, AddTeacher.class);
        startActivity(intent);
    }

    public void updateTeacher(View view)    {
        Intent intent = new Intent(this, SelectTeacher.class);
        startActivity(intent);
    }

    public void admin_changePassword(View view) {
        Intent intent = new Intent(this, PasswordChange.class);
        startActivity(intent);
    }

    public void performLogout(View view)    {
        SessionManager.getInstance().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0,
            "Logout").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

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
