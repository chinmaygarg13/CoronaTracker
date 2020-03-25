//package com.codinginflow.foregroundserviceexample;
package com.jpg.coronatracker;

import android.Manifest;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    //private EditText editTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText et_name = findViewById(R.id.name);
        final EditText et_phone = findViewById(R.id.phone);
        final Button b_enter = findViewById(R.id.enter);
        final Button b_service = findViewById(R.id.service);

        final SharedPreferences pref = this.getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);

        b_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String phone = et_phone.getText().toString();
                if(phone.length() != 10){
                    b_enter.setClickable(true);
                    Toast.makeText(MainActivity.this, "Please enter valid mobile number.", Toast.LENGTH_SHORT).show();
                }
                else {
                    pref.edit().putString("name", name).apply();
                    pref.edit().putString("phone", phone).apply();
                    b_enter.setVisibility(View.GONE);
                    et_name.setVisibility(View.GONE);
                    et_phone.setVisibility(View.GONE);
                    b_service.setVisibility(View.VISIBLE);
                }
            }
        });


    }

        private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS
            };

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
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Missing Permissions.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
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

    public void startService(View v) {
        //String input = editTextInput.getText().toString();
        String input;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Please grant Permission to access your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();
        /* Extract the IMEI of the person */
        input = tMgr.getDeviceId();
        //Boilerplate code to write a message to the database. Change/Create the hierarchy in getReference and set it's value to something
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(input);
        myRef.setValue("Hello");

        SharedPreferences pref = this.getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
        pref.edit().putString("emei", input).apply();






//        To use Firebase Storage
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference();







        Intent serviceIntent = new Intent(this, ExampleService.class);
        serviceIntent.putExtra("inputExtra", input);

        ContextCompat.startForegroundService(this, serviceIntent);
    }

//    public void stopService(View v) {
//        Intent serviceIntent = new Intent(this, ExampleService.class);
//        stopService(serviceIntent);
//    }
}