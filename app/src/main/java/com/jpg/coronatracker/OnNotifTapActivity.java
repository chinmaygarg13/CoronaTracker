package com.jpg.coronatracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OnNotifTapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
        String degree = pref.getString("degree_infected","3");
        if(degree == "2")
            setContentView(R.layout.on_tap);
        else
            setContentView(R.layout.on_tap_medium);

        String infString = pref.getString("infected_string","");
        if(infString != ""){
            final TextView t = findViewById(R.id.t3);
            Long inf = Long.parseLong(infString);
            Date obj = new Date(inf);
            String input = obj.toString();
            t.setText(infString);
        }

    }
}

