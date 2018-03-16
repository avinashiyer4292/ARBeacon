package com.avinash.arbeacon.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.avinash.arbeacon.ARBeaconApplication;
import com.avinash.arbeacon.R;
import com.avinash.arbeacon.services.BeaconDetectionService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by avinashiyer on 11/9/17.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener{
        private String time;
        private GoogleMap mMap;
        ArrayList<LatLng> markerPoints;
        GoogleApiClient mGoogleApiClient;
        Location mLastLocation;
        Marker mCurrLocationMarker;
        LocationRequest mLocationRequest;
        LatLng bearingLoc;

        private  static  final String TAG = "MapsActivity";
        boolean isGPS = false;
        boolean isNetwork = false;
        boolean canGetLocation = true;
        LocationManager locationManager;
        Location loc;
        private final static int ALL_PERMISSIONS_RESULT = 101;
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
        private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
        float bearing;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Toast.makeText(MapsActivity.this, "Please enter a destination!", Toast.LENGTH_LONG).show();
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(MapsActivity.this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COARSE_LOCATION );
        }
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Initializing
        markerPoints = new ArrayList<>();

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

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.maps_style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

            //LatLng current = new LatLng(29.6479620, -82.3440310);

    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

//
//    public static BitmapDescriptor getCustomMarker(Context context, Icon icon, double scale, int color){
//        IconDrawable id=new IconDrawable(context,icon).actionBarSize();
//        id.color(color);
//        Drawable d=id.getCurrent();
//        Bitmap bm=Bitmap.createBitmap(id.getIntrinsicWidth(),id.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
//        if (scale != 1)   bm=Bitmap.createScaledBitmap(bm,id.getIntrinsicWidth(),id.getIntrinsicHeight(),false);
//        Canvas c=new Canvas(bm);
//        d.draw(c);
//        return BitmapDescriptorFactory.fromBitmap(bm);
//    }

    private void drawCircle(LatLng location) {
        CircleOptions options = new CircleOptions();
        options.center(location);
        //Radius in meters
        options.radius(50);
        options.fillColor(getResources()
                .getColor(R.color.fill_color));
        options.strokeColor(getResources()
                .getColor(R.color.stroke_color));
        options.strokeWidth(5);
        mMap.addCircle(options);
    }

    private void getFixedParkingSpots(LatLng dest, int distance){



//        l = new LatLng(29.646547, -82.348001);
//        marker = mMap.addMarker(new MarkerOptions().position(l).title("2").
//                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
//        marker.setTag("green");
//        markerPoints.add(l);
//
//        l = new LatLng(29.649013, -82.343314);
//        marker = mMap.addMarker(new MarkerOptions().position(l).title("2").
//                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
//        marker.setTag("green");
//        markerPoints.add(l);
//
//
//        l = new LatLng(29.649013, -82.343314);
//        marker = mMap.addMarker(new MarkerOptions().position(l).title("2").
//                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
//        marker.setTag("green");
//        markerPoints.add(l);


//        l = new LatLng(dest.latitude+0.016, dest.longitude);
//        marker = mMap.addMarker(new MarkerOptions().position(l).title("1").
//                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
//        marker.setTag("green");
//        markerPoints.add(l);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

               LatLng l = new LatLng(29.648248, -82.343990);
//        bearingLoc = l;
        Marker marker = mMap.addMarker(new MarkerOptions().position(l).title("1").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
        marker.setTag("green");
        markerPoints.add(l);

        l = new LatLng( 29.646564, -82.347352);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("1").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
        marker.setTag("green");
        markerPoints.add(l);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(17));


        l = new LatLng(dest.latitude-0.0102, dest.longitude-0.0015);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("2").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
        marker.setTag("green");
        markerPoints.add(l);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(14));


        l = new LatLng(dest.latitude-0.022, dest.longitude-0.011);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("3").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
        marker.setTag("green");
        markerPoints.add(l);


        l = new LatLng(dest.latitude+0.013, dest.longitude+0.01345);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("4").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
        marker.setTag("green");
        markerPoints.add(l);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


        l = new LatLng(dest.latitude, dest.longitude+0.034);
        marker = mMap.addMarker(new MarkerOptions().position(l).title("5").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.beacon_marker)));
        marker.setTag("green");
        markerPoints.add(l);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        for(LatLng loc: markerPoints)
            drawCircle(loc);
    }

    private void getAllParkingSpots(LatLng dest, int distance){
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    public LatLng getRandomLocation(LatLng point, int radius) {

        List<LatLng> randomPoints = new ArrayList<>();
        List<Float> randomDistances = new ArrayList<>();
        Location myLocation = new Location("");
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);

        //This is to generate 10 random points
        for(int i = 0; i<10; i++) {
            double x0 = point.latitude;
            double y0 = point.longitude;

            Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 11100f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(y0);

            double foundLatitude = new_x + x0;
            double foundLongitude = y + y0;
            LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
            randomPoints.add(randomLatLng);
            Location l1 = new Location("");
            l1.setLatitude(randomLatLng.latitude);
            l1.setLongitude(randomLatLng.longitude);
            randomDistances.add(l1.distanceTo(myLocation));
        }
        //Get nearest point to the centre
        int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));
        return randomPoints.get(indexOfNearestPointToCentre);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(MapsActivity.this, "on location changed called!", Toast.LENGTH_SHORT).show();
        mLastLocation = location;
        Location SLoc  = new Location("source");
        Location DLoc  = new Location("destination");
        SLoc.setLatitude(location.getLatitude());
        SLoc.setLongitude(location.getLongitude());

        DLoc.setLatitude(29.645426);  //msl 29.648248, -82.343990
        DLoc.setLongitude(-82.347753); //reitz 29.649013, -82.343314

        //bookstrore 29.645426, -82.347753

        bearing = SLoc.bearingTo(DLoc);
        if(bearing<0)
            bearing+=360;
        Log.d("Location", String.valueOf(SLoc.bearingTo(DLoc)));
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        getFixedParkingSpots(latLng, 500);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPoints.get(1)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        List<LatLng> demoMarkerPoints = new ArrayList<>();
        demoMarkerPoints.add(new LatLng(29.647771, -82.344528));
        demoMarkerPoints.add(new LatLng(29.647608, -82.344749));
        demoMarkerPoints.add(new LatLng(29.647070, -82.345722));
        demoMarkerPoints.add(new LatLng(29.646331, -82.347739));




