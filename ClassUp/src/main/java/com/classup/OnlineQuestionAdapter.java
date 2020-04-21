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
            switch (i)  {
                case R.id.option_A:
                    student_answers.get(position).setOption_marked("A");
                    break;
                case R.id.option_B:
                    student_answers.get(position).setOption_marked("B");
                    break;
                case R.id.option_C:
                    student_answers.get(position).setOption_marked("C");
                    break;

                case R.id.option_D:
                    student_answers.get(position).setOption_marked("D");
                    break;
            }
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
