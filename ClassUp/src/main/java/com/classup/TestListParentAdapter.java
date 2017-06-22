package com.classup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by atulgupta on 05/06/17.
 */

public class TestListParentAdapter extends BaseAdapter {
    private Activity activity;
    private List<TestListSource> test_list;

    public TestListParentAdapter(Activity activity, List<TestListSource> completed_test_list) {
        super();
        this.activity = activity;
        this.test_list = completed_test_list;
    }

    @Override
    public int getCount()   {
        return test_list.size();
    }

    public Object getItem(int position) {
        return test_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        if(convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.p_test_list_row, null);
        }
        TextView date = (TextView)convertView.findViewById(R.id.txt_test_date);
        TextView subject = (TextView)convertView.findViewById(R.id.txt_test);
        TextView test_topic = (TextView)convertView.findViewById(R.id.txt_test_topics);

        // Now set respective values
        date.setText(test_list.get(position).getDate());
        subject.setText((test_list.get(position).getSubject()));
        test_topic.setText(test_list.get(position).getTest_topics());

        return convertView;
    }
}