//        animateMarker(mCurrLocationMarker, demoMarkerPoints.get(1), false, 6000);
//        animateMarker(mCurrLocationMarker, demoMarkerPoints.get(2), false, 9000);
        animateMarker(mCurrLocationMarker, demoMarkerPoints.get(3), false, 5000);

        showNotification();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MapsActivity.this, CameraPreviewActivity2.class);
                i.putExtra("bearing",bearing);
                startActivity(i);
                finish();
            }
        },10000);

//        Intent i = new Intent(MapsActivity.this, BeaconDetectionService.class);
//        MapsActivity.this.startService(i);

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker, int delay) {
        //Toast.makeText(MapsActivity.this, "Animating marker...", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 100;

        final Interpolator interpolator = new LinearInterpolator();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        },delay);

    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void showNotification(){
        Intent notifyIntent = new Intent(this, CameraPreviewActivity2.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{notifyIntent},
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon_24)
                .setContentTitle("AR Beacons Campus Tours!")
                .setContentText("You are near a point of interest!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}

//    private void addFriendMarkers() {
////        LatLng l = new LatLng(35.905154, -79.051382);
////        MarkerOptions options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face1)));
////        mMap.addMarker(options);
////
////        l = new LatLng(35.912019, -79.045052);
////        options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face2)));
////        mMap.addMarker(options);
////
////        l = new LatLng(35.902432, -79.046435);
////        options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face3)));
////        mMap.addMarker(options);
////
////        l = new LatLng(35.908336, -79.039329);
////        options = new MarkerOptions().position(l);
////        options.icon(BitmapDescriptorFactory.fromBitmap(
////                BitmapFactory.decodeResource(getResources(),
////                        R.drawable.face4)));
////        mMap.addMarker(options);
//
//
//    }
//
//


