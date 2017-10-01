package com.classup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by atulgupta on 10/04/17.
 */

class HWListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HWListSource> hwListSources = new ArrayList<>();
    public HWListAdapter(Activity a, ArrayList<HWListSource> hwListSources)     {
        super();
        this.activity = a;
        this.hwListSources = hwListSources;

    }

    @Override
    public int getCount()   {
        return hwListSources.size();
    }
    @Override
    public Object getItem(int position) {return hwListSources.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_hw_list, null);
        }

        TextView the_class = convertView.findViewById(R.id.class_sec);
        the_class.setText(hwListSources.get(position).getThe_class());

        TextView subject = convertView.findViewById(R.id.hw_subject);
        subject.setText(hwListSources.get(position).getSubject());

        TextView due_date = convertView.findViewById(R.id.hw_date);
        due_date.setText(hwListSources.get(position).getDue_date());

        return convertView;
    }

}
