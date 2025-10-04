package com.example.onenew;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Phone dark/light mode ko ignore karke hamesha Light theme use karega
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
    }
}
