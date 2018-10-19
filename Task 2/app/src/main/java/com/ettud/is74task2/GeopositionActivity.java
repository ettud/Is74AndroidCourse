package com.ettud.is74task2;

import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

//http://www.mapbox.com.s3-website-us-east-1.amazonaws.com/android-sdk/examples/user-location/

public class GeopositionActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, LocationEngineListener {
    private MapView              mMapView;
    private MapboxMap            mMap;
    private PermissionsManager   mPermissionsManager;
    private LocationEngine       mLocationEngine;
    private LocationLayerPlugin  mLocationLayerPlugin;
    private Location             mCurLocation;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.map_box_access_token));
        setContentView(R.layout.activity_geoposition);
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
            }
        });
    }

    @Override
    public void onMapReady(final MapboxMap mapboxMap) {
        mMap = mapboxMap;
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(55.1891368, 61.3568625)).title("Бизнес-центр \"Эталон\"").snippet("Штаб-квартира Интерсвязи")
        );
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(55.1774669, 61.3188811)).title("ЧелГУ").snippet("Курсы Интерсвязи")
        );
        enableLocationPlugin();
        mLocationLayerPlugin = new LocationLayerPlugin(mMapView, mMap, mLocationEngine);
        mLocationLayerPlugin.setLocationLayerEnabled(true);
        mLocationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        mLocationLayerPlugin.setRenderMode(RenderMode.COMPASS);
        getLifecycle().addObserver(mLocationLayerPlugin);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mapboxMap.getUiSettings().setZoomGesturesEnabled(true);
        mapboxMap.getUiSettings().setScrollGesturesEnabled(true);
        mapboxMap.getUiSettings().setAllGesturesEnabled(true);
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            animateCamera(location);
        }
    }

    public void animateCamera(Location location) {
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13.0));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (mLocationEngine!=null)
        {
            mLocationEngine.deactivate();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationPlugin() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngineLaunch();
        }
        else{
            mPermissionsManager = new PermissionsManager(this);
            mPermissionsManager.requestLocationPermissions(this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.geolocation_access_denied, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPermissionResult(boolean granted) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngineLaunch();
            return;
        }
        Toast.makeText(this, R.string.geolocation_access_denied, Toast.LENGTH_LONG).show();
        finish();
    }

    @SuppressLint("MissingPermission")
    void locationEngineLaunch(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mLocationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
            mLocationEngine.requestLocationUpdates();
            mLocationEngine.setPriority(LocationEnginePriority.BALANCED_POWER_ACCURACY);
            mLocationEngine.setInterval(0);
            mLocationEngine.activate();
            getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    void getLastLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Location lastLocation = mLocationEngine.getLastLocation();
            if (lastLocation != null) {
                mCurLocation = lastLocation;
                animateCamera(lastLocation);
            } else {
                mLocationEngine.addLocationEngineListener(this);
                mLocationEngine.requestLocationUpdates();
            }
        }
    }
}