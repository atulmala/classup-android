package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ParentCommCenter extends AppCompatActivity {
    private String student_id;
    private String student_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_comm_center);
        this.setTitle("Communication Center");

        student_id = getIntent().getStringExtra("student_id");
        student_name = getIntent().getStringExtra("student_name");

    }

    public void communicate_with_school(View view) {
        Intent intent = new Intent(getApplicationContext(),
            ParentCommunication.class);
        intent.putExtra("student_id", student_id);
        intent.putExtra("student_name", student_name);
        startActivity(intent);
    }

    public void image_video(View view)  {
        Intent intent = new Intent(this, HWList.class);
        intent.putExtra("sender", "parent_pic_video");
        intent.putExtra("student_id", student_id);
        startActivity(intent);
    }

    public void communication_history(View view)    {
        Intent intent = new Intent(getApplicationContext(),
            CommunicationHistory.class);
        intent.putExtra("student_id", student_id);
        intent.putExtra("student_name", student_name);
        // 14/03/2018 - we will be using the same screen to
        // show the communication history for teachers
        intent.putExtra("coming_from",
            "parent");

        startActivity(intent);
    }

    public void online_classes(View view)   {
        Intent intent = new Intent(getApplicationContext(), OnlineClasses.class);
        intent.putExtra("student_id", student_id);
        intent.putExtra("student_name", student_name);
        intent.putExtra("sender",
            "parent");

        startActivity(intent);

    }
}
