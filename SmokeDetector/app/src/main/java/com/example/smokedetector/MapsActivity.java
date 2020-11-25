package com.example.smokedetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private FusedLocationProviderClient client;
    private final float ZOOM = 16;
    private GoogleMap mMap;

    private Location newestLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<LatLng> locationList = new ArrayList<LatLng>();
    private List<LatLng> smokeList = new ArrayList<LatLng>();
    private List<LatLng> allergyList = new ArrayList<LatLng>();

    List<LatLng> smokeRes = new ArrayList<>();
    List<LatLng> allergyRes = new ArrayList<>();

    RequestQueue requestQueue;

    private LatLng preLocation = null;
    private LatLng curLocation = null;

    private Handler handler = new Handler();

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        requestQueue= Volley.newRequestQueue(MapsActivity.this);
        Button btn1 = findViewById(R.id.Button1);
        Button btn2 = findViewById(R.id.Button2);

        textView = findViewById(R.id.display);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curLocation!=null){
                    SendSmokeData(curLocation);
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curLocation!=null){
                    SendAllergyData(curLocation);
                }

            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        View mapView = mapFragment.getView();

        client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);//set the map refresh rate
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,2000);
                if (curLocation!=null){
                    swapList(curLocation);
                }
            }
        };
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null){
                    return;
                }
                newestLocation = locationResult.getLastLocation();

                boolean issmoke = checkSmoke(newestLocation);
                boolean isallergy = checkAllergy(newestLocation);
                if (issmoke){
                    textView.setText("Smoke Area");
                }
                if (isallergy){
                    Vibrator vibrator = (Vibrator) MapsActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
                    //vibrator.vibrate(1000);
                    textView.setText("Allergy Area");
                }
                if (!isallergy && !issmoke){
                    textView.setText("Clear");
                }


                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(newestLocation.getLatitude(),newestLocation.getLongitude()),ZOOM));

                locationList.add(new LatLng(newestLocation.getLatitude(),newestLocation.getLongitude()));

                if (preLocation == null){
                    preLocation = locationList.get(locationList.size()-1);
                }
                else{
                    curLocation = locationList.get(locationList.size()-1);
                }
            }
        };
        runnable.run();
    }

    private boolean checkSmoke(Location curLocation){
        boolean isSmoke = false;
        double smokeRange = 20.0;
        if (!smokeList.isEmpty()){
            for (int i = 0;i<smokeList.size();i++){
                LatLng tmpCoor = smokeList.get(i);
                double distance = calDistance(tmpCoor.latitude,tmpCoor.longitude,curLocation.getLatitude(),curLocation.getLongitude());
                if (distance<smokeRange){
                    isSmoke = true;
                }
            }
        }
        return isSmoke;
    }

    private boolean checkAllergy(Location curLocation){
        boolean isAllergy = false;
        double allergyRange = 100.0;
        if (!allergyList.isEmpty()){
            for (int i = 0;i<allergyList.size();i++){
                LatLng tmpCoor = allergyList.get(i);
                double distance = calDistance(tmpCoor.latitude,tmpCoor.longitude,curLocation.getLatitude(),curLocation.getLongitude());
                if (distance<allergyRange){
                    Vibrator vibrator = (Vibrator) MapsActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
                    isAllergy = true;
                }
            }
        }
        return isAllergy;
    }

    private void SendSmokeData(LatLng curLocation){
        double lat = curLocation.latitude;
        double lng = curLocation.longitude;
        Uri.Builder builder = Uri.parse("SampleUrl/CoorDetector_war/home/setsmoke").buildUpon();
        builder.appendQueryParameter("lat",String.valueOf(lat));
        builder.appendQueryParameter("lng",String.valueOf(lng));
        String url = "SampleUrl/CoorDetector_war/home/setsmoke?lat="+String.valueOf(lat)+"&lng="+String.valueOf(lng);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void SendAllergyData(LatLng curLocation){
        double lat = curLocation.latitude;
        double lng = curLocation.longitude;
        Uri.Builder builder = Uri.parse("SampleUrl/CoorDetector_war/home/setallergy").buildUpon();
        builder.appendQueryParameter("lat",String.valueOf(lat));
        builder.appendQueryParameter("lng",String.valueOf(lng));
        String url = "SampleUrl/CoorDetector_war/home/setallergy?lat="+String.valueOf(lat)+"&lng="+String.valueOf(lng);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Success", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void swapList(LatLng curLocation){
        double lat = curLocation.latitude;
        double lng = curLocation.longitude;

        String smokeUrl = "SampleUrl/CoorDetector_war/home/findsmoke?lat="+String.valueOf(lat)+"&lng="+String.valueOf(lng);
        String allergyUrl = "SampleUrl/CoorDetector_war/home/findallergy?lat="+String.valueOf(lat)+"&lng="+String.valueOf(lng);
        JsonObjectRequest jsonObjectRequestSmoke = new JsonObjectRequest(Request.Method.GET, smokeUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("ans");
                    if (jsonArray.length()!=0){
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            double lat = jsonObject.getDouble("lat");
                            double lng = jsonObject.getDouble("lng");
                            LatLng coor = new LatLng(lat,lng);
                            MapsActivity.this.smokeRes.add(coor);
                        }
                    }
                } catch (JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        JsonObjectRequest jsonObjectRequestAllergy = new JsonObjectRequest(Request.Method.GET, allergyUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("ans");
                    if (jsonArray.length()!=0){
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            double lat = jsonObject.getDouble("lat");
                            double lng = jsonObject.getDouble("lng");
                            LatLng coor = new LatLng(lat,lng);
                            MapsActivity.this.allergyRes.add(coor);
                        }
                    }
                } catch (JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequestSmoke);
        requestQueue.add(jsonObjectRequestAllergy);

        mMap.clear();

        if (smokeRes.size()!=0){
            smokeList = smokeRes;
            for (int i = 0;i<smokeRes.size();i++){
                CircleOptions circleOptions = new CircleOptions().center(new LatLng(smokeRes.get(i).latitude, smokeRes.get(i).longitude)).radius(20).fillColor(0xfd00ffff).strokeColor(Color.TRANSPARENT).strokeWidth(2);
                Circle circle = mMap.addCircle(circleOptions);
            }
        }

        if (allergyRes.size()!=0){
            allergyList = allergyRes;
            for (int i = 0;i<allergyRes.size();i++){
                CircleOptions circleOptions = new CircleOptions().center(new LatLng(allergyRes.get(i).latitude, allergyRes.get(i).longitude)).radius(100).fillColor(0x4dff0000).strokeColor(Color.TRANSPARENT).strokeWidth(2);
                Circle circle = mMap.addCircle(circleOptions);
            }
        }
        allergyRes.clear();
        smokeRes.clear();
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    private double calDistance(double lat1, double lng1, double lat2, double lng2) {
        double EARTH_RADIUS = 6371.393;
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 1000);
        return s;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"No permission",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},99);//request the permission
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        client.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }

}