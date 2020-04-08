package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ExamTestManagement extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_test_management);
    }

    public void scheduleTest(View view) {
        Intent intent = new Intent(this, ExamListTeacher.class);
        intent.putExtra("sender", "scheduleTest");
        startActivity(intent);
    }

    public void manageTest(View view)   {
        Intent intent = new Intent(this, ExamListTeacher.class);
        intent.putExtra("sender", "manageTest");
        startActivity(intent);
    }

    public void coScholastic(View view) {
        Intent intent = new Intent(this, SelectClassSection1.class);
        intent.putExtra("sender", "co_scholastic");
        startActivity(intent);
    }
}
