package com.classup;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.imagezoom.ImageAttacher;
import com.imagezoom.ImageAttacher.OnMatrixChangedListener;
import com.imagezoom.ImageAttacher.OnPhotoTapListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/* 08/04/17 courtsey
    http://androidtrainningcenter.blogspot.in/2013/04/update-pinch-to-zoom-example-and.html
*/

public class ReviewHW extends AppCompatActivity {
    String bucket = "classup2";

    final Activity a = this;
    ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;
    private Matrix matrix = new Matrix();
    private Bitmap bitmap1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_hw);
        imageView = (ImageView) findViewById(R.id.image_view);
        usingSimpleImage(imageView);

        // 12/04/2017 - we can arrive at this ativity from multiple sources
        // 1. when teacher creates homework by clicking picture. Then this actvity is used for
        // review picture and uploading to server
        // 2. when a teacher or parent taps on the list of homework to see the picture. Then this
        // activity will be shown for just reviewing the picture and then go back to hw list.
        // In this case Upload option in the menu will not be shown
        if(getIntent().getStringExtra("sender").equals("select_class")) {
            this.setTitle("Please Review");
            scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            if (getIntent().getStringExtra("photo_path").equals(""))  {
                Toast toast = Toast.makeText(this, "Error taking Picture. Please try again.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Intent intent = new Intent(this, HWList.class);
                startActivity(intent);
            }
            try {
                bitmap1 = scaleDownAndRotatePic(getIntent().getStringExtra("photo_path"));
                Picasso.with(a).load(new File(getIntent().getStringExtra("photo_path")))
                        .fit().centerCrop()
                        .into(imageView);
                //imageView.setImageBitmap(bitmap1);
            } catch (Exception e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(this, "Error taking Picture. Please try again.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Intent intent = new Intent(this, HWList.class);
                startActivity(intent);

            }
        }
        else    {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider =
                    new CognitoCachingCredentialsProvider(getApplicationContext(),
                    "us-west-2:f31aeb5c-d78f-4ba2-b0e0-4aaff07c8220", // Identity Pool ID
                    Regions.US_WEST_2 // Region
            );

            // Create an S3 client
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

            TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
            String location = getIntent().getStringExtra("location");
            final ProgressDialog progressDialog = new ProgressDialog(a);
            progressDialog.setMessage("Retrieving HW. This can take a few moment. Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            Picasso.with(getApplicationContext())
                    .load(location).fit()
                    .centerCrop()
                    .into(imageView,  new Callback() {
                @Override
                public void onSuccess() {
                    progressDialog.hide();
                    progressDialog.dismiss();

                }

                @Override
                public void onError() {
                    progressDialog.hide();
                    progressDialog.dismiss();
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Failed to download HW Image", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });
            String key = location.substring(34);
            System.out.println("key=" + key);
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + key);

            /*try {
                //S3Object s3object = s3.getObject(new GetObjectRequest(bucket, key));
                //System.out.println("Content-Type: "  + s3object.getObjectMetadata().getContentType());
                TransferObserver observer = transferUtility.download(
                        bucket,
                        key,
                        file
                );
                System.out.println(file);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                //imageView.setImageBitmap(bitmap);

            } catch (AmazonServiceException ase) {
                System.out.println("Caught an AmazonServiceException, which" +
                        " means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Caught an AmazonClientException, which means"+
                        " the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }*/

            String server_ip = MiscFunctions.getInstance().getServerIP(this);
            String url = server_ip + "/academics/get_hw_image/" +
                    getIntent().getStringExtra("hw_id");
            this.setTitle("HW Image");

            /*ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    System.out.println("image downloaded");
                    progressDialog.hide();
                    progressDialog.dismiss();
                    imageView.setImageBitmap(response);
                }
            }, 0, 0, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Volley request failed",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Log.e("GetHWImage", "http Volley request failed!", volleyError);
                    volleyError.printStackTrace();
                }
            });
            com.classup.AppController.getInstance().addToRequestQueue(ir, "GetHWImage");*/
        }
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(getIntent().getStringExtra("sender").equals("select_class")) {
            m.add(0, 0, 0, "Upload").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case 0:
                final String server_ip = MiscFunctions.getInstance().getServerIP(this);
                final String teacher = SessionManager.getInstance().getLogged_in_user();
                final String url = server_ip + "/academics/create_hw/";
                final String tag = "UploadHW";
                final Intent intent = getIntent();
                final String date = intent.getStringExtra("date") + "/" +
                        intent.getStringExtra("month") + "/" + intent.getStringExtra("year");
                final String the_class = intent.getStringExtra("class") +
                        "-" + intent.getStringExtra("section");
                final String subject = intent.getStringExtra("subject");
                String prompt = "Are you sure to upload the homework for " + the_class;
                prompt += ", Subject: " + subject;
                prompt += ", Due date: " + date + "?";

                final android.app.AlertDialog.Builder builder =
                        new android.app.AlertDialog.Builder(a);
                builder.setMessage(prompt).setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progressDialog = new ProgressDialog(a);
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                                url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                //Disimissing the progress dialog
                                progressDialog.dismiss();
                                //Showing toast message of the response
                                Toast.makeText(a, s, Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                //Dismissing the progress dialog
                                progressDialog.dismiss();

                                //Showing toast
                                Toast toast = Toast.makeText(a, volleyError.getMessage(),
                                                Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws
                                    AuthFailureError {
                                //Creating parameters
                                Map<String, String> params = new Hashtable<>();
                                //Converting Bitmap to String
                                try {
                                    String image = getStringImage(bitmap1);
                                    params.put("hw_image", image);
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Error taking Picture. Please try again.",
                                            Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    Intent intent = new Intent(a, HWList.class);
                                    startActivity(intent);
                                }
                                //Adding parameters
                                String timeStamp =
                                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String imageFileName = teacher + "-" + the_class + "_" +
                                        subject + "_" + timeStamp + ".jpg";

                                params.put("image_name", imageFileName);
                                params.put("school_id",
                                        SessionManager.getInstance().getSchool_id());
                                params.put("teacher", teacher);
                                params.put("class", intent.getStringExtra("class"));
                                params.put("section", intent.getStringExtra("section"));
                                params.put("subject", subject);
                                params.put("d", intent.getStringExtra("date"));
                                params.put("m", intent.getStringExtra("month"));
                                params.put("y", intent.getStringExtra("year"));
                                params.put("due_date", date);

                                //returning parameters
                                return params;
                            }
                        };
                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        com.classup.AppController.getInstance().
                                addToRequestQueue(stringRequest, tag);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "HW Upload in Progress. " +
                                        "It will appeare Home Work list after a few minutes",
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        Intent intent1 = new Intent(getApplicationContext(), TeacherMenu.class);
                        intent1.putExtra("sender", "teacher_menu");
                        //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                //Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent1);
                        //finish();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();
                return super.onOptionsItemSelected(item);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        scaleGestureDetector.onTouchEvent(ev);
        return true;
    }

    public void usingSimpleImage(ImageView imageView) {
        ImageAttacher mAttacher = new ImageAttacher(imageView);
        ImageAttacher.MAX_ZOOM = 3.0f; // triple the current Size
        ImageAttacher.MIN_ZOOM = 0.5f; // Half the current Size
        MatrixChangeListener mMaListener = new MatrixChangeListener();
        mAttacher.setOnMatrixChangeListener(mMaListener);
        PhotoTapListener mPhotoTap = new PhotoTapListener();
        mAttacher.setOnPhotoTapListener(mPhotoTap);
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            scaleFactor = Math.max(1.00f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor);
            imageView.setImageMatrix(matrix);
            return true;
        }
    }

    private class PhotoTapListener implements OnPhotoTapListener {
        @Override
        public void onPhotoTap(View view, float x, float y) {
        }
    }

    private class MatrixChangeListener implements OnMatrixChangedListener {
        @Override
        public void onMatrixChanged(RectF rect) {

        }
    }

    /* 09/04/17 -
   courtsey https://forums.bignerdranch.com/t/i
   mageview-showing-in-landscape-but-not-portrait/7689/6
   */
    public static Bitmap scaleDownAndRotatePic(String path) {//you can provide file path here
        int orientation;
        try {
            if (path == null) {
                return null;
            }
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 0;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bm = BitmapFactory.decodeFile(path, o2);
            Bitmap bitmap = bm;

            ExifInterface exif = new ExifInterface(path);

            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Log.e("ExifInteface .........", "rotation =" + orientation);

            Log.e("orientation", "" + orientation);
            Matrix m = new Matrix();

            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                m.postRotate(180);
                //m.postScale((float) bm.getWidth(), (float) bm.getHeight());
                // if(m.preRotate(90)){
                Log.e("in orientation", "" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                m.postRotate(90);
                Log.e("in orientation", "" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                m.postRotate(270);
                Log.e("in orientation", "" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bitmap;
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
