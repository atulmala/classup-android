package com.classup;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by atulgupta on 29/11/17.
 */

public class ImageVideoListAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<ImageVideoSource> image_list;

    public ImageVideoListAdapter(Activity activity, ArrayList<ImageVideoSource> image_list) {
        this.activity = activity;
        this.image_list = image_list;
    }

    @Override
    public int getCount()   {return image_list.size();}
    @Override
    public Object getItem(int position) {return image_list.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_image_video, null);
        }

        TextView date = convertView.findViewById(R.id.txt_date);
        date.setText(image_list.get(position).getDate());
        date.setTextColor(Color.BLUE);

        TextView sent_to = convertView.findViewById(R.id.txt_sent_to);
        String the_class = image_list.get(position).getThe_class();
        String section = image_list.get(position).getSection();
        String class_sec = the_class + "-" + section;
        sent_to.setText(class_sec);

        TextView type = convertView.findViewById(R.id.txt_type);
        type.setText(image_list.get(position).getType());

        if(image_list.get(position).getType().equals("image"))
            type.setTextColor(Color.MAGENTA);
        else
            type.setTextColor(Color.CYAN);

        TextView short_link = convertView.findViewById(R.id.short_link);
        short_link.setText(image_list.get(position).getShort_link());

        TextView description = convertView.findViewById(R.id.txt_description);
        description.setEnabled(false);
        description.setTextColor(Color.DKGRAY);
        String desc_text = image_list.get(position).getDescription();
//        desc_text += ". " + image_list.get(position).getShort_link();
        description.setText(desc_text);

        return convertView;
    }
}
