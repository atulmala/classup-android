package com.classup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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

    public void updateStudent(View view)    {
        Intent intent = new Intent(this, SelectClassSection1.class);
        startActivity(intent);
    }

    public void addTeacher(View view)   {
        Intent intent = new Intent(this, AddTeacher.class);
        startActivity(intent);
    }

    public void updateTeacher(View view)    {
        Intent intent = new Intent(this, SelectTeacher.class);
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

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Logout").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case 0:
                SessionManager.getInstance().logout();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
