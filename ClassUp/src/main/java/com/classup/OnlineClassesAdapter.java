package com.classup;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class OnlineClassesAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<OnlineClassSource> lecture_list;

    public OnlineClassesAdapter(Activity activity, ArrayList<OnlineClassSource> lecture_list) {
        this.activity = activity;
        this.lecture_list = lecture_list;
    }

    @Override
    public int getCount()   {return lecture_list.size();}
    @Override
    public Object getItem(int position) {return lecture_list.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_online_class, null);
        }

        TextView date = convertView.findViewById(R.id.txt_date);
        date.setText(lecture_list.get(position).getDate());
        date.setTextColor(Color.BLUE);

        TextView txt_class = convertView.findViewById(R.id.txt_class);
        String the_class = lecture_list.get(position).getThe_class();
        txt_class.setText(the_class);

        TextView subject = convertView.findViewById(R.id.txt_subject);
        subject.setText(lecture_list.get(position).getSubject());

        TextView teacher = convertView.findViewById(R.id.txt_teacher);
        teacher.setText(lecture_list.get(position).getTeacher());

        TextView topic = convertView.findViewById(R.id.txt_topic);
        topic.setText(lecture_list.get(position).getTopic());

        TextView video_link = convertView.findViewById(R.id.txt_video_link);
        video_link.setText(lecture_list.get(position).getYoutube_link());

        TextView doc_link = convertView.findViewById(R.id.txt_doc_link);
        doc_link.setText(lecture_list.get(position).getPdf_link());


        return convertView;
    }
}
