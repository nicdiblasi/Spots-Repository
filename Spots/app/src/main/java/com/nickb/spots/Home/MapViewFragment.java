package com.nickb.spots.Home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nickb.spots.R;
import com.nickb.spots.models.Location;

import java.util.ArrayList;



public class MapViewFragment extends Fragment {

    private static final String TAG = "MapViewFragment";



    MapView mMapView;
    private GoogleMap googleMap;
    private ArrayList<Location> spotLocations;

    // Fire base
    DatabaseReference mReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        spotLocations = new ArrayList<>();
        mReference = FirebaseDatabase.getInstance().getReference();

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately


        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "onCreateView: permissions granted");

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    Log.d(TAG, "onMapReady: initialising map");

                    googleMap = mMap;

                    // For showing a move to my location button
                    googleMap.setMyLocationEnabled(true);

                    getMarkers();

                    // For zooming automatically to the location of the marker
                    // CameraPosition cameraPosition = new CameraPosition.Builder().target().zoom(12).build();
                    // googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                }
            });
        } else {
            Log.d(TAG, "onCreateView: permissions not granted");
        }

        return view;
    }




    private void getMarkers() {
        Query query = mReference
                .child(getString(R.string.dbname_spots))
                .orderByChild(getString(R.string.field_location));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    Log.d(TAG, "onDataChange: found spot: " + singleSnapshot);

                    Log.d(TAG, "onDataChange: location: " + singleSnapshot.child(getString(R.string.field_location)));

                    double latitude = (double) singleSnapshot.child(getString(R.string.field_location)).child(getString(R.string.field_latitude)).getValue();
                    double longitude = (double) singleSnapshot.child(getString(R.string.field_location)).child(getString(R.string.field_longitude)).getValue();
                    String feature = (String) singleSnapshot.child(getString(R.string.field_location)).child(getString(R.string.field_feature)).getValue();



                    Location location = new Location(latitude, longitude, feature);
                    spotLocations.add(location);
                }
                addMarkers(spotLocations);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void addMarkers(ArrayList<Location> spotLocations) {
        for (int i = 0; i < spotLocations.size(); i++) {
            LatLng latLng = new LatLng(spotLocations.get(i).getLatitude(), spotLocations.get(i).getLongitude());
            googleMap.addMarker(new MarkerOptions()
            .position(latLng))
            .setTitle(spotLocations.get(i).getFeature());
        }
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
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}


