package com.example.task2spider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;

public class CustomViewActivity extends Activity {
    CustomViewLayout customViewLayout;
    public static Vibrator vibrator;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customViewLayout=new CustomViewLayout(this);
        setContentView(customViewLayout);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        customViewLayout.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        customViewLayout.resume();
    }

    public void onBackPressed() {
        startActivity(new Intent(CustomViewActivity.this, MainActivity.class));
    }
}
