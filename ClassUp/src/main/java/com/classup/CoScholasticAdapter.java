package com.classup;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by atulgupta on 28/09/17.
 */

public class CoScholasticAdapter  extends BaseAdapter{
    private Activity activity;
    private List<CoScholasticSource> grade_list;

    String blank = " ";

    public List<CoScholasticSource> getCoscholasticList() {
        return grade_list;
    }

    public CoScholasticAdapter(Activity activity, List<CoScholasticSource> grade_list) {
        super();
        this.activity = activity;
        this.grade_list = grade_list;
    }

    @Override
    public int getCount()   {
        return  grade_list.size();
    }

    @Override
    public Object getItem(int position) {
        return grade_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;

        if (view == null || view.getTag() == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.row_coscholastic, null);

            holder = new ViewHolder(view);

        } else {
            holder = (ViewHolder) view.getTag();

        }

        holder.full_name.setText(grade_list.get(position).getFull_name());
        holder.parent_name.setText(grade_list.get(position).getParent());

        holder.work.setTag(position);
        holder.art.setTag(position);
        holder.health.setTag(position);
        holder.dscpln.setTag(position);

        holder.work_A.setTag(position);
        holder.work_B.setTag(position);
        holder.work_C.setTag(position);
        holder.art_A.setTag(position);
        holder.art_B.setTag(position);
        holder.art_C.setTag(position);
        holder.health_A.setTag(position);
        holder.health_B.setTag(position);
        holder.health_C.setTag(position);
        holder.dscpln_A.setTag(position);
        holder.dscpln_B.setTag(position);
        holder.dscpln_C.setTag(position);

        String grade_art_ed = grade_list.get(position).getGrade_art_ed();
        switch (grade_art_ed) {
            case "A":
                holder.art_A.setChecked(true);
                break;

            case "B":
                holder.art_B.setChecked(true);
                break;

            case "C":
                holder.art_C.setChecked(true);
                break;
        }

        String grade_worked = grade_list.get(position).getGrade_work_ed();
        switch (grade_worked)   {
            case "A":
                holder.work_A.setChecked(true);
                break;

            case "B":
                holder.work_B.setChecked(true);
                break;

            case "C":
                holder.work_C.setChecked(true);
                break;
        }

        String grade_health = grade_list.get(position).getGrade_health();
        switch(grade_health)    {
            case "A":
                holder.health_A.setChecked(true);
                break;

            case "B":
                holder.health_B.setChecked(true);
                break;

            case "C":
                holder.health_C.setChecked(true);
                break;
        }

        String grade_dscpln = grade_list.get(position).getGrade_dscpln();
        switch (grade_dscpln)   {
            case "A":
                holder.dscpln_A.setChecked(true);
                break;

            case "B":
                holder.dscpln_B.setChecked(true);
                break;

            case "C":
                holder.dscpln_C.setChecked(true);
                break;
        }
        holder.health.setOnCheckedChangeListener(listener);
        holder.art.setOnCheckedChangeListener(listener);
        holder.work.setOnCheckedChangeListener(listener);
        holder.dscpln.setOnCheckedChangeListener(listener);

        holder.remarks.setText(grade_list.get(position).getRemarks_class_teacher());

        holder.remarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                grade_list.get(position).setRemarks_class_teacher(
                    holder.remarks.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (grade_list.get(position).getTerm().equals("term1")) {
            holder.promoted.setEnabled(false);
            holder.promoted.setVisibility(View.INVISIBLE);
        }
        else
            holder.promoted.setText(grade_list.get(position).getPromoted_to_class());

        return view;
    }

    RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            Object object = radioGroup.getTag();
            int position = (int)object;
            switch (i)  {
                case R.id.art_A:
                    grade_list.get(position).setGrade_art_ed("A");
                    break;
                case R.id.art_B:
                    grade_list.get(position).setGrade_art_ed("B");
                    break;
                case R.id.art_C:
                    grade_list.get(position).setGrade_art_ed("C");
                    break;

                case R.id.work_A:
                    grade_list.get(position).setGrade_work_ed("A");
                    break;
                case R.id.work_B:
                    grade_list.get(position).setGrade_work_ed("B");
                    break;
                case R.id.work_C:
                    grade_list.get(position).setGrade_work_ed("C");
                    break;

                case R.id.health_A:
                    grade_list.get(position).setGrade_health("A");
                    break;
                case R.id.health_B:
                    grade_list.get(position).setGrade_health("B");
                    break;
                case R.id.health_C:
                    grade_list.get(position).setGrade_health("C");
                    break;

                case R.id.dscpln_A:
                    grade_list.get(position).setGrade_dscpln("A");
                    break;
                case R.id.dscpln_B:
                    grade_list.get(position).setGrade_dscpln("B");
                    break;
                case R.id.dscpln_C:
                    grade_list.get(position).setGrade_dscpln("C");
                    break;
            }
        }
    };

    static class ViewHolder {
        TextView full_name;
        TextView parent_name;

        RadioGroup work;
        RadioGroup art;
        RadioGroup health;
        RadioGroup dscpln;

        RadioButton work_A;
        RadioButton work_B;
        RadioButton work_C;

        RadioButton art_A;
        RadioButton art_B;
        RadioButton art_C;

        RadioButton health_A;
        RadioButton health_B;
        RadioButton health_C;

        RadioButton dscpln_A;
        RadioButton dscpln_B;
        RadioButton dscpln_C;

        EditText remarks;
        EditText promoted;
        static int i = 0;

        public ViewHolder(View view)    {
            this.full_name = view.findViewById(R.id.lbl_student);
            this.parent_name = view.findViewById(R.id.txt_parent);

            this.work = view.findViewById(R.id.radioGroup_work);
            this.art = view.findViewById(R.id.radioGroup_art);
            this.health = view.findViewById(R.id.radioGroup_health);
            this.dscpln = view.findViewById(R.id.radioGroup_dscpln);

            this.work.clearCheck();
            this.work_A = view.findViewById(R.id.work_A);
            this.work_B = view.findViewById(R.id.work_B);
            this.work_C = view.findViewById(R.id.work_C);

            this.art_A = view.findViewById(R.id.art_A);
            this.art_B = view.findViewById(R.id.art_B);
            this.art_C = view.findViewById(R.id.art_C);

            this.health_A = view.findViewById(R.id.health_A);
            this.health_B = view.findViewById(R.id.health_B);
            this.health_C = view.findViewById(R.id.health_C);

            this.dscpln_A = view.findViewById(R.id.dscpln_A);
            this.dscpln_B = view.findViewById(R.id.dscpln_B);
            this.dscpln_C = view.findViewById(R.id.dscpln_C);

            this.remarks = view.findViewById(R.id.edit_ct_remarks);
            this.promoted = view.findViewById(R.id.txt_promoted_to);
        }
    }

}
