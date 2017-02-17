package cz.uhk.fim.soucera.geocatcher.features;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.Caches_DB;
import cz.uhk.fim.soucera.geocatcher.R;
import cz.uhk.fim.soucera.geocatcher.utils.Utils;
import cz.uhk.fim.soucera.geocatcher.waypoints.Waypoint;


public class PointToPointActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SensorEventListener {

    float[] mGravity;
    float[] mGeomagnetic;
    private double azimut = 0;
    private Location currentLoc;
    private Location targetLoc;
    private GoogleApiClient mGoogleApiClient;
    private float currentDirection = 0f;
    private ImageView imagearrow;
    private TextView tvDistance;
    private TextView tvCacheName;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pto_p);

        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }
            initializeLocationSettings();

            mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
                // Success! There's a magnetometer.
                magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }
            else {
                // Failure! No magnetometer.
                Toast.makeText(this, "No sensor for MAGNETIC FIELD!", Toast.LENGTH_LONG).show();
            }

            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
                accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            else {
                Toast.makeText(this, "No sensor for ACCELEROMETER!", Toast.LENGTH_LONG).show();
            }

            tvDistance = (TextView) findViewById(R.id.tv_distance);
            imagearrow = (ImageView) findViewById(R.id.iv_compassarrow);
            tvCacheName = (TextView) findViewById(R.id.tv_cache_name_navigate);
            double lat = getIntent().getDoubleExtra("lat", 0);
            double lon = getIntent().getDoubleExtra("long", 0);
            int idCache = getIntent().getIntExtra("idCache", 0);
            int idWpt = getIntent().getIntExtra("idWpt", 0);
            if(idCache > 0){
                Caches_DB caches_db = new Caches_DB(this);
                Cache cache = caches_db.getCache(idCache);
                caches_db.close();
                tvCacheName.setText(cache.getName());
            } else if (idWpt > 0){
                Caches_DB caches_db = new Caches_DB(this);
                Waypoint wpt = caches_db.getWpt(idWpt);
                caches_db.close();
                tvCacheName.setText(wpt.getDesc());
            } else {
                tvCacheName.setText("-");
            }

            targetLoc = new Location("target");
            targetLoc.setLatitude(lat);
            targetLoc.setLongitude(lon);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return true;
        }
        return true;
    }

    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void sleduj(Location target) {
        azimut = Math.toDegrees(azimut);
        GeomagneticField geoField = new GeomagneticField(
                Double.valueOf(currentLoc.getLatitude()).floatValue(),
                Double.valueOf(currentLoc.getLongitude()).floatValue(),
                Double.valueOf(currentLoc.getAltitude()).floatValue(),
                System.currentTimeMillis());
        azimut += geoField.getDeclination(); // converts magnetic north into true north
        float bearing = currentLoc.bearingTo(target); // (it's already in degrees)
        float direction = (float) azimut - bearing;
        direction = direction % 360;

        final RotateAnimation rotateAnim = new RotateAnimation(-currentDirection, -direction, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        currentDirection = direction;


        rotateAnim.setDuration(1000);
        rotateAnim.setFillAfter(true);
        rotateAnim.setInterpolator(new LinearInterpolator());
        imagearrow.startAnimation(rotateAnim);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = event.values;
            }

            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimut = orientation[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            System.out.println("ON CONNECTED");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initializeLocationSettings() {
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                            System.out.println("BuildGoogleAPI___________________________");
                        }
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            currentLoc = location;
            sleduj(targetLoc);
            LatLng myLocLatLon = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
            LatLng destinationCoord = new LatLng(targetLoc.getLatitude(), targetLoc.getLongitude());
            tvDistance.setText("" + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
