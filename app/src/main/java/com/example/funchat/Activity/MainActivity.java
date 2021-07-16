package com.example.funchat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.funchat.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {

                   Intent i = new Intent(MainActivity.this, HomeActivity.class);
                   startActivity(i);
                   finish();
           }
       },3000);
    }
//
//    @Override
//    public void onBackPressed() {
//
//        if(doubleBackToExitPressedOnce) {
//            super.onBackPressed();
//            return;
//        }
//
//        Toast.makeText(this,"Please click back again to exit",Toast.LENGTH_SHORT).show();
//        doubleBackToExitPressedOnce=true;
//    }
}