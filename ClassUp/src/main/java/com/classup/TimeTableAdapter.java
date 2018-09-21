package com.classup;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Created by atulgupta on 08/04/18.
 */

public class TimeTableAdapter extends BaseAdapter {
    private Activity activity;
    private List<TTSource> ttSourceList;
    private String for_whom;
    private String d;

    public TimeTableAdapter(Activity activity, List<TTSource> ttSourceList,
                            String for_whom) {
        super();
        this.activity = activity;
        this.ttSourceList = ttSourceList;
        this.for_whom = for_whom;
        this.d = d;
    }

    @Override
    public int getCount()   {return ttSourceList.size();}
    @Override
    public Object getItem(int position) {return ttSourceList.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_time_table, null);
        }

        TextView period_details = convertView.findViewById(R.id.txt_time_table);
        String details = "Period # "+ ttSourceList.get(position).getPeriod() + ":  ";
        switch (for_whom)   {
            case "teacher":
                if (ttSourceList.get(position).getPeriod().equals(""))
                    details = "";
                details += ttSourceList.get(position).getThe_class() + "-";
                details += ttSourceList.get(position).getSection() + "     ";
                details += ttSourceList.get(position).getSubject();
                if(ttSourceList.get(position).getPeriod().equals(""))
                    details = details.substring(0, details.length()-6);
                break;
            case "student":
                details += ttSourceList.get(position).getSubject();
                break;
        }
        period_details.setText(details);


        return convertView;
    }

}
