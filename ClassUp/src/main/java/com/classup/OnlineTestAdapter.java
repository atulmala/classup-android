package com.classup;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class OnlineTestAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<OnlineTestSource> test_list;

    public OnlineTestAdapter(Activity activity, ArrayList<OnlineTestSource> test_list) {
        this.activity = activity;
        this.test_list = test_list;
    }

    @Override
    public int getCount()   {return test_list.size();}
    @Override
    public Object getItem(int position) {return test_list.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_online_class, null);
        }

        TextView date = convertView.findViewById(R.id.test_date);
        date.setText(test_list.get(position).getDate());
        date.setTextColor(Color.BLUE);

        TextView txt_class = convertView.findViewById(R.id.the_class);
        String the_class = test_list.get(position).getThe_class();
        txt_class.setText(the_class);

        TextView subject = convertView.findViewById(R.id.subject);
        subject.setText(test_list.get(position).getSubject());

        return convertView;
    }
}
