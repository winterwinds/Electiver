package com.example.electiver;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;

public class DeadlineActivity extends AppCompatActivity {
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_deadline);


        Intent intent = getIntent();
        TextView text = (TextView)findViewById(R.id.text1);
        text.setText(String.valueOf(intent.getIntExtra("course_id", 0)));

    }
}
