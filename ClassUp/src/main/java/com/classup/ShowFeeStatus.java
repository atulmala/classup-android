package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShowFeeStatus extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_fee_status);
        Intent intent = getIntent();
        String welcome_message = intent.getStringExtra("welcome_message");
        String amount_due = intent.getStringExtra("amount_due");
        String stop_access = intent.getStringExtra("stop_access");

        TextView textView = findViewById(R.id.default_message);
        textView.setText(welcome_message);

        Button button = findViewById(R.id.btn_continue);
        if (stop_access.equals("true"))  {
            button.setEnabled(false);
            button.setVisibility(View.INVISIBLE);
        }
        else    {
            button.setEnabled(true);
        }
    }

    public void showWard(View view) {
        startActivity(new Intent
            ("com.classup.ShowWard"));
    }
}
