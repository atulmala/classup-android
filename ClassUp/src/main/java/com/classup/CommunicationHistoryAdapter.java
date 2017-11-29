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
 * Created by atulgupta on 15/10/16.
 */

public class CommunicationHistoryAdapter extends BaseAdapter {
    private Activity activity;
    private List<CommunicationSource> communicationSourceList;

    public CommunicationHistoryAdapter(Activity activity,
                                       List<CommunicationSource> communicationSourceList) {
        super();
        this.activity = activity;
        this.communicationSourceList = communicationSourceList;
    }

    @Override
    public int getCount()   {return communicationSourceList.size();}
    @Override
    public Object getItem(int position) {return communicationSourceList.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_communication_history, null);
        }

        TextView date = convertView.findViewById(R.id.communication_date);
        date.setText(communicationSourceList.get(position).getDate());

        TextView communication_date = convertView.findViewById(R.id.communication_date);
        String the_date = communicationSourceList.get(position).getDate();
        communication_date.setText(the_date);
        communication_date.setTextColor(Color.LTGRAY);

        EditText communication_text = convertView.findViewById(R.id.communiction_text);
        communication_text.setEnabled(false);
        //communication_text.setInputType(InputType.TYPE_NULL);
        communication_text.setTextColor(Color.DKGRAY);
        String the_text = communicationSourceList.get(position).getText();
        communication_text.setText(the_text);

        return convertView;
    }
}

