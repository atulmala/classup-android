package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SchoolAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_admin);
    }

    public void attendanceSummary(View view)    {
        Intent intent = new Intent(this, SelectDate.class);
        startActivity(intent);
    }

    public void sendBulkSMS(View view)  {
        Intent intent = new Intent(this, SendBulkSMS.class);
        startActivity(intent);
    }

    public void addStudent(View view)  {
        Intent intent = new Intent(this, AddStudent.class);
        startActivity(intent);
    }

    public void admin_changePassword(View view) {
        Intent intent = new Intent(this, PasswordChange.class);
        startActivity(intent);
    }

    public void performLogout(View view)    {
        SessionManager.getInstance().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
