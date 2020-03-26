package com.jpg.coronatracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.RequiresApi;

public class App extends Application {
    public static final String CHANNEL_ID = "exampleServiceChannel";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate()
    {
        super.onCreate();;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createNotificationChannel();
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Example Service Channel", NotificationManager.IMPORTANCE_DEFAULT);


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }
}
