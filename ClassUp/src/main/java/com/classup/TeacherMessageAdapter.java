package com.classup;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by atulgupta on 29/11/17.
 */

public class TeacherMessageAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<MessageSource> message_list;

    public TeacherMessageAdapter(Activity activity, ArrayList<MessageSource> message_list) {
        this.activity = activity;
        this.message_list = message_list;
    }

    @Override
    public int getCount()   {return message_list.size();}
    @Override
    public Object getItem(int position) {return message_list.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_teacher_message_record, null);
        }

        TextView date = convertView.findViewById(R.id.txt_date);
        date.setText(message_list.get(position).getDate());
        date.setTextColor(Color.BLUE);

        TextView sent_to = convertView.findViewById(R.id.txt_sent_to);
        sent_to.setText(message_list.get(position).getSent_to());

        EditText message = convertView.findViewById(R.id.txt_teacher_message);
        message.setEnabled(false);
        message.setTextColor(Color.DKGRAY);
        String message_text = message_list.get(position).getMessage();
        message.setText(message_text);

        return convertView;
    }
}
