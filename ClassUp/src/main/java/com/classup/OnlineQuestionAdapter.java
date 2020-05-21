package com.classup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class OnlineQuestionAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<OnlineQuestionSource> question_list;
    ArrayList<StudentAnswers> student_answers;
    String student_id;

    public OnlineQuestionAdapter(Activity activity, ArrayList<OnlineQuestionSource> question_list,
                                 ArrayList<StudentAnswers> student_answers) {
        this.activity = activity;
        this.question_list = question_list;
        this.student_answers = student_answers;
    }

    @Override
    public int getCount() {
        return question_list.size();
    }

    @Override
    public Object getItem(int position) {
        return question_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        final ViewHolder holder;

        if(view == null || view.getTag() == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_online_question, null);
            holder = new ViewHolder(view);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        holder.q_no.setTag(position);
        holder.question.setTag(position);
        holder.options.setTag(position);
        holder.option_A.setTag(position);
        holder.option_B.setTag(position);
        holder.option_C.setTag(position);
        holder.option_D.setTag(position);

        holder.q_no.setText(question_list.get(position).getQ_no());
        holder.option_A.setText("A. " + question_list.get(position).getOption_A());

        holder.question.setText(question_list.get(position).getQuestion());

        holder.option_B.setText("B. " + question_list.get(position).getOption_B());

        holder.option_C.setText("C. " + question_list.get(position).getOption_C());

        holder.option_D.setText("D. " + question_list.get(position).getOption_D());

        String option_marked = student_answers.get(position).getOption_marked();
        switch (option_marked)  {
            case "A":
                holder.option_A.setChecked(true);
                break;
            case "B":
                holder.option_B.setChecked(true);
                break;
            case "C":
                holder.option_C.setChecked(true);
                break;
            case "D":
                holder.option_D.setChecked(true);
                break;
        }

        holder.options.setOnCheckedChangeListener(listener);

        return view;
    }
    RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            Object object = radioGroup.getTag();
            int position = (int)object;
            String student_id = student_answers.get(position).getStudent_id();
            String question_id = student_answers.get(position).getQuestion_id();
            String answer_marked = "X";
            switch (i)  {
                case R.id.option_A:
                    student_answers.get(position).setOption_marked("A");
                    answer_marked = "A";
                    break;
                case R.id.option_B:
                    student_answers.get(position).setOption_marked("B");
                    answer_marked = "B";
                    break;
                case R.id.option_C:
                    student_answers.get(position).setOption_marked("C");
                    answer_marked = "C";
                    break;
                case R.id.option_D:
                    student_answers.get(position).setOption_marked("D");
                    answer_marked = "D";
                    break;
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("student_id", student_id);
                jsonObject.put("question_id", question_id);
                jsonObject.put("answer_marked", answer_marked);
            } catch (JSONException je) {
                System.out.println("unable to create json object for marking answer online test ");
                je.printStackTrace();
            }
            String server_ip = MiscFunctions.getInstance().getServerIP(activity);
            String url1 = server_ip + "/online_test/mark_answer/";
            JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                (Request.Method.POST, url1, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError ||
                            error instanceof NoConnectionError) {
                            if (!MiscFunctions.getInstance().checkConnection
                                (activity)) {
                                Toast.makeText(activity,
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(activity,
                                    "Some problem at server end, please try after some time",
                                    Toast.LENGTH_LONG).show();
                            }
                        } else if (error instanceof ServerError) {
                            Toast.makeText(activity,
                                "User does not exist. Please contact " +
                                    "ClassUp Support at support@classup.in",
                                Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(activity,
                                "Network error, please try later",
                                Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        System.out.println("inside volley error handler(LoginActivity)");
                        // TODO Auto-generated method stub
                    }
                });
            int socketTimeout = 300000;//5 minutes
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsObjRequest1.setRetryPolicy(policy);
            com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);
        }
    };

    static class ViewHolder {
        TextView q_no;
        TextView question;
        RadioGroup options;
        RadioButton option_A;
        RadioButton option_B;
        RadioButton option_C;
        RadioButton option_D;

        public ViewHolder(View view)    {
            this.q_no = view.findViewById(R.id.q_no);
            this.question = view.findViewById(R.id.question);
            this.options = view.findViewById(R.id.options);
            this.option_A = view.findViewById(R.id.option_A);
            this.option_B = view.findViewById(R.id.option_B);
            this.option_C = view.findViewById(R.id.option_C);
            this.option_D = view.findViewById(R.id.option_D);
        }
    }
}
