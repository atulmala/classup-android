package com.classup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by atulgupta on 30/11/17.
 */

public class MessageReceiverAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<RecepientMessageSource> message_list;

    public MessageReceiverAdapter(Activity activity, ArrayList<RecepientMessageSource> message_list) {
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_teacher_message_receivers, null);
        }

        TextView student = convertView.findViewById(R.id.txt_stu_name);
        student.setText(message_list.get(position).getStudent());
        TextView message = convertView.findViewById(R.id.txt_message_recievers);
        message.setText(message_list.get(position).getMessage());
        return convertView;
    }
}
