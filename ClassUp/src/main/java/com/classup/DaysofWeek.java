package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DaysofWeek extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daysof_week);
        Intent intent = getIntent();
        final String student_id = intent.getStringExtra("student_id");
        final String coming_from = intent.getStringExtra("coming_from");


        String[] days_of_week = new String[] {"Monday", "Tuesday", "Wednesday",
                                                "Thursday", "Friday", "Saturday"};

        final ListView listView = findViewById(R.id.list_view_days_of_week);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_expandable_list_item_1, android.R.id.text1, days_of_week);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String day = (String)listView.getItemAtPosition(i);
                Intent intent = new Intent(DaysofWeek.this, MyTimeTable.class);
                intent.putExtra("coming_from", coming_from);
                if (coming_from.equals("student"))  {
                    intent.putExtra("student_id", student_id);
                }
                intent.putExtra("day", day);
                startActivity(intent);
            }
        });
    }
}
