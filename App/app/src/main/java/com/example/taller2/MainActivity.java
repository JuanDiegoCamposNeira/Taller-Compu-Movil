package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //----------------------------------------------
    //              Attributes
    //----------------------------------------------
    // Map
    private GoogleMap mMap;

    // Notification
    private static final int NOTIFICATION_CODE = 200;
    private static final String NOTIFICATION_CHANNEL = "NOTIFICATION";

    // User location
    private LatLng currentLocation = null;
    private FusedLocationProviderClient userLocation;

    // Firebase
    private FirebaseAuth userAuth;
    private DatabaseReference database;
    private String currentUserId = "";
    private boolean initialState = true;
    ArrayList<User> users = new ArrayList<User>();

    // Permissions
    private static final String[] locationPermissions = new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
    private static final int REQUEST_LOCATION_ACCESS_CODE = 100;
    private static final int LOCATION_ACCESS_CODE = 101;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Configurations
        userAuth = FirebaseAuth.getInstance();
        currentUserId = userAuth.getUid();
        database = FirebaseDatabase.getInstance().getReference("Users");
        createNotificationChannel();


        // Create listener for location in users ...
        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Save initial state of DB
                if (initialState) {
                    initialState = false;
                    for (DataSnapshot singleUser : snapshot.getChildren()) {
                        User user = singleUser.getValue( User.class );
                        users.add(user);
                    }
                    Log.i("STATE_I", "Entered to initialize the users in the database");
                    return;
                }

                // Create notification if changed user is available now
                short shouldStartLocationActivity = shouldCreateNotification(snapshot);
                Log.i("STATE:", "INDEX ... " + String.valueOf(shouldStartLocationActivity));
                if (shouldStartLocationActivity != -1) {
                    Log.i("STATE", "USER CHANGED ITS STATUS");
                    short index = shouldStartLocationActivity;
                    createNotificaion(index);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("STATE", "Notification:Error");
            }
        };
        database.addValueEventListener(usersListener); // Assign the listener to a database reference



        // Request permissions to access location
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permiso para acceder a la localización requerido")
                    .setMessage("Este permiso permite a la aplicación acceder a su ubicación, para mostrarlo en el mapa")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, locationPermissions, LOCATION_ACCESS_CODE);
                        }
                    })
                    .setNegativeButton("Negar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, locationPermissions, LOCATION_ACCESS_CODE);
        }

    }

    //--------------------------------------------------
    //             Methods
    //--------------------------------------------------

    private void createNotificaion(int index) {

        // Create an explicit intent for an Activity in your app
        Intent showUserLocation = new Intent(this, SinglePersonLocationActivity.class);
        showUserLocation.putExtra("nombre", users.get(index).Nombres);
        showUserLocation.putExtra("lat", users.get(index).Latitud);
        showUserLocation.putExtra("lng", users.get(index).Longitud);

        showUserLocation.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, showUserLocation, 0);

        String notificationMessage = users.get(index).Nombres + " ahora se encuentra disponible :)";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "Simple id channel")
                .setSmallIcon(R.drawable.googleg_disabled_color_18)
                .setContentTitle("Cambio de estado")
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(1, notificationBuilder.build());

    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "NOTIFICATION";
            String description = "NOTIFICATION";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Function that decides if the a notification should be shown to the user
     * @param snapshot, contains the users of the DB
     * @return index of the user that is now available, or -1 if a user changed its state to unavailable
     */
    private short shouldCreateNotification(DataSnapshot snapshot) {
        short changedUser = -1;
        ArrayList<User> changedUsers = new ArrayList<User>();

        // Fill changed users array
        for (DataSnapshot singleUser : snapshot.getChildren()) {
            User user = singleUser.getValue( User.class );
            changedUsers.add( user );
        }
        // If a new user was registered
        if (changedUsers.size() != users.size()) {
            uploadUsersArray(changedUsers);
            return (short) ((short) changedUsers.size() - 1);
        }

        // Check for users that have changed its status to available
        for (int i = 0; i < changedUsers.size(); i++) {
            // User have change its status to available
            if (!changedUsers.get( i ).Disponible.equals( users.get(i).Disponible ) && changedUsers.get( i ).Disponible.equals("true")) {
                changedUser = (short) i;
            }
        }

        uploadUsersArray(changedUsers);

        return changedUser;
    }

    private void uploadUsersArray(ArrayList<User> newUsersArray) {
        // If a user registered
        if (newUsersArray.size() != users.size()) {
            users.add(newUsersArray.get( newUsersArray.size() - 1 ));
            return;
        }
        // If a user state changed
        for (int i = 0; i < newUsersArray.size(); i++) {
            users.get(i).Disponible = newUsersArray.get(i).Disponible;
        }
    }

    private void getLastKnownLocation() {
        userLocation = LocationServices.getFusedLocationProviderClient(this);
        userLocation.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker( new MarkerOptions()
                                    .position(userLocation)
                                    .title("Localización actual del usuario")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) );
                        }
                    }
                });
    }

    private String loadJSON() {

        String JSONString = "";

        try {
            InputStream is = this.getAssets().open("locations.json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            JSONString = new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSONString;
    }

    //--------------------------------------------------
    //             On results methods
    //--------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_ACCESS_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    getLastKnownLocation();
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    new AlertDialog.Builder(this)
                            .setTitle("Permiso negado")
                            .setMessage("No se mostrará su ubicación en el mapa debido a que negó el permiso")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Store locations info
        ArrayList<LatLng> locationsCoordinates = new ArrayList<LatLng>();
        ArrayList<String> locationsNames = new ArrayList<String>();

        // Read JSON file
        try {
            JSONObject obj = new JSONObject(loadJSON());
            JSONArray locationsArray = obj.getJSONArray("locationsArray");
            for (int i = 0; i < locationsArray.length(); i++) {
                JSONObject location = locationsArray.getJSONObject(i);

                String locationName = location.getString("name");
                locationsNames.add(locationName);

                double locationLatitude = Double.parseDouble(location.getString("latitude"));
                double locationLongitude = Double.parseDouble(location.getString("longitude"));
                locationsCoordinates.add(new LatLng(locationLatitude, locationLongitude));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add favorite locations to map
        for (int i = 0; i < locationsCoordinates.size(); i++) {
            LatLng coordinate = locationsCoordinates.get(i);
            String name = locationsNames.get(i);
            mMap.addMarker(new MarkerOptions().position(coordinate).title(name));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationsCoordinates.get(0)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
    }

    //--------------------------------------------------
    //              Menu
    //--------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {
            // Sign out, selected
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity( new Intent(this, LoginActivity.class) );
                return true;

            case R.id.menu_set_available:
               database.child(currentUserId).child("Disponible").get()
                       .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Get current state
                                    String currentState = String.valueOf(task.getResult().getValue());
                                    currentState = (currentState.equals("false")) ? "true" : "false";
                                    // Write new state
                                    database.child(currentUserId).child("Disponible").setValue(currentState);
                                    String message = "Su estado ahora es [ ";
                                    message += (currentState.equals("false")) ? "No disponible ]" : "Disponible ]";
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                return true;

            case R.id.menu_show_active_users:
                startActivity( new Intent(this, ShowUsersActivity.class) );
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

}