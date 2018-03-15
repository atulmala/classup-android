package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class CompletedTestsActivityFragment extends Fragment {
    String tag = "Completed Tests";
    String server_ip;

    public CompletedTestsActivityFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_completed_tests, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        final Context c = this.getContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);

        String logged_in_user = SessionManager.getInstance().getLogged_in_user();
        // as we are using singleton pattern to get the logged in user, sometimes the method
        // call returns a blank string. In this case we will retry for 20 times and if not
        // successful even after then we will ask the user to log in again
        int i = 0;
        while (logged_in_user.equals("")) {
            logged_in_user = SessionManager.getInstance().getLogged_in_user();
            if (i++ == 20)  {
                Toast.makeText(c, "There seems to be some problem with network. Please re-login",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent("com.classup.LoginActivity"));
            }
        }
        final String url =  server_ip + "/academics/completed_test_list/" +
                logged_in_user + "/?format=json";

        final ArrayList<TestListSource> completed_test_list = new ArrayList<>();
        ListView listView = (ListView)getActivity().findViewById(R.id.completed_test_list);

        final CompletedTestListAdapter adapter = new CompletedTestListAdapter(getActivity(),
                completed_test_list);

        listView.setAdapter(adapter);

        final Intent intent = new Intent(this.getActivity(), MarksEntry.class);

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener()    {
            public void onItemClick(AdapterView<?> view, View v, int position, long id) {
                intent.putExtra("test_id", completed_test_list.get(position).getId());
                intent.putExtra("test_type", completed_test_list.get(position).getTest_type());
                intent.putExtra("class", completed_test_list.get(position).getThe_class());
                intent.putExtra("section", completed_test_list.get(position).getSection());
                intent.putExtra("subject", completed_test_list.get(position).getSubject());
                intent.putExtra("higher_class",
                    completed_test_list.get(position).getWhether_higher_class());

                if (completed_test_list.get(position).getMax_marks().equals("Grade Based"))
                    intent.putExtra("grade_based", true);
                else
                    intent.putExtra("grade_based", false);

                startActivity(intent);
            }
        });

        final ProgressDialog progressDialog = new ProgressDialog(c);
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

                                String date = jo.getString("date_conducted");
                                String yy = date.substring(0, 4);
                                String month = date.substring(5, 7);
                                String dd = date.substring(8, 10);
                                String ddmmyyyy = dd + "/" + month + "/" + yy;

                                String the_class = jo.getString("the_class");

                                // 24/12/2017 - need to understand whether the test is for higher
                                // class, ie, XI or XII
                                String higher_class = "false";
                                if ((the_class.equals("XI")) || (the_class.equals("XII")))
                                    higher_class = "true";

                                String section = jo.getString("section");
                                String subject = jo.getString("subject");
                                String max_marks = jo.getString("max_marks");
                                String grade_based = jo.getString("grade_based");

                                if (grade_based.equals("true")) {
                                    max_marks = "Grade Based";
                                }
                                else    {
                                    String mm = max_marks;
                                    // remove the decimal and last two 0s
                                    String mm1 = mm.substring(0, mm.length()-3);
                                    max_marks = mm1;
                                }

                                // get the id of the test
                                String id = jo.getString("id");
                                // syllabus for the test
                                String syllabus = jo.getString("syllabus");

                                // 23/09/2017 - get the test type (Unit or Term)
                                String test_type = jo.getString("test_type");

                                // put all the above details into the adapter
                                completed_test_list.add(new TestListSource(ddmmyyyy, the_class,
                                    section, subject, max_marks, id,
                                    syllabus, test_type, higher_class));
                                adapter.notifyDataSetChanged();
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                        "while trying to fetch the list of students");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the list of students");
                                e.printStackTrace();
                            }
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
                            Toast.makeText(c,
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(c,
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(c,
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