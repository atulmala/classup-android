package com.classup;

/**
 * Created by atulgupta on 12/01/16.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

/**
 * Created by atulgupta on 07/10/15.
 */
public class SelectStudentAdapter extends ArrayAdapter {
    private ArrayList<AttendanceListSource> student_list;
    public ArrayList<String> selected_students = new ArrayList<>();

    String server_ip;
    Intent intent;

    public List<AttendanceListSource> getStudent_list() {
        return student_list;
    }

    public SelectStudentAdapter(Context context, int textViewResourceId,
                              ArrayList<AttendanceListSource> student_list) {
        super(context, textViewResourceId, student_list);
        this.student_list = student_list;
    }


    @Override
    public int getCount()   {
        return  getStudent_list().size();
    }

    @Override
    public Object getItem(int position) {
        return student_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.select_student_row, null);
        }
        final CheckedTextView textView =
                (CheckedTextView) convertView.findViewById(R.id.student_name);

        TextView student_name = (TextView) convertView.findViewById(R.id.student_name);
        student_name.setText(student_list.get(position).getName_rollno());
        if (selected_students.contains(student_list.get(position).getId()))   {
            textView.setChecked(true);
        }
        else
            textView.setChecked(false);

        return convertView;
    }
}

