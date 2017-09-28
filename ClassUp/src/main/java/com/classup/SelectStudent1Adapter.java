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
 * Created by atulgupta on 22/02/17.
 */

public class SelectStudent1Adapter extends ArrayAdapter {
    private ArrayList<AttendanceListSource> student_list = new ArrayList<>();

    public ArrayList<AttendanceListSource> getStudent_list()    {
        return this.student_list;
    }

    public SelectStudent1Adapter(Context context, int textViewResourceId,
                                 ArrayList<AttendanceListSource> student_list)   {
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
            convertView = inflater.inflate(R.layout.row_select_student, null);
        }
        // we don't want to show the checkbox
        final CheckedTextView textView =
                (CheckedTextView) convertView.findViewById(R.id.lbl_roll_no);
        //textView.setVisibility(View.INVISIBLE);

        TextView student_name = (TextView) convertView.findViewById(R.id.lbl_roll_no);
        student_name.setText(student_list.get(position).getFull_name());
        return convertView;
    }
}
