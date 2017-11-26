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

public class ActivityGroupAdapter extends BaseAdapter {
    private Activity activity;
    private List<ActivityGroupSource> activity_groups_list;

    public ActivityGroupAdapter(Activity activity, List<ActivityGroupSource> activity_groups_list) {
        this.activity = activity;
        this.activity_groups_list = activity_groups_list;
    }

    @Override
    public int getCount()   {return activity_groups_list.size();}
    @Override
    public Object getItem(int position) {return activity_groups_list.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_activity_group, null);
            //convertView.setLongClickable(true);
        }

        TextView group = convertView.findViewById(R.id.txt_group_name);
        group.setText(activity_groups_list.get(position).getActivity_group());

        TextView incharge = convertView.findViewById(R.id.txt_incharge);
        incharge.setText(activity_groups_list.get(position).getIncharge());

        return convertView;
    }
}
