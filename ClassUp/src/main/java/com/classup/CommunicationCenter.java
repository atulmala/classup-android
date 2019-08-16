package com.classup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class CommunicationCenter extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_center);

        this.setTitle("Communication Center");
    }

    public void sendMessage(View view)  {
        Intent intent = new Intent(this, SelectClassSection.class);
        intent.putExtra("sender", "send_message");
        startActivity(intent);
    }

    public void showArrangements (View view)    {
        Intent intent = new Intent (this, Arrangements.class);
        intent.putExtra("teacher", SessionManager.getInstance().getLogged_in_user());
        startActivity (intent);
    }

    public void showActivityGroups (View view)   {
        Intent intent = new Intent (this, ActivityGroup.class);
        startActivity (intent);
    }

    public void showTeacherMessageHistory (View view)   {
        Intent intent = new Intent(this, TeacherMessageRecord.class);
        startActivity(intent);
    }

    public void showCirculars(View view)    {
        Intent intent = new Intent(this, CommunicationHistory.class);
        intent.putExtra("coming_from", "teacher");
        startActivity(intent);
    }

    public void ShowWeekDays(View view) {
        Intent intent = new Intent(this, DaysofWeek.class);
        intent.putExtra("coming_from", "teacher");
        startActivity(intent);
    }

    public void shareVideo(View view)  {
        // check for storage access permissions
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            Intent intent = new Intent(this, HWList.class);
            intent.putExtra("sender", "share_pic");
            startActivity(intent);
        }
    }

    public void sharePic(View view) {
        // check for storage access permissions
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            Intent intent = new Intent(this, HWList.class);
            intent.putExtra("sender", "share_pic");
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String message = "Thanks for granting the permission, " +
                        "this functionality will now be available";
                    Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    String message = "As you have not granted the required permission, " +
                        "this functionality will not be available";
                    Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
