package com.example.taller2;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    //              Create menu
    //--------------------------------------------------
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
    //              Create menu
    //--------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return true;
    }

}