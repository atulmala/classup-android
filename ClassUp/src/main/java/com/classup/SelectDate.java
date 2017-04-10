package com.classup;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

public class SelectDate extends AppCompatActivity {
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_date);

        datePicker = (DatePicker)findViewById(R.id.pick_att_summ_school_date);

        ViewGroup viewGroup = (ViewGroup)datePicker.findViewById(Resources.getSystem().
                getIdentifier("month", "id", "android"));

        Calendar calendar =  Calendar.getInstance();
        datePicker.setMaxDate(calendar.getTimeInMillis());
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Next").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case 0:
                Intent intent = new Intent(this, SchoolAttendanceSummary.class);
                final Integer d = datePicker.getDayOfMonth();
                final Integer m = datePicker.getMonth() + 1;  // because index start from 0
                final Integer y = datePicker.getYear();
                intent.putExtra("date", d.toString());
                intent.putExtra("month", m.toString());
                intent.putExtra("year", y.toString());

                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
