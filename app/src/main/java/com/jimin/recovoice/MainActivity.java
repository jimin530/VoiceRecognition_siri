package com.jimin.recovoice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getApplicationContext(), SpottingService.class)); // Start CMUSphinx by Service
            }
        });
    }
    @Override
    protected void onDestroy() {
        try {
            stopService(new Intent(getApplicationContext(), SpottingService.class));
            stopService(new Intent(getApplicationContext(), SttService.class));
        } catch (Exception e) {
        }
        super.onDestroy();
    }
}
