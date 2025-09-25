package com.example.onenew;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TextView tvWelcome = findViewById(R.id.tvWelcome);
//        TextView tvHome    = findViewById(R.id.tvHome);
//        TextView tvMart    = findViewById(R.id.tvMart);


        // Slide down Welcome
        Animation welcomeAnim = AnimationUtils.loadAnimation(this, R.anim.slide_down);
       // tvWelcome.startAnimation(welcomeAnim);

        // Left & Right slide for Home/Mart after slight delay so Welcome looks smooth
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            tvHome.setVisibility(View.VISIBLE);
//            tvMart.setVisibility(View.VISIBLE);
//            tvHome.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
//            tvMart.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));


        CardView cardLogoIn = findViewById(R.id.cardLogIn);

        cardLogoIn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        }
    }

