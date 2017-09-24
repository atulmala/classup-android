package com.classup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.NumberPicker;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParentCommunication extends AppCompatActivity {
    String student_id;
    String student_name;
    private NumberPicker cat_picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_communication);

        // get the id and name of the student
        student_id = getIntent().getStringExtra("student_id");
        student_name = getIntent().getStringExtra("student_name");
        cat_picker = (NumberPicker)findViewById(R.id.pick_comm_category);

        String category_url =
                MiscFunctions.getInstance().getServerIP(getApplicationContext()) +
                "/parents/retrieve_categories/";
        setupPicker(cat_picker, category_url, "category", "Category Extraction");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }

    public void sendParentMessage(View view)    {
        final String tag = "SubmitParentsCommunication";
        final String[] category_list = cat_picker.getDisplayedValues();
        ActionEditText editText = (ActionEditText)findViewById(R.id.txt_parent_message_content);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setHorizontallyScrolling(false);

        editText.setLines(3);
        //editText.setMaxLines(Integer.MAX_VALUE);
        String communication_text = editText.getText().toString();
        if (communication_text.equals(""))    {
            Toast.makeText(getApplicationContext(), "Message is empty!",
                    Toast.LENGTH_SHORT).show();

        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("communication_text", communication_text);
                jsonObject.put("student_id", student_id);
                jsonObject.put("category", category_list[cat_picker.getValue()]);
                System.out.println(jsonObject);
            }
            catch (JSONException je)  {
                System.out.println("unable to create json for subjects to be deleted");
                je.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException ae) {
                System.out.println("array out of bounds exception");
                ae.printStackTrace();
            }
            String url =
                    MiscFunctions.getInstance().getServerIP(getApplicationContext()) +
                    "/parents/submit_parents_communication/";

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(tag, response.toString());

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(tag, "Error: " + error.getMessage());
                    if (error instanceof TimeoutError ||
                            error instanceof NoConnectionError) {
                        Toast.makeText(getApplicationContext(),
                                "Slow network connection or No internet connectivity",
                                Toast.LENGTH_LONG).show();
                    }  else if (error instanceof ServerError) {
                        Toast.makeText(getApplicationContext(),
                                "Slow network connection or No internet connectivity",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(getApplicationContext(),
                                "Slow network connection or No internet connectivity",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        //TODO
                    }
                }
            });
            com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
            Toast.makeText(getApplicationContext(),
                    "Your communication has been sent. If needed, school authorities will " +
                            "contact you.", Toast.LENGTH_SHORT).show();
            // 12/09/17 - Now we are building the custom
            // Analysis via AWS
            try {
                AnalyticsEvent event =
                        SessionManager.getInstance().analytics.getEventClient().
                                createEvent("Parent Communication");
                event.addAttribute("user", SessionManager.getInstance().
                        getLogged_in_user());
                // we also capture the communication category
                event.addAttribute("category", category_list[cat_picker.getValue()]);
                SessionManager.getInstance().analytics.
                        getEventClient().
                        recordEvent(event);
            } catch (NullPointerException exception)    {
                System.out.println("flopped in creating " +
                        "analytics Parent Communication");
            } catch (Exception exception)   {
                System.out.println("flopped in " +
                        "creating analytics Parent Communication");
            }
            Intent intent = new Intent("com.classup.ParentsMenu");
            intent.putExtra("student_id", student_id);
            intent.putExtra("student_name", student_name);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


    }
    public void setupPicker(final NumberPicker picker, String url,
                            final String item_to_extract, final String tag) {
        final ArrayList<String> item_list = new ArrayList<String>();

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
                        //picker_contents =
                        try {
                            picker.setMaxValue(picker_contents.length - 1);
                            picker.setDisplayedValues(picker_contents);
                        }
                        catch (ArrayIndexOutOfBoundsException e)    {
                            System.out.println("there seems to be no data for " + tag);
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "There seems to be some problem at server end" +
                                            "Please try after sometime", Toast.LENGTH_LONG).show();
                            startActivity(new Intent("com.classup.ParentsMenu"));
                        }
                        catch (Exception e) {
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
                        System.out.println("inside volley error handler");
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
    }
}
