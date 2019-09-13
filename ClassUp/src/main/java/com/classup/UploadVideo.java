package com.classup;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.google.api.client.http.InputStreamContent;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadVideo {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SERVER_PATH = "";
    Context context;
    private Uri mFileUri;
    private Boolean whole_class;
    String the_class;
    String section;
    ArrayList<String> students;
    String description;

    public UploadVideo(Activity context, Uri mFileUri, Boolean whole_class, String the_class,
                       String section, ArrayList<String> students, String description) {
        this.context = context;
        this.mFileUri = mFileUri;
        this.whole_class = whole_class;
        this.the_class = the_class;
        this.section = section;
        this.students = students;
        this.description = description;
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public void upload_video() {
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

        String url = MiscFunctions.getInstance().getServerIP(context);
        String teacher = SessionManager.getInstance().getLogged_in_user();
        try {
            JSONObject paramObject = new JSONObject();
            paramObject.put("whole_class", whole_class);
            paramObject.put("the_class", the_class);
            paramObject.put("section", section);
            paramObject.put("students", students);
            paramObject.put("teacher", teacher);
            paramObject.put("description", description);

            String parameters = paramObject.toString();

            RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), mediaFile);
            MultipartBody.Part vFile = MultipartBody.Part.createFormData("video",
                mediaFile.getName(), videoBody);
            MultipartBody.Part params = MultipartBody.Part.createFormData("params", parameters);

            Retrofit retrofit = new Retrofit.Builder().baseUrl(url + "/pic_share/upload_video/")
                .addConverterFactory(GsonConverterFactory.create()).build();
            VideoInterface vInterface = retrofit.create(VideoInterface.class);
            Call<ResultObject> serverCom = vInterface.uploadVideoToServer(vFile, params);
            serverCom.enqueue(new Callback<ResultObject>() {
                @Override
                public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
                    ResultObject result = response.body();
                    if(!TextUtils.isEmpty(result.getSuccess())){
                        Toast.makeText(context, "Result " + result.getSuccess(),
                            Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Result " + result.getSuccess());
                    }
                }
                @Override
                public void onFailure(Call<ResultObject> call, Throwable t) {
                    Log.d(TAG, "Error message " + t.getMessage());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
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
