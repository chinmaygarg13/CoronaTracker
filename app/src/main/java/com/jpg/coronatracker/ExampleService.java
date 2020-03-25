package com.jpg.coronatracker;

//package com.codinginflow.foregroundserviceexample;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.android.gms.nearby.connection.ConnectionsClient;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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
        startAdvertisin();


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


    private void startAdvertisin() {

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
            String discoverer_endpoint;

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














//    private static final String[] REQUIRED_PERMISSIONS =
//            new String[]{
//                    Manifest.permission.BLUETOOTH,
//                    Manifest.permission.BLUETOOTH_ADMIN,
//                    Manifest.permission.ACCESS_WIFI_STATE,
//                    Manifest.permission.CHANGE_WIFI_STATE,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.READ_PHONE_NUMBERS
//            };
//
//    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
//
//    /** Called when our Activity has been made visible to the user. */
//    @Override
//    protected void onStart() {
//        super.onStart();
//        //ye get permission wala part tu chahe to mainactivity mai bhi daal de.
//        if (!hasPermissions(this, getRequiredPermissions())) {
//            if (Build.VERSION.SDK_INT < 23) {
//                ActivityCompat.requestPermissions(
//                        this, getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
//            } else {
//                requestPermissions(getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
//            }
//        }
//    }
//
//    /** Called when the user has accepted (or denied) our permission request. */
//    @CallSuper
//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
//            for (int grantResult : grantResults) {
//                if (grantResult == PackageManager.PERMISSION_DENIED) {
//                    Toast.makeText(this, "Missing Permissions.", Toast.LENGTH_LONG).show();
//                    finish();
//                    return;
//                }
//            }
//            recreate();
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    protected String[] getRequiredPermissions() {
//        return REQUIRED_PERMISSIONS;
//    }
//
//    public static boolean hasPermissions(Context context, String... permissions) {
//        for (String permission : permissions) {
//            if (ContextCompat.checkSelfPermission(context, permission)
//                    != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }

    /** Our handler to Nearby Connections. */
    private ConnectionsClient mConnectionsClient;

    /** The devices we've discovered near us. */
    private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();

    private final Map<String, Endpoint> mPendingConnections = new HashMap<>();

    /**
     * The devices we are currently connected to. For advertisers, this may be large. For discoverers,
     * there will only be one entry in this map.
     */
    private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();

    /**
     * True if we are asking a discovered device to connect to us. While we ask, we cannot ask another
     * device.
     */
    private boolean mIsConnecting = false;

    /** True if we are discovering. */
    private boolean mIsDiscovering = false;

    /** True if we are advertising. */
    private boolean mIsAdvertising = false;

    /** Callbacks for connections to other devices. */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
                    mPendingConnections.put(endpointId, endpoint);
                    ExampleService.this.onConnectionInitiated(endpoint, connectionInfo);
                }
                //Functions below are not needed as we won't be connecting anyways.
                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) { }

                @Override
                public void onDisconnected(String endpointId) { }
            };

    protected void startAdvertising() {
        mIsAdvertising = true;
        final String localEndpointName = getName();

        AdvertisingOptions.Builder advertisingOptions = new AdvertisingOptions.Builder();
        advertisingOptions.setStrategy(getStrategy());

        mConnectionsClient
                .startAdvertising(
                        localEndpointName,
                        getServiceId(),
                        mConnectionLifecycleCallback,
                        advertisingOptions.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) { }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mIsAdvertising = false;
                            }
                        });
    }

    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
    }

    protected void startDiscovering() {
        mIsDiscovering = true;
        mDiscoveredEndpoints.clear();
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
        discoveryOptions.setStrategy(getStrategy());
        mConnectionsClient
                .startDiscovery(
                        getServiceId(),
                        new EndpointDiscoveryCallback() {
                            @Override
                            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                                if (getServiceId().equals(info.getServiceId())) {
                                    Endpoint endpoint = new Endpoint(endpointId, info.getEndpointName());
                                    mDiscoveredEndpoints.put(endpointId, endpoint);
                                    onEndpointDiscovered(endpoint);
                                }
                            }

                            @Override
                            public void onEndpointLost(String endpointId) { }
                        },
                        discoveryOptions.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) { }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mIsDiscovering = false;
                            }
                        });
    }

    protected void onEndpointDiscovered(Endpoint endpoint) {

        Toast.makeText(var,"Discovered",Toast.LENGTH_SHORT).show();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && var.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(var,"Please grant Permission to access your phone number", Toast.LENGTH_SHORT).show();
        }
        TelephonyManager tMgr = (TelephonyManager) var.getSystemService(Context.TELEPHONY_SERVICE);
        String emei = tMgr.getDeviceId();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(emei);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss");
        String dt = sdf.format(new Date());

        Pair<String,Endpoint> pair = new Pair<>(dt,endpoint);

        DatabaseReference newRef = myRef.push();
        newRef.setValue(pair);
    }

    /** Resets and clears all state in Nearby Connections. */
    protected void stopAllEndpoints() {
        mConnectionsClient.stopAllEndpoints();
        mIsAdvertising = false;
        mIsDiscovering = false;
        mIsConnecting = false;
        mDiscoveredEndpoints.clear();
        mPendingConnections.clear();
        mEstablishedConnections.clear();
    }

    /** Returns a list of currently connected endpoints. */
    protected Set<Endpoint> getDiscoveredEndpoints() {
        return new HashSet<>(mDiscoveredEndpoints.values());
    }

    protected String getName(){
        return "Chinmay Garg";
        //TODO: take name as input from user, store in a shared pref and return here.
    }

    protected String getServiceId(){
        return "com.jpg.coronatracker";
    }

    protected Strategy getStrategy(){
        return Strategy.P2P_CLUSTER;
    }

    SharedPreferences pref = this.getSharedPreferences("com.jpg.coronatracker", Context.MODE_PRIVATE);

    /** Represents a device we can talk to. */
    protected class Endpoint {
        //we can manipulate this class so that it has only mobile number and maybe timestamp.
        @NonNull private final String id;
        @NonNull private final String name;
        @NonNull private String mob;
        private String emei;

        private Endpoint(@NonNull String id, @NonNull String name) {
            this.id = id;
            this.name = name;
            this.emei = pref.getString("emei", null);
        }

        @NonNull
        public String getId() {
            return id;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @NonNull
        public String getMob() { return mob; }

        public String getEmei(){ return emei; }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Endpoint) {
                Endpoint other = (Endpoint) obj;
                return id.equals(other.id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Endpoint{id=%s, name=%s, mobile_no=%s, emei=%s}", id, name, mob, emei);
        }
    }

}