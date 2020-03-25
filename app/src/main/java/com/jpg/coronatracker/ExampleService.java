package com.jpg.coronatracker;

//package com.codinginflow.foregroundserviceexample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.widget.Toast;
//import android.support.v4.app.NotificationCompat;
//import android.support.annotation.Nullable;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
//
//import static com.codinginflow.foregroundserviceexample.App.CHANNEL_ID;

import static com.jpg.coronatracker.App.CHANNEL_ID;
//import static com.example.service_example.;

public class ExampleService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Example Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_android)
//                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        //do heavy work on a background thread
        //stopSelf();


        startDiscovery();
        startAdvertising();
        //startDiscovery();


        return START_STICKY;
    }


    private ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
            //do something
            //Toast.makeText(this,"Nearby working atleast",Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,"",Toast.LENGTH_SHORT);
            System.out.println("HELLO");
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            //do something
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            //do something
        }
    } ;


    private OnSuccessListener onSuccessListener = new OnSuccessListener() {
        @Override
        public void onSuccess(Object o) {
            //do something
        }
    };

    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            //do something on failure
        }
    };


    private void startAdvertising() {



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this,"Please grant Permission to access your phone number", Toast.LENGTH_SHORT).show();
        }



        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String endPoint = tMgr.getDeviceId();


        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(this)
                .startAdvertising(endPoint, "com.jpg.coronatracker", connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

        final Handler handler = new Handler();

        Toast.makeText(this,"Adv started",Toast.LENGTH_SHORT).show();
    }


//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                    stopSelf();
//                    Toast.makeText()
//            }
//        },5000);


        //stopSelf();


//                        getUserNickname(), SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
//                .addOnSuccessListener(
//                        (Void unused) -> {
                            // We're advertising!
//                        })
//                .addOnFailureListener(
//                        (Exception e) -> {
//                            // We were unable to start advertising.





















    private Context var;

    private EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            //let's write to firebase now
            String discoverer_endpoint = "Endpoint found";

//            if (ActivityCompat.checkSelfPermission(var, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                Toast.makeText(var, "Please grant Permission to access your phone number", Toast.LENGTH_SHORT).show();
//                return;
//            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && var.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(var,"Please grant Permission to access your phone number", Toast.LENGTH_SHORT).show();
            }



            TelephonyManager tMgr = (TelephonyManager) var.getSystemService(Context.TELEPHONY_SERVICE);
            discoverer_endpoint = tMgr.getDeviceId();

            Toast.makeText(var,"Discovered",Toast.LENGTH_SHORT).show();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(discoverer_endpoint);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss");
            String dt = sdf.format(new Date());

            Pair<String,DiscoveredEndpointInfo> pair = new Pair<>(dt,discoveredEndpointInfo);
//            JSONObject obj = new JSONObject();
//
//            try {
//                obj.put("timestamp",dt);
//                obj.put("info",discoveredEndpointInfo);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }


            //myRef.setValue(discoveredEndpointInfo);
            DatabaseReference newRef = myRef.push();
            newRef.setValue(pair);

        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            //do something
        }
    };






    private void startDiscovery() {

        Toast.makeText(this,"Discovering",Toast.LENGTH_SHORT).show();
        var = this;

        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(this)
                .startDiscovery("com.jpg.coronatracker", endpointDiscoveryCallback, discoveryOptions);
//                .addOnSuccessListener(
//                        (Void unused) -> {
//                            // We're discovering!
//                        })
//                .addOnFailureListener(
//                        (Exception e) -> {
//                            // We're unable to start discovering.
//                        })

    }















    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}