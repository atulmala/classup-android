package com.classup;

/**
 * Created by atulgupta on 12/01/16.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atulgupta on 07/10/15.
 */
public class SelectStudentAdapter extends BaseAdapter {
    private ArrayList<AttendanceListSource> student_list;
    public ArrayList<String> selected_students = new ArrayList<>();

    String server_ip;
    Context context;

   public List<AttendanceListSource> getStudent_list() {
        return student_list;
    }

    public SelectStudentAdapter(Context context, ArrayList<AttendanceListSource> student_list,
                                ArrayList<String > selected_students) {
        super();
        this.student_list = student_list;
        this.selected_students = selected_students;

        this.context = context;
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
                    (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_select_student1, null);
        }

        final CheckBox chk = (CheckBox)convertView.findViewById(R.id.chk_select);

        TextView student_name = (TextView) convertView.findViewById(R.id.lbl_roll_no);
        student_name.setText(student_list.get(position).getFull_name());

        TextView roll_no = (TextView)convertView.findViewById(R.id.roll_no);
        roll_no.setVisibility(View.INVISIBLE);
        roll_no.setText(student_list.get(position).getRoll_number());

        TextView parent_name = (TextView)convertView.findViewById(R.id.parent_name);
        parent_name.setText(student_list.get(position).getParent_name());

        if (selected_students.contains(student_list.get(position).getId())) {
            chk.setChecked(true);
        }
        else    {
            chk.setChecked(false);
        }

        chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chk.setChecked(!chk.isChecked());
                if (!chk.isChecked()) {
                    selected_students.add(student_list.get(position).getId());
                    System.out.println(selected_students);
                    notifyDataSetChanged();
                } else {
                    if (selected_students.contains(student_list.get(position).getId())) {
                        selected_students.remove(student_list.get(position).getId());
                    }
                    notifyDataSetChanged();
                }
            }
        });

        return convertView;
    }
}

