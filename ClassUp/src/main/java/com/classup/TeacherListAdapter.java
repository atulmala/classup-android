package com.classup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by atulgupta on 28/02/17.
 */

public class TeacherListAdapter extends ArrayAdapter {
    private ArrayList<TeacherListSource> teacher_list = new ArrayList<>();

    public TeacherListAdapter(Context context, int textViewResourceId,
                              ArrayList<TeacherListSource> teacher_list)   {
        super(context, textViewResourceId, teacher_list);
        this.teacher_list = teacher_list;
    }

    @Override
    public int getCount()   {
        return  teacher_list.size();
    }

    @Override
    public Object getItem(int position) {
        return teacher_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_select_student, null);
        }
        // we don't want to show the checkbox
        final CheckedTextView textView =
                (CheckedTextView) convertView.findViewById(R.id.lbl_roll_no);

        TextView student_name = (TextView) convertView.findViewById(R.id.lbl_roll_no);
        student_name.setText(teacher_list.get(position).getFull_name());
        return convertView;
    }
}
