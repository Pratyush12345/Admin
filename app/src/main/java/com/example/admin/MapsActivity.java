package com.example.admin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Map;

import io.opencensus.trace.export.SpanExporter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private FirebaseDatabase database;
    private DatabaseReference mRef, mdriver,mNextRef,mgetkey;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLoactionMarker,startLoactionMarker,endLoactionMarker,coveredLoactionMarker;
    public static final int REQUEST_LOCATION_CODE_ = 99;
    double startlat, startlon, currentlan, currentlon,Coverlan=1.1,Coverlon=1.1,prevCoverlan,prevCoverlon,prevCurrentlan=1.1,prevCurrentlon=1.1;
    double endlat, endlon, capacity,CheckCapacity;
    private UserActivity user,dupuser;
    private String Zone,Ward,Subward, subchild;
    int counter;
    double smallest ;
    private FirebaseUser currentuser;
    private String uid;
    int count=0;
    private TextView String1, String2, String3;
    long above60left=0,covereddustbins=0,online=0,firsttime=0;
    Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Zone=getIntent().getStringExtra("Zone");
        Ward=getIntent().getStringExtra("Ward");
        Subward=getIntent().getStringExtra("Subward");

        String1=(TextView)findViewById(R.id.textView3);
        String2=(TextView)findViewById(R.id.textView4);
        String3=(TextView)findViewById(R.id.textView5);

        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        uid = currentuser.getUid();
        mdriver= FirebaseDatabase.getInstance().getReference("Surat").child(Zone).child(Ward).child(Subward).child("Driver");
        user = new UserActivity();
        dupuser = new UserActivity();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        track();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE_:
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    //permission granted
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(client==null)
                        {
                            buildGoogleApiClient();
                        }
                    }mMap.setMyLocationEnabled(true);
                }
                else //permission denied
                {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_LONG).show();
                }
                //return;

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

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    protected synchronized void buildGoogleApiClient()
    {
        client =new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }




    @Override
    public void onLocationChanged (Location location){


    }//on location changed


    public void track()
    {

        mdriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentlan=(Double) dataSnapshot.child("Currentlan").getValue();
                currentlon=(Double) dataSnapshot.child("Currentlon").getValue();

                startlat=(Double) dataSnapshot.child("Startlan").getValue();
                startlon=(Double) dataSnapshot.child("Startlon").getValue();


                endlat=(Double) dataSnapshot.child("Endlan").getValue();
                endlon=(Double) dataSnapshot.child("Endlon").getValue();


                if(currentLoactionMarker!=null) {
                    currentLoactionMarker.remove();
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(currentlan, currentlon));
                markerOptions.title("Current location");
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.truck));
                currentLoactionMarker = mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentlan, currentlon)));
                //mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

                online=(long)dataSnapshot.child("online").getValue();
                if(online==0&&firsttime==0)
                {
                    if(i==null) {
                        i = new Intent(MapsActivity.this, pop.class);
                        startActivity(i);

                    }
                    online=1;
                    firsttime=1;

                }
                else if(online==1)
                    {
                    firsttime=0;
                }

                    if(startLoactionMarker!=null)
                    startLoactionMarker.remove();

                    MarkerOptions markerOptions1 = new MarkerOptions();
                    markerOptions1.position(new LatLng(startlat, startlon));
                    markerOptions1.title("Start location ");
                    markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    startLoactionMarker = mMap.addMarker(markerOptions1);
                    mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(startlat, startlon)));

                    if(endLoactionMarker!=null)
                    endLoactionMarker.remove();

                    MarkerOptions markerOptions2 = new MarkerOptions();
                    markerOptions2.position(new LatLng(endlat, endlon));
                    markerOptions2.title("Dustbin to be reached");
                    markerOptions2.icon(BitmapDescriptorFactory.fromResource(R.drawable.redmarker));
                    endLoactionMarker = mMap.addMarker(markerOptions2);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(endlat, endlon)));

                    LatLng end=new LatLng(endlat,endlon);
                    LatLng start=new LatLng(startlat,startlon);
                    PolylineOptions poption=new PolylineOptions().add(start).add(end).width(8).color(Color.GRAY).geodesic(true);
                    mMap.addPolyline(poption);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start,13));

                Coverlan=(Double) dataSnapshot.child("Coverlan").getValue();
                Coverlon=(Double) dataSnapshot.child("Coverlon").getValue();


                if(Coverlon!=1.1 && Coverlan!=1.1&&prevCoverlan==Coverlan&& prevCoverlon!=Coverlon) {

                            MarkerOptions markerOptions3 = new MarkerOptions();
                            markerOptions3.position(new LatLng(Coverlan, Coverlon));
                            markerOptions3.title("covered Dustbin");
                            markerOptions3.icon(BitmapDescriptorFactory.fromResource(R.drawable.blackmarker));
                            coveredLoactionMarker = mMap.addMarker(markerOptions3);
                            //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(startlat, startlon)));
                        }
                prevCoverlan=Coverlan;
                prevCoverlon=Coverlon;

                above60left=(long)dataSnapshot.child("above60").getValue();
                covereddustbins=(long)dataSnapshot.child("covereddustbins").getValue();

                String1.setText("Dustbins above 60 percent   "+(above60left+covereddustbins));
                String2.setText("Covered Dustbins                    "+covereddustbins);
                String3.setText("Dustbins to be covered          "+above60left);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }//track



   /* public void refresh(int milliseconds)
    {

        final Handler handler=new Handler();
        final Runnable runnable=new Runnable() {
            @Override
            public void run()  {
                optimal();
            }
        };
        handler.postDelayed(runnable,milliseconds);
    }*/

    /*private String getDirectionUrl()
    {
        StringBuilder googleDirectionsUrl =new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+startlat+","+startlon);
        googleDirectionsUrl.append("&destination="+endlat+","+endlon);
        googleDirectionsUrl.append("&key="+"AIzaSyCbXNt5isxGgBSi_N5Zu0YrLf9GFN7csww");
        return googleDirectionsUrl.toString();
    }*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }

    }
    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE_);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE_);
            }
            return false;

        }
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
