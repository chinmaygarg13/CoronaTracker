package com.jpg.coronatracker;

//package com.codinginflow.foregroundserviceexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.DateInterval;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.Looper;
import android.renderscript.Sampler;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
//import android.support.v4.app.NotificationCompat;
//import android.support.annotation.Nullable;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.ValueEventRegistration;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
//
//import static com.codinginflow.foregroundserviceexample.App.CHANNEL_ID;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.jpg.coronatracker.App.CHANNEL_ID;
//import static com.example.service_example.;

public class ExampleService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private Location mCurrentLocation;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private String imei_number;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mCurrentLocation = location;
                        }
                    }
                });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    /**TODO- send location to firebase. For timestamp, do not use location.getTime().
                     For latitude: location.getLatitude, longitude: location.getLongitude.
                     For accuracy: location.getAccuracy (it will be used later to draw circle)**/
                    SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
                    String endPoint = pref.getString("imei","");
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference(endPoint);
                    DatabaseReference newRef = myRef.child("location_track").push();
                    newRef.child("location").setValue(location.getLatitude());
                    newRef.child("accuracy").setValue(location.getAccuracy());
                    newRef.child("longitude").setValue(location.getLongitude());
                    newRef.child("altitude").setValue(location.getAltitude());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dt = sdf.format(new Date());
                    newRef.child("dateTime").setValue(dt);

                    Log.d("location",location.toString());

                }
            }
        };

        createLocationRequest();
        startLocationUpdates();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String input = intent.getStringExtra("inputExtra");
        Log.d("service","Service has started successfully");
        //Toast.makeText(this, "yaha to aa raha hai.....", Toast.LENGTH_LONG).show();

        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                //.setContentTitle("CoronaTracker")
                //.setContentText("")
                .setSmallIcon(R.drawable.ic_android)
