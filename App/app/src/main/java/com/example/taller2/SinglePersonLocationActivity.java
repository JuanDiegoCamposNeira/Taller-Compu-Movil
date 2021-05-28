package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

public class SinglePersonLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    //----------------------------------------------
    //              Attributes
    //----------------------------------------------
    // Map
    private GoogleMap mMap;
    private double distanceBetweenUsers = -1;

    // User location
    private FusedLocationProviderClient userLocation;
    private LatLng currentUserLocation;
    private static final String[] locationPermissions = new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
    private static final int LOCATION_ACCESS_CODE = 101;

    // Other user info
    private LatLng otherUserLocation;
    private String otherUserName;

    // Firebase
    private FirebaseAuth userAuth;
    private DatabaseReference database;
    private String currentUserId = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_person_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Configurations
        userAuth = FirebaseAuth.getInstance();
        currentUserId = userAuth.getUid();
        database = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);


        // Get other user's location passed by the information on the intent
        Intent intent = getIntent();
        otherUserName = intent.getStringExtra("nombre");
        double otherUserLat = Double.parseDouble(intent.getStringExtra("lat"));
        double otherUserLng = Double.parseDouble(intent.getStringExtra("lng"));
        otherUserLocation = new LatLng(otherUserLat, otherUserLng);

        //

    }

    //--------------------------------------------------
    //                  Methods
    //--------------------------------------------------
    // Code snnipet taken from :
    // https://www.geeksforgeeks.org/program-distance-two-points-earth/
    private void calculateDistanceBeweenTwoCoordinates() {
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        // Current user
        double lon1 = Math.toRadians(currentUserLocation.longitude);
        double lat1 = Math.toRadians(currentUserLocation.latitude);
        // Other user
        double lon2 = Math.toRadians(otherUserLocation.longitude);
        double lat2 = Math.toRadians(otherUserLocation.latitude);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        distanceBetweenUsers = (c * r);
    }

    private void getLastKnownLocation() {
        userLocation = LocationServices.getFusedLocationProviderClient(this);

        // Get current user location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            new AlertDialog.Builder(this)
                    .setTitle("Permiso para acceder a la localización requerido")
                    .setMessage("Este permiso permite a la aplicación acceder a su ubicación, para mostrarlo en el mapa")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SinglePersonLocationActivity.this, locationPermissions, LOCATION_ACCESS_CODE);
                        }
                    })
                    .setNegativeButton("Negar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
            return;
        }

        userLocation.getLastLocation()
                .addOnSuccessListener(SinglePersonLocationActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // Get user's location
                            getLastKnownLocation();
                            Log.i("USERLOC", currentUserLocation.toString());
                            // Calculate distance
                            calculateDistanceBeweenTwoCoordinates();
                            String distanceMessage = "Distancia : " + String.valueOf(distanceBetweenUsers) + " km ";

                            // Add marker to current user location
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentUserLocation)
                                    .title("Localización actual del usuario")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentUserLocation));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(14));

                            // Add marker in other user's location
                            mMap.addMarker( new MarkerOptions()
                                    .title(otherUserName)
                                    .snippet(distanceMessage)
                                    .position(otherUserLocation));

                        }
                    }
                });

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

        // Get user's location
        getLastKnownLocation();
//        Log.i("USERLOC", currentUserLocation.toString());
//        // Calculate distance
//        calculateDistanceBeweenTwoCoordinates();
//        String distanceMessage = "Distancia : " + String.valueOf(distanceBetweenUsers);
//
//        // Add marker to current user location
//        mMap.addMarker(new MarkerOptions()
//                .position(currentUserLocation)
//                .title("Localización actual del usuario")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentUserLocation));
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
//
//        // Add marker in other user's location
//        mMap.addMarker( new MarkerOptions()
//            .title(otherUserName)
//            .snippet(distanceMessage)
//            .position(otherUserLocation));
    }


    //--------------------------------------------------
    //                  Menu
    //--------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            // Sign out, selected
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
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
                                    Toast.makeText(SinglePersonLocationActivity.this, message, Toast.LENGTH_SHORT).show();
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