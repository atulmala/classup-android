package com.classup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atulgupta on 07/10/15.
 */
public class SetSubjectsAdapter extends ArrayAdapter {
    private ArrayList<SubjectListSource> subject_list;
    public ArrayList<String> selected_subjects = new ArrayList<>();

    public List<SubjectListSource> getSubject_list() {
        return subject_list;
    }


    public SetSubjectsAdapter(Context context, int textViewResourceId,
                              ArrayList<SubjectListSource> subject_list,
                              ArrayList<String> already_set_subjects) {
        super(context, textViewResourceId, subject_list);
        this.subject_list = subject_list;
        this.selected_subjects = already_set_subjects;
    }

    @Override
    public int getCount()   {
        return  getSubject_list().size();
    }

    @Override
    public Object getItem(int position) {
        return subject_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_set_subject, null);
        }
        final CheckedTextView textView = convertView.findViewById(R.id.subject_name);

        TextView subject_name = convertView.findViewById(R.id.subject_name);
        subject_name.setText(subject_list.get(position).getSubject_name());

        if (selected_subjects.contains(subject_list.get(position).getSubject_name()))   {
            textView.setChecked(true);
        }
        else
            textView.setChecked(false);

        return convertView;
    }
}
