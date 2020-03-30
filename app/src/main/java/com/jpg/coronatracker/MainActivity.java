//package com.codinginflow.foregroundserviceexample;
package com.jpg.coronatracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    //private EditText editTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        Boolean ft = pref.getBoolean("first_time", true);

        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH);
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH_ADMIN);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_WIFI_STATE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.CHANGE_WIFI_STATE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_PHONE_STATE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            REQUIRED_PERMISSIONS.add(Manifest.permission.READ_PHONE_NUMBERS);

        }

        final EditText et_name1 = findViewById(R.id.name1);
        final EditText et_name2 = findViewById(R.id.name2);
        final EditText et_phone = findViewById(R.id.phone);
        final Button b_enter = findViewById(R.id.enter);
        final Button b_service = findViewById(R.id.service);
        final TextView shield = findViewById(R.id.shield);
        final ImageView person = findViewById(R.id.person);
        final ImageView halo = findViewById(R.id.halo);
        final TextView sertext = findViewById(R.id.sertext);

        if (ft == false) {
            b_enter.setVisibility(View.GONE);
            et_name1.setVisibility(View.GONE);
            et_name2.setVisibility(View.GONE);
            et_phone.setVisibility(View.GONE);
            b_service.setVisibility(View.VISIBLE);
            shield.setVisibility(View.VISIBLE);
            person.setVisibility(View.VISIBLE);
            halo.setVisibility(View.VISIBLE);
            sertext.setVisibility(View.VISIBLE);
            sertext.setText("Tap to Restart the Shield.");
            String level = pref.getString("degree_infected","4");
            switch (level){
                case "1":
                    person.setImageResource(R.drawable.level1);
                    shield.setText("You are INFECTED.");
                    break;
                case "2":
                    person.setImageResource(R.drawable.level2);
                    shield.setText("You are at HIGH Risk.");
                    break;
                case "3":
                    person.setImageResource(R.drawable.level3);
                    shield.setText("You are at MODERATE Risk.");
                case "4":
                    person.setImageResource(R.drawable.level4);
                    shield.setText("You are doing fine.");
            }
        }

        b_enter.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                String name = et_name1.getText().toString();
                name += " " + et_name2.getText().toString();
                String phone = et_phone.getText().toString();
                Boolean cont = true;

                String imei = null;
                if(Build.VERSION.SDK_INT < 29) {
                    TelephonyManager tMgr = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        imei = tMgr.getImei();
                    } else {
                        imei = tMgr.getDeviceId();
                    }
                }
                else{
                    if(phone.length() != 10){
                        Toast.makeText(MainActivity.this, "Please enter a 10 digit mobile number", Toast.LENGTH_SHORT).show();
                        b_enter.setClickable(true);
                        cont = false;
                    }
                    else{
                        imei = phone;
                    }
                }

                if(cont) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference(imei);

                    DatabaseReference nameRef = myRef.child("name");
                    nameRef.setValue(name);

                    DatabaseReference phoneRef = myRef.child("phone");
                    phoneRef.setValue(phone);

                    DatabaseReference degree_infectedRef = myRef.child("degree_infected");
                    degree_infectedRef.setValue(4);

                    DatabaseReference date_infected_sinceRef = myRef.child("infected_since");
                    date_infected_sinceRef.setValue(null);


                    pref.edit().putString("name", name).apply();
                    pref.edit().putString("phone", phone).apply();
                    pref.edit().putBoolean("first_time", false).apply();
                    pref.edit().putString("imei", imei).apply();

                    b_enter.setVisibility(View.GONE);
                    et_name1.setVisibility(View.GONE);
                    et_name2.setVisibility(View.GONE);
                    et_phone.setVisibility(View.GONE);
                    b_service.setVisibility(View.VISIBLE);
                    person.setVisibility(View.VISIBLE);
                }
            }
        });


    }
    private List<String> REQUIRED_PERMISSIONS = new ArrayList<String>();
//    private static final String[] REQUIRED_PERMISSIONS =
//        new String[]{
//                Manifest.permission.BLUETOOTH,
//                Manifest.permission.BLUETOOTH_ADMIN,
//                Manifest.permission.ACCESS_WIFI_STATE,
//                Manifest.permission.CHANGE_WIFI_STATE,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.READ_PHONE_NUMBERS,
//    };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    /** Called when our Activity has been made visible to the user. */
    @Override
    protected void onStart() {
        super.onStart();
        //ye get permission wala part tu chahe to mainactivity mai bhi daal de.
        if (!hasPermissions(this, getRequiredPermissions())) {
            if (Build.VERSION.SDK_INT < 23) {
                ActivityCompat.requestPermissions(
                        this, getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
            } else {
                requestPermissions(getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
    }

    /** Called when the user has accepted (or denied) our permission request. */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int i = 0;
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Log.d("permission", permissions[i]);
                    Toast.makeText(this, "Missing Permissions.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                i += 1;
            }
            recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected String[] getRequiredPermissions() {
        String[] temp =  new String[REQUIRED_PERMISSIONS.size()];
        REQUIRED_PERMISSIONS.toArray(temp);
        return temp;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @SuppressLint("MissingPermission")
    public void startService(View v) {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        minimizeApp();
    }
}