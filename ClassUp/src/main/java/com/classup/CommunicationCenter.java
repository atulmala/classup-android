package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CommunicationCenter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_center);

        this.setTitle("Communication Center");
    }

    public void sendMessage(View view)  {
        Intent intent = new Intent(this, SelectClassSection.class);
        intent.putExtra("sender", "send_message");
        startActivity(intent);
    }

    public void showArrangements (View view)    {
        Intent intent = new Intent (this, Arrangements.class);
        intent.putExtra("teacher", SessionManager.getInstance().getLogged_in_user());
        startActivity (intent);
    }
}
