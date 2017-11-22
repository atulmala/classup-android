package com.classup;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Created by atulgupta on 22/11/17.
 */


public class ArrangementsAdapter extends BaseAdapter {
    private Activity activity;
    private List<ArrangementSource> arrangement_ist;

    public ArrangementsAdapter(Activity activity, List<ArrangementSource> arrangement_ist) {
        this.activity = activity;
        this.arrangement_ist = arrangement_ist;
    }

    @Override
    public int getCount()   {return arrangement_ist.size();}
    @Override
    public Object getItem(int position) {return arrangement_ist.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_arrangements, null);
        }

        TextView period = convertView.findViewById(R.id.txt_period);
        period.setText(arrangement_ist.get(position).getPeriod());

        TextView class_sec = convertView.findViewById(R.id.txt_class);
        String the_class = arrangement_ist.get(position).getThe_class() + "-" +
            arrangement_ist.get(position).getSection();
        class_sec.setText(the_class);

        return convertView;
    }
}
