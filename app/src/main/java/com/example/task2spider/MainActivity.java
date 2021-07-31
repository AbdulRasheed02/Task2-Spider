package com.example.task2spider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_Novice, btn_Normal, btn_Nightmare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        this.setTitle("MENU");

        btn_Novice=findViewById(R.id.btn_Novice);
        btn_Normal=findViewById(R.id.btn_Normal);
        btn_Nightmare=findViewById(R.id.btn_Nightmare);

        btn_Novice.setText("NOVICE");
        btn_Normal.setText("NORMAL");
        btn_Nightmare.setText("NIGHTMARE");

        btn_Novice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,CustomViewActivity.class);
                intent.putExtra("NoviceMode", true);
                intent.putExtra("NormalMode", false);
                intent.putExtra("NightmareMode", false);
                startActivity(intent);
            }
        });

        btn_Normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,CustomViewActivity.class);
                intent.putExtra("NoviceMode", false);
                intent.putExtra("NormalMode", true);
                intent.putExtra("NightmareMode", false);
                startActivity(intent);
            }
        });

        btn_Nightmare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,CustomViewActivity.class);
                intent.putExtra("NoviceMode", false);
                intent.putExtra("NormalMode", false);
                intent.putExtra("NightmareMode", true);
                startActivity(intent);
            }
        });

    }

    public void onBackPressed() {
        finishAffinity();
    }
}