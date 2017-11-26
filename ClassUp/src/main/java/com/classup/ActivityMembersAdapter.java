package com.classup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by atulgupta on 25/11/17.
 */

public class ActivityMembersAdapter extends BaseAdapter {
    private Activity activity;
    private List<AttendanceListSource> members_list;

    public ActivityMembersAdapter(Activity activity, List<AttendanceListSource> members_list) {
        this.activity = activity;
        this.members_list = members_list;
    }

    @Override
    public int getCount()   {return members_list.size();}
    @Override
    public Object getItem(int position) {return members_list.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_activity_members, null);
        }

        TextView name = convertView.findViewById(R.id.txt_student_name);
        name.setText(members_list.get(position).getFull_name());

        return convertView;
    }
}
