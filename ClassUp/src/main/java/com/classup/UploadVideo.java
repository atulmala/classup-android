package com.classup;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class UploadVideo {
    private static final String DEVELOPER_KEY = "AIzaSyCVmBBIm0CrGF2nSZMOYeeXRVqoEWw3HBY";

    private static final String APPLICATION_NAME = "ClassUp YouTube Video upload";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static YouTube youtubeService;
    YouTube.Videos.Insert request = null;

    Context context;
    Uri mFileUri;
    Boolean whole_class;
    String the_class;
    String section;
    ArrayList<String> students;

    public UploadVideo(Context context, Uri mFileUri, Boolean whole_class, String the_class,
                       String section, ArrayList<String> students) {
        this.context = context;
        this.mFileUri = mFileUri;
        this.whole_class = whole_class;
        this.the_class = the_class;
        this.section = section;
        this.students = students;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public void upload_video() {
        try {
            youtubeService = getService();

        } catch (GeneralSecurityException e) {
            System.out.println("youtubeservice creation failed due to GeneralSecurityException");
            e.printStackTrace();
            Log.v("youtube", "exception", e);
        } catch (IOException e) {
            System.out.println("youtubeservice creation failed due to IOException");



        }

        // Define the Video object, which will be uploaded as the request body.
        Video video = new Video();

        // Add the snippet object property to the Video object.
        VideoSnippet snippet = new VideoSnippet();
        snippet.setCategoryId("22");
        snippet.setDescription("Description of uploaded video.");
        snippet.setTitle("Test video upload.");
        video.setSnippet(snippet);

        // Add the status object property to the Video object.
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private");
        video.setStatus(status);

        String path = getPath(mFileUri);
        File mediaFile = new File(path);
        InputStreamContent mediaContent = null;
        try {
            mediaContent = new InputStreamContent("video/*",
                new BufferedInputStream(new FileInputStream(mediaFile)));
            System.out.println("mediaContent = " + mediaContent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mediaContent.setLength(mediaFile.length());

        // Define and execute the API request

        try {
            request = youtubeService.videos()
                .insert("snippet,status", video, mediaContent);
            InsertVideo insertVideo = new InsertVideo();
            insertVideo.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class InsertVideo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            Video response = null;
            try {
                response = request.setKey("t3sbLcdhHA8E7_fVQMTasU5l").execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("response = " + response);
            // do above Server call here
            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null,
            null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();


        cursor = context.getContentResolver().query(
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null, MediaStore.Video.Media._ID + " = ? ",
            new String[]{document_id}, null);


        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}
