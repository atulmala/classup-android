package com.classup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParentsMenu extends AppCompatActivity {

    private String student_id;
    private String student_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_menu);
        student_id = getIntent().getStringExtra("student_id");
        student_name = getIntent().getStringExtra("student_name");

        TextView textView = (TextView)findViewById(R.id.txt_parent_menu_Heading);
        textView.setText(student_name);
    }

    public void p_attendanceSummary(View view)    {
        Intent intent = new Intent(this, ShowAttendanceSummaryParents.class);
        intent.putExtra("student_id", student_id);
        intent.putExtra("student_name", student_name);
        startActivity(intent);
    }

    public void p_communicateSchool(View view)  {
        Intent intent = new Intent(this, ParentCommunication.class);
        intent.putExtra("student_id", student_id);
        intent.putExtra("student_name", student_name);
        startActivity(intent);
    }

    public void p_logout(View view) {
        Intent intent = new Intent(this, LoginActivity.class).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void p_changePassword(View view) {
        Intent intent = new Intent(this, PasswordChange.class);
        startActivity(intent);
    }

    public void p_term_testResults (View view)  {
        Intent intent = new Intent(this, ShowExamList.class);
        intent.putExtra("student_id", student_id);
        intent.putExtra("student_name", student_name);
        startActivity(intent);
    }

    public void p_subject_wiseMarks(View view)  {
        Intent intent = new Intent(this, ParentsSelectSubject.class);
        intent.putExtra("student_id", student_id);
        intent.putExtra("student_name", student_name);
        startActivity(intent);
    }
}