//                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread
        //stopSelf();

        startDiscovery();
        startAdvertisin();

        return START_STICKY;
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(30*60*1000);
        locationRequest.setFastestInterval(10*60*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(50);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    // Bluetooth is disconnected, do handling here
                    BluetoothAdapter.getDefaultAdapter().enable();
                    startAdvertisin();
                    startDiscovery();
                    Toast.makeText(ExampleService.this, "Corona ko bhagana hai to bluetooth hume hai ON rakhna.", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
            Nearby.getConnectionsClient(ExampleService.this).disconnectFromEndpoint(s);
            Log.d("service","in OnConnection Initiated");
            Log.d("service",String.valueOf(connectionInfo));
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

    private Context adv;
    private void startAdvertisin() {

        Log.d("service","in Start Adv");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this,"Please grant Permission to access your phone number", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
        String endPoint = pref.getString("imei", "");


        adv = this;
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(this)
                .startAdvertising(endPoint, "com.jpg.coronatracker", connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

        final Handler handler = new Handler();

        Toast.makeText(this,"Adv started",Toast.LENGTH_SHORT).show();
    }


    private Context var;

    private EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            //let's write to firebase now
            final String discoverer_endpoint;
            Log.d("endpoint_found","in onEndpointFound");
            Log.d("endpoint_found",s);
            Log.d("endpoint_found",discoveredEndpointInfo.getEndpointName());

            SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
            discoverer_endpoint = pref.getString("imei","");

            FirebaseDatabase database = FirebaseDatabase.getInstance();

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////

            DatabaseReference myRef = database.getReference(discoverer_endpoint+"/degree_infected");
//          String my_degree = myRef.equalTo("degree_infected").toString();

            Log.d("db","predb on discovery");
            ValueEventListener valueEventListener2 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String my_degree=null;
                    Log.d("db","inside on datachange");
                    if(dataSnapshot.getValue()==null) {
                        //Log.d("db", dataSnapshot.getValue().toString());
                       Log.d("db","I am here");
                       Log.d("db",dataSnapshot.getKey());
                    }
                    if(dataSnapshot.getValue() == null) {
                        Log.d("db", "no value in degree");
                        my_degree = "4";
                    }
                    else {
                        my_degree = dataSnapshot.getValue().toString();
                        SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
                        pref.edit().putString("degree_infected",my_degree).apply();
                    }
                    Log.d("notif","onMyDegreeChange");
                    Log.d("notif",my_degree);
                    if(my_degree.equals("2"))
                    {
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ExampleService.this);
                        mBuilder.setSmallIcon(R.drawable.warn_notif_200_low);
                        mBuilder.setChannelId(CHANNEL_ID);
                        mBuilder.setContentTitle("YOU ARE AT RISK");
                        mBuilder.setContentText("You have come in contact with an infected individual. Tap here to know more.");
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("You have come in contact with an infected individual. Tap here to know more."));
                        Intent intent = new Intent(var, OnNotifTapActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(var, 0, intent, 0);
                        mBuilder.setContentIntent(pendingIntent);
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(6,mBuilder.build());
                    }
                    else if(my_degree.equals("3"))
                    {
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ExampleService.this);
                        mBuilder.setSmallIcon(R.drawable.warn_notif_200_low);
                        mBuilder.setContentTitle("YOU ARE AT RISK");
                        mBuilder.setContentText("You have come in contact with a person who was previously in vicinity of an infected individual. Seek quarantine ASAP.");
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("You have come in contact with a person who was previously in vicinity of an infected individual. Seek quarantine ASAP. Tap here to know more."));
                        Intent intent = new Intent(var, OnNotifTapActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(var, 0, intent, 0);
                        mBuilder.setContentIntent(pendingIntent);
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(8, mBuilder.build());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            myRef.addValueEventListener(valueEventListener2);
            //myRef.removeEventListener(valueEventListener2);
            Log.d("db","post first db on discovery");


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////








            DatabaseReference myRef2 = database.getReference(discoverer_endpoint+"/infected_since");
//          String my_degree = myRef.equalTo("degree_infected").toString();

            //Log.d("db","predb on discovery");
            ValueEventListener valueEventListener3 = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //String my_degree=null;
                    Long infected_since=null;


                    Log.d("db","inside on datachange");
//                    if(dataSnapshot.getValue()==null) {
//                        //Log.d("db", dataSnapshot.getValue().toString());
//                        Log.d("db","I am here");
//                        //Log.d("db",dataSnapshot.getKey());
//
//                        //
//                    }
                    if(dataSnapshot.getValue()==(null)) {
                        Log.d("db", "no value in degree");
                        //my_degree = "4";
                        infected_since = 0l;
                    }
                    else
                        infected_since = Long.parseLong(dataSnapshot.getValue().toString());


                    Log.d("infected_since",infected_since.toString());
                    SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
                    pref.edit().putString("infected_string",infected_since.toString()).apply();
                    Log.d("db",infected_since.toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            myRef2.addValueEventListener(valueEventListener3);
            //myRef.removeEventListener(valueEventListener2);
            Log.d("db","post first db on discovery");

















//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////






            //DatabaseReference myRef3 = database.getReference(discoverer_endpoint);
















            DatabaseReference hisRef = database.getReference(discoveredEndpointInfo.getEndpointName()+"/degree_infected");
            ValueEventListener valueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("service","here");
                    String degree_infected;
                    if(dataSnapshot.getValue() == null)
                        degree_infected = "4";
                    else
                    {
                        degree_infected = dataSnapshot.getValue().toString();
                        SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
                        Integer my_current_degree = Integer.parseInt(pref.getString("degree_infected","4"));
                        int neighbor_degree = Integer.parseInt(degree_infected);
                        if(neighbor_degree+1<my_current_degree)
                        {
                            my_current_degree = neighbor_degree+1;
                            pref.edit().putString("degree_infected",my_current_degree.toString()).apply();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference hisRef = database.getReference(discoverer_endpoint+"/degree_infected");
                            hisRef.setValue(my_current_degree.toString());
                        }
                    }
                    Log.d("readdb",degree_infected);

                    if (degree_infected.equals("2") || degree_infected.equals("1")) {
                        Log.d("db","Reaching inside, hence string");
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ExampleService.this);
                        mBuilder.setSmallIcon(R.drawable.warn_notif_200_low);
                        mBuilder.setContentTitle("YOU ARE AT RISK");
                        mBuilder.setChannelId(CHANNEL_ID);
                        if (degree_infected.equals("1")) {

                            Log.d("notif","onMyDegreeChange");
                            Log.d("notif",degree_infected);
                            Log.d("db","you");
                            mBuilder.setContentText("You have come in contact with an infected individual. Tap here to know more.");
                            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("You have come in contact with an infected individual. Tap here to know more."));

                        } else {
                            mBuilder.setContentText("You have come in contact with a person who was previously in vicinity of an infected individual. Seek quarantine ASAP.");
                            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("You have come in contact with a person who was previously in vicinity of an infected individual. Seek quarantine ASAP. Tap here to know more"));
                            SharedPreferences pref = getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);
                            //Date ts = new Date(Long.parseLong(pref.getString("infected_since",String.valueOf((new Date(0l)).getTime()))));
                            Date d = new Date();
                            Date ts = new Date(Long.parseLong(pref.getString("infected_since",String.valueOf(d.getTime()))));
                            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("You have come in contact with a person who was previously in vicinity of an infected individual. at "+ts.toString()+" Seek quarantine ASAP."));

                        }
                        Intent intent = new Intent(var, OnNotifTapActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(var, 0, intent, 0);
                        mBuilder.setContentIntent(pendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(8, mBuilder.build());
                        //unregisterReceiver(mReceiver);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            hisRef.addValueEventListener(valueEventListener);
            //hisRef.removeEventListener(valueEventListener);

            Log.d("db","post 2nd db on discovery");
            //String degree_infected = hisStatusRef.equalTo("degree_infected").toString();
            //Log.d("degree",degree_infected);

            Log.d("endpoint_discovered","I have reached 362.");
            //TODO: This line maybe redundant
            myRef.keepSynced(true);
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            //String dt = sdf.format(new Date());

            Long dt = new Date().getTime();

            Pair<Long, String> pair = new Pair<>(dt,discoveredEndpointInfo.getEndpointName());
            Toast.makeText(var,"Discovered",Toast.LENGTH_SHORT).show();
            Log.d("endpoint_discovered","DISCOVERED");
            Log.d("endpoint_discovered",discoveredEndpointInfo.getEndpointName());
            Log.d("endpoint_discovered",dt.toString());

            Log.d("time",String.valueOf(pref.getAll()));
            //final Date T = new Date();
            final Long T = new Date((new Date(System.currentTimeMillis()+5*60*1000)).getTime()-(new Date()).getTime()).getTime();
            String old_d = pref.getString(discoveredEndpointInfo.getEndpointName(), null);

            Long old_date = null;
            if(old_d!=null)
                old_date = Long.parseLong(old_d);

            Log.d("time","DATE");
            Long current_date = new Date().getTime();
            //Date current_date = new Date();//Current date time
            Long diff = new Date().getTime();

            if(old_date!=null) {
                //compute the difference
                Log.d("time",old_date.toString());
                diff = new Date(current_date- old_date).getTime();
                Log.d("time","Reached difference calculator");
                Log.d("time",diff.toString());
            }
            if(old_date==null || diff>T)
            {
                Log.d("time","writing");
                pref.edit().putString(discoveredEndpointInfo.getEndpointName(),String.valueOf(dt)).apply();
                Log.d("time stored", String.valueOf(dt));

                DatabaseReference newRef = database.getReference(discoverer_endpoint);
                DatabaseReference nRef = newRef.push();
                nRef.setValue(pair);
                //DatabaseReference n = myRef.getRef(discoveredEndpointInfo.getEndpointName());

            }
            Nearby.getConnectionsClient(ExampleService.this).rejectConnection(discoveredEndpointInfo.getEndpointName());
            //Nearby.getConnectionsClient(ExampleService.this).disconnectFromEndpoint(discoveredEndpointInfo.getEndpointName());
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            Log.d("end_point_lost","Endpoint Lost");
        }
    };

    private void startDiscovery() {

        var = this; // store context
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();


        Nearby.getConnectionsClient(this)
                .startDiscovery("com.jpg.coronatracker", endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
        Log.d("service","in discovery");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}