package com.classup;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;


public class VideoReviewActivity extends AppCompatActivity {
    VideoView mVideoView;
    MediaController mc;
    private String mChosenAccountName;
    private Uri mFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_review);
        Intent intent = getIntent();

        setTitle(R.string.playing_the_video_in_upload_progress);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mFileUri = intent.getData();

        try {
            mVideoView = findViewById(R.id.videoView);
            mc = new MediaController(this);
            mVideoView.setMediaController(mc);
            mVideoView.setVideoURI(mFileUri);

            mc.show();
            mVideoView.start();
        } catch (Exception e) {
            Log.e(this.getLocalClassName(), e.toString());
        }
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent intent = getIntent();
        if (intent.getStringExtra("sender").equals("share_video"))
            menu.add(0, 0, 0,
                "Upload").
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, SelStudentForPicSharing.class);
        intent.putExtra("sender", "share_video");
        intent.setData(mFileUri);
        System.out.println("intent set to SelstudentForPicSharing");
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }
}
