package com.example.bowlmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.net.Uri;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Color;
import android.view.View;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bowlmate.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.Task;
import android.Manifest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BowlingLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private FusedLocationProviderClient client;
    private LatLng currentLatLng;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 44;

    private static final String API_KEY = "AIzaSyDr64tr-Y3YopYDi7PmbUou96Q0o3wSYlI";
    private LinearLayout locationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(this);

        locationList = findViewById(R.id.location_list);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(BowlingLocation.this, AboutApp.class);
            startActivity(intent);
        });
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));

                // 🔥 Now it's safe to call this:
                searchNearby("bowling_alley");

            } else {
                Toast.makeText(BowlingLocation.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                getCurrentLocation();
            }
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchNearby(String type) {
        if (currentLatLng == null) {
            getCurrentLocation(); // get location first then search
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + currentLatLng.latitude + "," + currentLatLng.longitude +
                "&radius=15000&type=" + type +
                "&key=" + API_KEY;

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
        locationList.removeAllViews(); // clear previous views

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<LocationItem> items = new ArrayList<>();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                            String name = place.getString("name");
                            String address = place.optString("vicinity", "No address");

                            LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                            float[] resultsDist = new float[1];
                            Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude,
                                    latLng.latitude, latLng.longitude, resultsDist);
                            float distanceMeters = resultsDist[0];

                            items.add(new LocationItem(name, address, latLng, distanceMeters));
                        }

                        // Sort by distance (nearest to farthest)
                        Collections.sort(items, Comparator.comparingDouble(item -> item.distance));

                        for (LocationItem item : items) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(item.latLng)
                                    .title(item.name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

                            addLocationCard(item);
                        }

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));

                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing results", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void addLocationCard(LocationItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.rounded_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(Color.BLACK);
        card.addView(name);

        TextView distance = new TextView(this);
        distance.setText(String.format("(%.1f km)", item.distance / 1000));
        distance.setTextSize(14);
        distance.setTextColor(Color.RED);
        distance.setPadding(0, 4, 0, 0);
        card.addView(distance);

        TextView address = new TextView(this);
        address.setText(item.address);
        address.setTextSize(14);
        address.setTextColor(Color.DKGRAY);
        address.setPadding(0, 8, 0, 0);
        card.addView(address);

        card.setOnClickListener(v -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.latLng, 15));
            mMap.addMarker(new MarkerOptions()
                    .position(item.latLng)
                    .title(item.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        });

        locationList.addView(card);
    }

    // Inner class to hold data
    private static class LocationItem {
        String name, address;
        LatLng latLng;
        float distance;

        LocationItem(String n, String a, LatLng l, float d) {
            name = n;
            address = a;
            latLng = l;
            distance = d;
        }
    }
}