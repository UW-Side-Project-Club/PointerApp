package com.acromace.pointer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.ArrayList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


// TODO: Add a button to recenter the map at your your location (see Maps app)
// TODO: Separate out map marker creation to another function
// TODO: Refresh the list of Points when coming back from CreatePointActivity

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GetPointsCallbackInterface {

    private static final String TAG = "MainActivity";
    private FloatingActionButton fab;
    private GoogleMap googleMap;
    private Server server = new Server();

    public static final int LOCATION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.add_fab);

        setupFab();

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // Add a marker at current location and move the map's camera to the same location.
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.googleMap = googleMap;

        // Check GPS is enabled
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        // Check for permissions
        // TODO: After checking for permissions, don't start accessing the location until it's enabled
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted so request
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
        }

        final MainActivity self = this;

        LocationListener ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //when the location changes, update the map by zooming to the location
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Log.i(TAG, "Location changed: " + latitude + " " + longitude);

                //Asking for the points at the location
                server.getPoints(latitude, longitude, self);

                // TODO: Also zoom into the camera on the map
                LatLng loc = new LatLng(latitude, longitude);
                // TODO: Change the Google Maps pin to something that looks better
                googleMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title("Your Location"));
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        //Get Coordinates
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, ll);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    // getPointsResponse(success, points, errorMessage) puts points on the map, with success
    // indicating if the points have been successfully fetched. errorMessage if it is not a
    // success. If they have been successfully fetched, then points contains all of the points
    // in the requested area.
    public void getPointsResponse(final boolean success, final ArrayList<Point> points, final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO: Save the points here to load when the map loads
                if (googleMap == null ) {
                    Log.w(TAG, "Points loaded but map has not");
                    return;
                }
                clearPointsFromMap();

                for (Point point: points) {
                    LatLng loc = point.getPosition();
                    String msg = point.getMessage();
                    // TODO: Change the Google Maps pin to something that looks better
                    googleMap.addMarker(new MarkerOptions().position(loc).title(msg));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // This is used to add the debug button (see onOptionsItemSelected)
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // This adds the debug button to the action bar at the top right
            case R.id.action_debug:
                Intent intent = new Intent(this, DebugActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupFab() {
        final MainActivity mainActivity = this;
        // TODO: Send the current location to the CreatePointActivity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mainActivity, CreatePointActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clearPointsFromMap() {
        // TODO: Find out how to clear points from the map
    }
}
