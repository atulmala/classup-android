package com.classup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by root on 9/21/15.
 */
public class PendingTestListAdapter extends BaseAdapter {
    private Activity activity;
    private List<TestListSource> pending_test_list;

    public PendingTestListAdapter(Activity activity, List<TestListSource> pending_test_list) {

        super();
        this.activity = activity;
        this.pending_test_list = pending_test_list;
    }

    @Override
    public int getCount()   {
        return pending_test_list.size();
    }

    public Object getItem(int position) {
        return pending_test_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.test_list_row, null);
        }

        final View convertViewRef = convertView;
        TextView date_col = (TextView)convertView.findViewById(R.id.txt_test_date);
        TextView class_col = (TextView)convertView.findViewById(R.id.txt_class);
        TextView section_col = (TextView)convertView.findViewById(R.id.txt_section);
        TextView subject_col = (TextView)convertView.findViewById(R.id.txt_subject);
        TextView maxmarks_col = (TextView)convertView.findViewById(R.id.txt_marks);

        // Now set respective values
        date_col.setText(pending_test_list.get(position).getDate());
        class_col.setText(pending_test_list.get(position).getThe_class());
        section_col.setText(pending_test_list.get(position).getSection());
        subject_col.setText((pending_test_list.get(position).getSubject()));
        maxmarks_col.setText(pending_test_list.get(position).getMax_marks());

        return convertView;
    }
}
