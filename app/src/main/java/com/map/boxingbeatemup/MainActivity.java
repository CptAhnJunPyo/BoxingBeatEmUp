package com.map.boxingbeatemup;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity {
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            btnStart = findViewById(R.id.btnStart);

            btnStart.setOnClickListener(view -> {
                try {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            // Clean up resources
            if (btnStart != null) {
                btnStart.setOnClickListener(null);
                btnStart = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Only finish after the transition is complete
        finish();
    }
}