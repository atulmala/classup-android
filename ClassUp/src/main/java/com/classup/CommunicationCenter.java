package com.classup;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class CommunicationCenter extends AppCompatActivity {

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
        //Intent intent = new Intent(this, SelectClassSection.class);
        //intent.putExtra("sender", "share_video");
       // startActivity(intent);
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra("sender", "share_video");
        final int ACTIVITY_SELECT_VIDEO = 100;
        startActivityForResult(intent, ACTIVITY_SELECT_VIDEO);
    }

    public void sharePic(View view) {
        //Intent intent = new Intent(this, SelectClassSection.class);

        //startActivity(intent);
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra("sender", "share_pic");
        final int ACTIVITY_SELECT_IMAGE = 200;
        startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);
    }
}
