package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    // User location
    private LatLng currentLocation = null;
    private FusedLocationProviderClient userLocation;

    // Firebase
    private FirebaseAuth userAuth;
    private DatabaseReference database;
    private String currentUserId = "";

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
        database = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);


        // Create listener for location in users ...


        // Create notification


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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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


    //--------------------------------------------------
    //             Methods
    //--------------------------------------------------
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
               database.child("Disponible").get()
                       .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Get current state
                                    String currentState = String.valueOf(task.getResult().getValue());
                                    currentState = (currentState.equals("false")) ? "true" : "false";
                                    // Write new state
                                    database.child("Disponible").setValue(currentState);
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