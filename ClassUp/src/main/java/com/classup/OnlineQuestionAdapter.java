package com.classup;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class OnlineQuestionAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<OnlineQuestionSource> question_list = new ArrayList<>();

    public OnlineQuestionAdapter(Activity activity, ArrayList<OnlineQuestionSource> question_list) {
        this.activity = activity;
        this.question_list = question_list;


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
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_online_question, null);
        }

        TextView q_no = convertView.findViewById(R.id.q_no);
        q_no.setText(question_list.get(position).getQ_no());

        TextView question = convertView.findViewById(R.id.question);
        question.setText(question_list.get(position).getQuestion());

        TextView option_A = convertView.findViewById(R.id.option_A);
        option_A.setText("A. " + question_list.get(position).getOption_A());

        TextView option_B = convertView.findViewById(R.id.option_B);
        option_B.setText("B. " + question_list.get(position).getOption_B());

        TextView option_C = convertView.findViewById(R.id.option_C);
        option_C.setText("C. " + question_list.get(position).getOption_C());

        TextView option_D = convertView.findViewById(R.id.option_D);
        option_D.setText("D. " + question_list.get(position).getOption_D());

        return convertView;
    }
}
