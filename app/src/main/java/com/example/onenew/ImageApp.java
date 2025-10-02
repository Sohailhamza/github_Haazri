package com.example.onenew;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class ImageApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map config = new HashMap();
        config.put("cloud_name", "dmqsjefwy");   // 👈 tumhara cloud name
        config.put("api_key", "414429798968795");   // 👈 console se lo // le li hy
        config.put("api_secret", "rgq3_ojgngFpnWRLRBuHeDlvNlI"); // 👈 secret (agar unsigned preset use nahi kar rahe)

        MediaManager.init(this, config);
    }
}
