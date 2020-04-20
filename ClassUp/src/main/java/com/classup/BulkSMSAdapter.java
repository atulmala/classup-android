package com.classup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by atulgupta on 14/02/17.
 */

public class BulkSMSAdapter extends ArrayAdapter{
    private ArrayList<ClassListSource> class_list;
    public ArrayList<String> selection = new ArrayList<>();

    String server_ip;
    Intent intent;

    public List<ClassListSource> getClass_list() {
        return class_list;
    }

    public BulkSMSAdapter(Context context, int textViewResourceId,
                                ArrayList<ClassListSource> class_list) {
        super(context, textViewResourceId, class_list);
        this.class_list = class_list;
    }

    @Override
    public int getCount()   {
        return  getClass_list().size();
    }

    @Override
    public Object getItem(int position) {
        return class_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_bulk_sms, null);
        }
        final CheckedTextView textView =
                convertView.findViewById(R.id.ot_class);

        TextView the_class = convertView.findViewById(R.id.ot_class);
        the_class.setText(class_list.get(position).getThe_class());
        if (selection.contains(class_list.get(position).getThe_class()))   {
            textView.setChecked(true);
        }
        else
            textView.setChecked(false);

        return convertView;
    }
}

