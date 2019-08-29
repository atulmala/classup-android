//package com.classup;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.graphics.Color;
//import android.net.Uri;
//import android.preference.PreferenceManager;
//import android.provider.MediaStore;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import com.android.volley.toolbox.ImageLoader;
//import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
//import com.google.api.client.googleapis.media.MediaHttpUploader;
//import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.InputStreamContent;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.client.util.ExponentialBackOff;
//import com.google.api.services.youtube.YouTube;
//import com.google.api.services.youtube.model.Video;
//import com.google.api.services.youtube.model.VideoSnippet;
//import com.google.api.services.youtube.model.VideoStatus;
//
//import java.io.BufferedInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import static com.classup.AppController.TAG;
//import static com.google.api.client.googleapis.media.MediaHttpDownloader.DownloadState.NOT_STARTED;
//import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.INITIATION_COMPLETE;
//import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.INITIATION_STARTED;
//import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.MEDIA_COMPLETE;
//import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS;
//
//public class UploadVideo1 {
//    Uri mFileUri;
//    Context context;
//    Boolean whole_class;
//    String the_class;;
//    String section;
//    ArrayList<String> students;
//students
//    GoogleAccountCredential credential;
//    public static final String ACCOUNT_KEY = "atulmala@gmail.com";
//    private ImageLoader mImageLoader;
//    private String mChosenAccountName;
//    private String appName;
//    private Uri mFileURI = null;
//    private static String VIDEO_FILE_FORMAT = "video/*";
//
//    static final String REQUEST_AUTHORIZATION_INTENT = "com.google.example.yt.RequestAuth";
//    static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.google.example.yt.RequestAuth.param";
//    private static int UPLOAD_NOTIFICATION_ID = 1001;
//    private static int PLAYBACK_NOTIFICATION_ID = 1002;
//
//
//    public UploadVideo(Uri mFileUri, Context context, Boolean whole_class, String the_class,
//                       String section, ArrayList<String> students) {
//        this.mFileUri = mFileUri;
//        this.context = context;
//        this.whole_class = whole_class;
//        this.the_class = the_class;
//        this.section = section;
//        this.students = students;
//
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        mChosenAccountName = sp.getString(ACCOUNT_KEY, null);
//        credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(Auth.SCOPES));
//        credential.setBackOff(new ExponentialBackOff());
//        credential.setSelectedAccountName(mChosenAccountName);
//        credential.setBackOff(new ExponentialBackOff());
//
//        appName = context.getResources().getString(R.string.app_name);
//    }
//
//    public String upload() {
//        final HttpTransport transport = AndroidHttp.newCompatibleTransport();
//        final JsonFactory jsonFactory = new GsonFactory();
//
//        final YouTube youtube = new YouTube.Builder(transport, jsonFactory,
//            credential).setApplicationName(appName).build();
//
//        InputStream fileInputStream = null;
//        String videoId = null;
//        try {
//
//            final long fileSize = context.getContentResolver().openFileDescriptor(mFileUri,
//                "r").getStatSize();
//            fileInputStream = context.getContentResolver().openInputStream(mFileUri);
//            String[] proj = {MediaStore.Images.Media.DATA};
//            Cursor cursor = context.getContentResolver().query(mFileUri, proj,
//                null, null, null);
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//
//            //videoId = ResumableUpload.upload(youtube, fileInputStream, fileSize, mFileUri, cursor.getString(column_index), context.getApplicationContext());
//            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.classup_logo);
//            final NotificationManager notifyManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            // The id of the channel.
//            String id = "my_channel_01";
//
//            CharSequence name = context.getString(R.string.channel_name);
//
//            String description = context.getString(R.string.channel_description);
//
//            int importance = android.app.NotificationManager.IMPORTANCE_LOW;
//
//            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
//
//            mChannel.setDescription(description);
//
//            mChannel.enableLights(true);
//            mChannel.setLightColor(Color.RED);
//
//            mChannel.enableVibration(true);
//            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//
//            notifyManager.createNotificationChannel(mChannel);
//
//            try {
//                // Add extra information to the video before uploading.
//                Video videoObjectDefiningMetadata = new Video();
//                VideoStatus status = new VideoStatus();
//                status.setPrivacyStatus("public");
//                videoObjectDefiningMetadata.setStatus(status);
//
//                // We set a majority of the metadata with the VideoSnippet object.
//                VideoSnippet snippet = new VideoSnippet();
//                snippet.setTags(Arrays.asList(Constants.DEFAULT_KEYWORD,
//                    generateKeywordFromPlaylistId(Constants.UPLOAD_PLAYLIST)));
//
//                // Set completed snippet to the video object.
//                videoObjectDefiningMetadata.setSnippet(snippet);
//
//                InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,
//                        new BufferedInputStream(fileInputStream));
//                mediaContent.setLength(fileSize);
//                YouTube.Videos.Insert videoInsert =
//                    youtube.videos().insert("snippet,statistics,status", videoObjectDefiningMetadata,
//                        mediaContent);
//
//                // Set the upload type and add event listener.
//                MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
//
//                /*
//                 * Sets whether direct media upload is enabled or disabled. True = whole media content is
//                 * uploaded in a single request. False (default) = resumable media upload protocol to upload
//                 * in data chunks.
//                 */
//                uploader.setDirectUploadEnabled(false);
//
//                MediaHttpUploaderProgressListener progressListener =
//                    new MediaHttpUploaderProgressListener() {
//                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
//                        switch (uploader.getUploadState()) {
//                            case INITIATION_STARTED:
//                                builder.setContentText(context.getString
//                                    (R.string.initiation_started)).setProgress((int) fileSize,
//                                    (int) uploader.getNumBytesUploaded(), false);
//                                notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
//                                break;
//                            case INITIATION_COMPLETE:
//                                builder.setContentText(context.getString
//                                    (R.string.initiation_completed)).setProgress((int) fileSize,
//                                    (int) uploader.getNumBytesUploaded(), false);
//                                notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
//                                break;
//                            case MEDIA_IN_PROGRESS:
//                                builder
//                                    .setContentTitle(context.getString(R.string.youtube_upload) +
//                                        (int) (uploader.getProgress() * 100) + "%")
//                                    .setContentText(context.getString(R.string.upload_in_progress))
//                                    .setProgress((int) fileSize,
//                                        (int) uploader.getNumBytesUploaded(), false);
//                                notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
//                                break;
//                            case MEDIA_COMPLETE:
//                                builder.setContentTitle(context.getString(R.string.upload_completed))
//                                    .setContentText(context.getString(R.string.upload_completed))
//                                    // Removes the progress bar
//                                    .setProgress(0, 0, false);
//                                notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
//                            case NOT_STARTED:
//                                Log.d(this.getClass().getSimpleName(),
//                                    context.getString(R.string.upload_not_started));
//                                break;
//                        }
//                    }
//                };
//                //uploader.setProgressListener(progressListener);
//
//                // Execute upload.
//                Video returnedVideo = videoInsert.execute();
//                Log.d(TAG, "Video upload completed");
//                videoId = returnedVideo.getId();
//                Log.d(TAG, String.format("videoId = [%s]", videoId));
//
//            } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
//                Log.e(TAG, "GooglePlayServicesAvailabilityIOException", availabilityException);
//                notifyFailedUpload(context, context.getString(R.string.cant_access_play),
//                    notifyManager, builder);
//            } catch (UserRecoverableAuthIOException userRecoverableException) {
//                Log.i(TAG, String.format("UserRecoverableAuthIOException: %s",
//                    userRecoverableException.getMessage()));
//                requestAuth(context, userRecoverableException);
//            } catch (IOException e) {
//                Log.e(TAG, "IOException", e);
//                notifyFailedUpload(context, context.getString(R.string.please_try_again),
//                    notifyManager, builder);
//            } catch (Exception e) {
//                Log.e(TAG, "IOException", e);
//                notifyFailedUpload(context, context.getString(R.string.please_try_again),
//                    notifyManager, builder);
//            }
//
//
//        } catch (FileNotFoundException e) {
//            Log.e(context.getApplicationContext().toString(), e.getMessage());
//        } finally {
//            try {
//                fileInputStream.close();
//            } catch (IOException e) {
//                // ignore
//            }
//        }
//        return videoId;
//    }
//
//    private static void requestAuth(Context context,
//                                    UserRecoverableAuthIOException userRecoverableException) {
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
//        Intent authIntent = userRecoverableException.getIntent();
//        Intent runReqAuthIntent = new Intent(REQUEST_AUTHORIZATION_INTENT);
//        runReqAuthIntent.putExtra(REQUEST_AUTHORIZATION_INTENT_PARAM, authIntent);
//        manager.sendBroadcast(runReqAuthIntent);
//        Log.d(TAG, String.format("Sent broadcast %s", REQUEST_AUTHORIZATION_INTENT));
//    }
//
//    private static void notifyFailedUpload(Context context, String message,
//                                           NotificationManager notifyManager,
//                                           NotificationCompat.Builder builder) {
//        builder.setContentTitle(context.getString(R.string.upload_failed))
//            .setContentText(message);
//        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
//    }
//
//    public static String generateKeywordFromPlaylistId(String playlistId) {
//        if (playlistId == null) playlistId = "";
//        if (playlistId.indexOf("PL") == 0) {
//            playlistId = playlistId.substring(2);
//        }
//        playlistId = playlistId.replaceAll("\\W", "");
//        String keyword = Constants.DEFAULT_KEYWORD.concat(playlistId);
//        if (keyword.length() > Constants.MAX_KEYWORD_LENGTH) {
//            keyword = keyword.substring(0, Constants.MAX_KEYWORD_LENGTH);
//        }
//        return keyword;
//    }
//}
