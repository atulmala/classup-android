package com.classup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by atulgupta on 02/10/15.
 */
public class MarksEntryListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater mLayoutInflater = null;
    private List<MarksEntryListSource> marks_entry_list;
    private Boolean whether_grade_based;
    //private String test_id;

    public String max_marks = "50";
    public String pass_marks = "10";

    public List<MarksEntryListSource> getMarks_entry_list() {
        return marks_entry_list;
    }

    public MarksEntryListAdapter(Activity activity, List<MarksEntryListSource> list,
                                 Boolean whether_grade_based) {
        super();
        this.activity = activity;

        this.marks_entry_list = list;
        this.whether_grade_based = whether_grade_based;
        //this.test_id = test_id;

        mLayoutInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

   @Override
    public int getCount()   {
        return  marks_entry_list.size();
    }

    @Override
    public Object getItem(int position) {
        return marks_entry_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)   {
        View view = convertView;
        final ViewHolder holder;
        if (view == null || view.getTag() == null)    {
            //LayoutInflater inflater = activity.getLayoutInflater();
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.marks_entry_row1, null);
            holder = new ViewHolder(view);
            //view.setTag(holder);
        }
        else
            holder = (ViewHolder)view.getTag();

        if(!whether_grade_based) {
            // numeric keyboard for marks based test
            holder.marks_or_grade.setInputType
                    (InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        else
            holder.marks_or_grade.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        holder.roll_no.setText(marks_entry_list.get(position).getRoll_no());
        holder.full_name.setText(marks_entry_list.get(position).getFull_name());
        holder.parent_name.setText(marks_entry_list.get(position).getParent());

        String mg;
        if(!whether_grade_based)
            mg = marks_entry_list.get(position).getMarks();
        else
            mg = marks_entry_list.get(position).getGrade();

        switch (mg)  {
            case "-5000.00":
            case "-5000":
            case "":
                mg = "";
                holder.whether_absent.setChecked(false);
                holder.marks_or_grade.setEnabled(true);
                break;

            case "-1000.00":
            case "-1000":
                mg = "ABS";
                holder.whether_absent.setChecked(true);
                holder.marks_or_grade.setEnabled(false);
                break;
            default:
                break;
        }
        holder.marks_or_grade.setText(mg);

        // if the marks obtained are less than passing marks they need to be highligted
        if (!whether_grade_based)   {
            if (!holder.marks_or_grade.getText().toString().equals("") &&
                    !holder.marks_or_grade.getText().toString().equals("ABS"))
            {
                float marks = 0;
                if (holder.marks_or_grade.getText().toString().equals(".")) {
                    marks = 0;
                }
                else {
                    marks = Float.parseFloat(holder.marks_or_grade.getText().toString());
                }
                float pm = Float.parseFloat(pass_marks.toString());
                if (marks < pm) {
                    holder.marks_or_grade.setTextColor(Color.RED);
                }
                else
                    holder.marks_or_grade.setBackgroundColor(Color.WHITE);
            }
        }

        // program the absence switch
        holder.whether_absent.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!holder.whether_absent.hasFocus())
                    if (isChecked)  {
                        holder.marks_or_grade.setText("ABS");
                        holder.marks_or_grade.setTextColor(Color.BLUE);
                        holder.marks_or_grade.setEnabled(false);

                        if(whether_grade_based)
                            marks_entry_list.get(position).setGrade("-1000.00");
                        else
                            marks_entry_list.get(position).setMarks("-1000.00");
                    }
                    else    {
                        holder.marks_or_grade.setText("");
                        holder.marks_or_grade.setEnabled(true);
                        holder.marks_or_grade.setBackgroundColor(Color.WHITE);

                        if(whether_grade_based)
                            marks_entry_list.get(position).setGrade("-5000.00");
                        else
                            marks_entry_list.get(position).setMarks("-5000.00");
                    }

            }
        });

        holder.marks_or_grade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!whether_grade_based) {
                    switch (holder.marks_or_grade.getText().toString()) {
                        case "":
                            marks_entry_list.get(position).setMarks("-5000.00");
                            break;
                        case "ABS":
                            marks_entry_list.get(position).setMarks("-1000.00");
                            break;
                        default:
                            float marks = 0;
                            if (holder.marks_or_grade.getText().toString().equals(".")) {
                                marks = 0;
                            }
                            else {
                                marks = Float.parseFloat(holder.marks_or_grade.getText().toString());
                            }
                            float mm = Float.parseFloat(max_marks.toString());
                            float pm = Float.parseFloat(pass_marks.toString());
                            //float mm = Float.parseFloat(mm_list.get(0).toString());
                            if (marks > mm) {
                                String message = "Marks entered: ";
                                message += holder.marks_or_grade.getText().toString();
                                message += " for " + holder.full_name.getText() + " are more than ";
                                message += "Max marks: " + max_marks.toString();
                                Toast toast = Toast.makeText(activity, message, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                holder.marks_or_grade.setText("");
                            }

                            if (marks < pm) {
                                holder.marks_or_grade.setBackgroundColor(Color.RED);
                            }
                            else
                                holder.marks_or_grade.setBackgroundColor(Color.WHITE);

                            marks_entry_list.get(position).
                                    setMarks(holder.marks_or_grade.getText().toString());
                            break;
                    }
                } else {
                    switch (holder.marks_or_grade.getText().toString()) {
                        case "":
                            marks_entry_list.get(position).setGrade("-5000.00");
                            break;
                        case "ABS":
                            marks_entry_list.get(position).setGrade("-1000.00");
                            break;
                        default:
                            marks_entry_list.get(position).
                                    setGrade(holder.marks_or_grade.getText().toString());
                            break;
                    }
                }
                //print_list(position);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    static class ViewHolder {
        TextView roll_no;
        TextView full_name;
        TextView parent_name;
        EditText marks_or_grade;
        CheckBox whether_absent;
        static int i = 0;

        public ViewHolder(View view)    {
            //System.out.println("i=" + i++);
            this.roll_no = (TextView)view.findViewById(R.id.marks_entry_roll_no);
            this.full_name = (TextView)view.findViewById(R.id.marks_entry_name);
            this.parent_name = (TextView)view.findViewById(R.id.parent_name);
            this.marks_or_grade = (EditText)view.findViewById(R.id.marks_entry_marks_or_grade);
            this.whether_absent = (CheckBox)view.findViewById(R.id.absence_switch);
        }
    }
}