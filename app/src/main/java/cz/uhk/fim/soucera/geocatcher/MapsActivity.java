package cz.uhk.fim.soucera.geocatcher;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.uhk.fim.soucera.geocatcher.caches.SelectCachesActivity;
import cz.uhk.fim.soucera.geocatcher.caches.SelectDetailCacheActivity;
import cz.uhk.fim.soucera.geocatcher.features.PointToPointActivity;
import cz.uhk.fim.soucera.geocatcher.waypoints.DetailWptActivity;
import cz.uhk.fim.soucera.geocatcher.utils.Utils;
import cz.uhk.fim.soucera.geocatcher.waypoints.Waypoint;

import static cz.uhk.fim.soucera.geocatcher.R.id.map;
import static cz.uhk.fim.soucera.geocatcher.R.id.textViewMapDistance;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener,
        PopupMenu.OnMenuItemClickListener, ResultCallback<Status> {

    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker marker;
    private ArrayList<Marker> markers;
    private ArrayList<Marker> routePoints;
    private Cache followCache;
    private Waypoint followWpt;
    private static String[] PERMISSIONS_MAPS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final float ZOOM_BASE_VALUE = 14f;
    private static final int SELECT_CACHE_ACTIVITY = 1000;
    private static final CharSequence[] MAP_TYPE_ITEMS = {"Normal", "Satellite", "Hybrid", "Terrain"};
    private static final CharSequence[] GEOFENCE_RADIUS_ITEMS = {"50", "100", "150", "300"};
    private static final CharSequence[] CACHES_TYPE_ITEMS = {"All", "Traditional", "Multi", "Mystery"};
    private static final CharSequence[] FILTER_FIND_ITEMS = {"Všechny keše", "Nalezené keše", "Nenalezené keše"};
    private float previousZoomLevel = -1.0f;
    int checkItemRadius;
    int checkItemMapType;
    private boolean isZooming = false;
    private boolean isMoving = false;
    private boolean isMarkerFollow = false;
    private boolean isGeofencingEnabled;
    private boolean isShowWptsEnabled;
    private boolean isShortestWayEnabled;
    private TextView viewDistance;
    private int pomIndex;
    private
    @Nullable
    PopupMenu popup;

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static float GEOFENCE_RADIUS = 100.0f; // in meters
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private int filterFind;
    private int filterType;
    private static final int FILTER_ALL = 0;
    private static final int FILTER_FOUND = 1;
    private static final int FILTER_NOT_FOUND = 2;
    private static final int LIST_NALEZENE = 1;
    private static final int FILTER_TRADITIONAL = 1;
    private static final int FILTER_MULTI = 2;
    private static final int FILTER_MYSTERY = 3;


    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferenceEditor;
    private static final int PREFERENCE_MODE_PRIVATE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        viewDistance = (TextView) findViewById(R.id.textViewMapDistance);
        viewDistance.setVisibility(View.INVISIBLE);
        preferenceSettings = getPreferences(PREFERENCE_MODE_PRIVATE);
        isGeofencingEnabled = preferenceSettings.getBoolean("isGeofencingEnabled", true);
        isShowWptsEnabled = preferenceSettings.getBoolean("isShowWptsEnabled", true);
        filterFind = preferenceSettings.getInt("filterFind", FILTER_ALL);
        filterType = preferenceSettings.getInt("filterType", FILTER_ALL);
        //System.out.println("FilterFind: " + filterFind);
        //System.out.println("FilterType: " + filterType);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Unregister for location callbacks:
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_CACHE_ACTIVITY && resultCode == RESULT_OK && data != null) {
            try {
                findSelectedCache(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 10) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("RESULT FROM DETAIL = RESULT_OK");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data.getIntExtra("id", 0) > 0)
                    findSelectedCache(data);
                else
                    findCaches(filterFind, filterType);
                System.out.println("RESULT FROM DETAIL = RESULT_CANCELED");
            }
        } else if (requestCode == SELECT_CACHE_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("RESULT FROM SELECT = RESULT_OK");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("RESULT FROM SELECT = RESULT_CANCELED");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_cache:
                try {
                    View menuItemView = findViewById(R.id.action_select_cache); // SAME ID AS MENU ID
                    showMenuFindOptions(menuItemView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_map_type:
                showMapTypeSelectorDialog();
                return true;
            case R.id.action_geofence:
                try {
                    View menuItemView = findViewById(R.id.action_geofence); // SAME ID AS MENU ID
                    showMenuGeofenceOptions(menuItemView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_clear:
                showClearMapDialog();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return true;
    }

    public void showMenuFindOptions(View v) {
        try {
            Log.i(TAG, "ShowPopUpMenuFind");
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                popup = new PopupMenu(actionBar.getThemedContext(), v);
                popup.setOnMenuItemClickListener(this);
                popup.inflate(R.menu.menu_map_find);
                MenuItem item = popup.getMenu().findItem(R.id.action_show_waypoints);
                if (isShowWptsEnabled) {
                    item.setTitle("Vypnout Waypoints");
                } else {
                    item.setTitle("Zapnout Waypoints");
                }
                popup.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showMenuGeofenceOptions(View v) {
        try {
            Log.i(TAG, "ShowPopUpMenuFind");
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                popup = new PopupMenu(actionBar.getThemedContext(), v);
                popup.setOnMenuItemClickListener(this);
                popup.inflate(R.menu.menu_map_geofence);
                MenuItem item = popup.getMenu().findItem(R.id.action_enable_geofence);
                if (isGeofencingEnabled) {
                    item.setTitle("Vypnout Geofencing");
                } else {
                    item.setTitle("Zapnout Geofencing");
                }
                popup.show();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_find_one_cache:
                Log.i(TAG, "MenuClick_find_one_cache_action");
                Intent serverSetIntent = new Intent(this, SelectCachesActivity.class);
                startActivityForResult(serverSetIntent, SELECT_CACHE_ACTIVITY);
                return true;
            case R.id.action_find_address:
                Log.i(TAG, "MenuClick_filter_find_caches");
                try {
                    showAddressDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_filter_find_caches:
                Log.i(TAG, "MenuClick_filter_find_caches");
                try {
                    showFilterFindCacheDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_filter_caches:
                Log.i(TAG, "MenuClick_filter_caches");
                try {
                    showFilterTypeCacheDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_show_waypoints:
                Log.i(TAG, "MenuClick_show_on_off_wpts");

                isShowWptsEnabled = !isShowWptsEnabled;
                preferenceEditor = preferenceSettings.edit();
                preferenceEditor.putBoolean("isShowWptsEnabled", isShowWptsEnabled);
                preferenceEditor.apply();
                findCaches(filterFind, filterType);
                return true;
            case R.id.action_planning_route:
                Log.i(TAG, "MenuClick_track_route");
                isShortestWayEnabled = true;
                routePoints = new ArrayList<>();
                return true;
            case R.id.action_enable_geofence:
                Log.i(TAG, "MenuClick_enable_geofence_radius");
                isGeofencingEnabled = !isGeofencingEnabled;
                if (!isGeofencingEnabled) {
                    item.setTitle("Enable");
                    stopGeofence();
                    Toast.makeText(getApplicationContext(), "Geofencing is disable!", Toast.LENGTH_SHORT).show();
                } else {
                    item.setTitle("Disable");
                    Toast.makeText(getApplicationContext(), "Geofencing is enable!", Toast.LENGTH_SHORT).show();
                }
                preferenceEditor = preferenceSettings.edit();
                preferenceEditor.putBoolean("isGeofencingEnabled", isGeofencingEnabled);
                preferenceEditor.apply();
                return true;
            case R.id.action_geofence_radius:
                Log.i(TAG, "MenuClick_geofence_radius_action");
                showGeofenceRadiusSelectorDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            Log.i(TAG, "onMapReady");
            this.googleMap = googleMap;
            if (this.googleMap == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
            setMapMoveListener();
            isEnableGPS();
            initializeMapLocationSettings();
            initializeUiSettings();
            initializeMapTraffic();
            initializeMapType();
            initializeMapViewSettings();

            Intent intent = getIntent();
            int id = intent.getIntExtra("id", 0);
            if (id > 0) {
                findSelectedCache(intent);
            } else {
                findCaches(filterFind, filterType);
            }
        } catch (Exception e) {
            Log.e(TAG, "onMapReady problem!");
            e.printStackTrace();
        }
    }

    public void setMapMoveListener() {
        Log.i(TAG, "setListeners");
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnMyLocationButtonClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    public void initializeUiSettings() {
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    public void initializeMapLocationSettings() {
        Log.i(TAG, "initializeMapLocationSettings");
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
        }
    }

    public void initializeMapTraffic() {
        Log.i(TAG, "initializeMapTraffic");
        googleMap.setTrafficEnabled(false);
    }

    public void initializeMapType() {
        Log.i(TAG, "initializeMapType");
        int mapType = preferenceSettings.getInt("googleMapType", GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMapType(mapType);
    }

    public void initializeMapViewSettings() {
        Log.i(TAG, "initializeMapViewSettings");
        googleMap.setIndoorEnabled(false);
        googleMap.setBuildingsEnabled(false);
    }

    protected synchronized void buildGoogleApiClient() {
        if (checkPlayServices()) {
            Log.i(TAG, "Build Google API Client.");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        } else {
            Log.e(TAG, "Your Device doesn't support Google Play Services.");
        }

    }

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean checkPlayServices() {
        Log.i(TAG, "Check Google Play Services.");
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected_LocationRequest");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //move map camera
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate loc;
        if (previousZoomLevel < 0 && !isMoving) {
            previousZoomLevel = ZOOM_BASE_VALUE;
            loc = CameraUpdateFactory.newLatLngZoom(latLng, previousZoomLevel);
            googleMap.animateCamera(loc);
        } else {
            if (!isMoving) {
                loc = CameraUpdateFactory.newLatLngZoom(latLng, previousZoomLevel);
                googleMap.animateCamera(loc);
            } else {
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(previousZoomLevel));
            }
        }

        if (isMarkerFollow) {
            LatLng myLocLatLon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if (followCache != null) {
                LatLng destinationCoord = new LatLng(followCache.getLat(), followCache.getLon());
                viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");
            }
            if (followWpt != null) {
                LatLng destinationCoord = new LatLng(followWpt.getLat(), followWpt.getLon());
                viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");
            }


        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        Log.i(TAG, "checkLocationPermission");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_MAPS,
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_MAPS,
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (mGoogleApiClient == null) {
                                buildGoogleApiClient();
                            }
                            googleMap.setMyLocationEnabled(true);
                        }

                    }

                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showGeofenceRadiusSelectorDialog() {
        Log.i(TAG, "ShowGeofenceRadiusSelectorDialog");
        final String fDialogTitle = "Velikost radiusu pro notifikace: ";
        checkItemRadius = preferenceSettings.getInt("checkItemRadius", 2);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);
        builder.setTitle(fDialogTitle);
        builder.setSingleChoiceItems(
                GEOFENCE_RADIUS_ITEMS,
                checkItemRadius,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                GEOFENCE_RADIUS = 50f;
                                checkItemRadius = 0;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putFloat("geofence_radius", GEOFENCE_RADIUS);
                                preferenceEditor.putInt("checkItemRadius", checkItemRadius);
                                preferenceEditor.apply();
                                break;
                            case 1:
                                GEOFENCE_RADIUS = 100f;
                                checkItemRadius = 1;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putFloat("geofence_radius", GEOFENCE_RADIUS);
                                preferenceEditor.putInt("checkItemRadius", checkItemRadius);
                                preferenceEditor.apply();
                                break;
                            case 2:
                                GEOFENCE_RADIUS = 150f;
                                checkItemRadius = 2;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putFloat("geofence_radius", GEOFENCE_RADIUS);
                                preferenceEditor.putInt("checkItemRadius", checkItemRadius);
                                preferenceEditor.apply();
                                break;
                            case 3:
                                GEOFENCE_RADIUS = 300f;
                                checkItemRadius = 3;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putFloat("geofence_radius", GEOFENCE_RADIUS);
                                preferenceEditor.putInt("checkItemRadius", checkItemRadius);
                                preferenceEditor.apply();
                                break;
                            default:
                                checkItemRadius = 1;
                                GEOFENCE_RADIUS = 100f;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putFloat("geofence_radius", GEOFENCE_RADIUS);
                                preferenceEditor.putInt("checkItemRadius", checkItemRadius);
                                preferenceEditor.apply();
                        }
                        if (marker != null && isGeofencingEnabled) {
                            stopGeofence();
                            startGeofence();
                            drawGeofence();
                        }
                        dialog.dismiss();
                    }
                }
        ).show();
    }

    private void showAddressDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);
        alert.setTitle("Adresa");
        alert.setMessage("Zadejte adresu vyhledávání:");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert.setIcon(getResources().getDrawable(R.drawable.ico_find_address, getTheme()));
        } else {
            alert.setIcon(getResources().getDrawable(R.drawable.ico_find_address));
        }
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        //input.setPadding(50, 50, 50, 50);
        input.setSingleLine();
        input.setTextColor(getResources().getColor(R.color.colorWhite));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        input.setLayoutParams(params);
        container.addView(input);
        alert.setView(container);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String location = input.getText().toString();
                Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geoCoder.getFromLocationName(location, 5);
                    if (addresses.size() > 0) {
                        Double lat = (double) (addresses.get(0).getLatitude());
                        Double lon = (double) (addresses.get(0).getLongitude());

                        StringBuilder builder = new StringBuilder();
                        int maxLines = addresses.get(0).getMaxAddressLineIndex();
                        for (int i = 0; i < maxLines; i++) {
                            String addressStr = addresses.get(0).getAddressLine(i);
                            builder.append(addressStr);
                            builder.append(" ");
                        }

                        String finalAddress = builder.toString();

                        final LatLng user = new LatLng(lat, lon);
                        marker = googleMap.addMarker(new MarkerOptions()
                                .position(user)
                                .title(finalAddress)
                                .icon(BitmapDescriptorFactory.defaultMarker()));
                        marker.setTag(finalAddress);
                        isMarkerFollow = true;
                        CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(user, previousZoomLevel);
                        googleMap.animateCamera(loc);
                        isMoving = true;
                        viewDistance.setVisibility(View.VISIBLE);
                        LatLng myLocLatLon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        LatLng destinationCoord = user;
                        viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.setNeutralButton("Smazat mapu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                googleMap.clear();
            }
        });
        alert.show();
    }

    private void showFilterTypeCacheDialog() {
        Log.i(TAG, "showFilterTypeCacheDialog");
        final String fDialogTitle = "Jaký typ keše chcete zobrazit: ";
        filterType = preferenceSettings.getInt("filterType", FILTER_ALL);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);
        builder.setTitle(fDialogTitle);
        builder.setSingleChoiceItems(
                CACHES_TYPE_ITEMS,
                filterType,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        try {
                            switch (item) {
                                case 0:
                                    filterType = FILTER_ALL;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterType", filterType);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                                case 1:
                                    filterType = FILTER_TRADITIONAL;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterType", filterType);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                                case 2:
                                    filterType = FILTER_MULTI;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterType", filterType);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                                case 3:
                                    filterType = FILTER_MYSTERY;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterType", filterType);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                                default:
                                    filterType = FILTER_ALL;
                                    preferenceEditor.putInt("filterType", filterType);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                            }
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).show();
    }

    private void showFilterFindCacheDialog() {
        Log.i(TAG, "showFilterFindCacheDialog");
        final String fDialogTitle = "Jakým způsobem chcete keše vyhledávat: ";
        filterFind = preferenceSettings.getInt("filterFind", FILTER_ALL);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);
        builder.setTitle(fDialogTitle);
        builder.setSingleChoiceItems(
                FILTER_FIND_ITEMS,
                filterFind,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        try {
                            switch (item) {
                                case FILTER_ALL:
                                    Log.i(TAG, "MenuClick_find_all_caches_action");
                                    filterFind = FILTER_ALL;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterFind", FILTER_ALL);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                                case FILTER_FOUND:
                                    Log.i(TAG, "MenuClick_found_caches_filter");
                                    filterFind = FILTER_FOUND;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterFind", FILTER_FOUND);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                                case FILTER_NOT_FOUND:
                                    Log.i(TAG, "MenuClick_not_found_caches_filter");
                                    filterFind = FILTER_NOT_FOUND;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterFind", FILTER_NOT_FOUND);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                                default:
                                    Log.i(TAG, "MenuClick_find_all_caches_action");
                                    filterFind = FILTER_ALL;
                                    preferenceEditor = preferenceSettings.edit();
                                    preferenceEditor.putInt("filterFind", FILTER_ALL);
                                    preferenceEditor.apply();
                                    findCaches(filterFind, filterType);
                                    break;
                            }
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).show();
    }

    private void showMapTypeSelectorDialog() {
        Log.i(TAG, "ShowMapTypeSelectorDialog");
        final String fDialogTitle = "Typ zobrazení mapy: ";
        if (checkItemMapType == 0) checkItemMapType = googleMap.getMapType() - 1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);
        builder.setTitle(fDialogTitle);
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItemMapType,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 1:
                                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                checkItemMapType = 1;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putInt("googleMapType", GoogleMap.MAP_TYPE_SATELLITE);
                                preferenceEditor.apply();
                                break;
                            case 2:
                                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                checkItemMapType = 2;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putInt("googleMapType", GoogleMap.MAP_TYPE_TERRAIN);
                                preferenceEditor.apply();
                                break;
                            case 3:
                                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                checkItemMapType = 3;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putInt("googleMapType", GoogleMap.MAP_TYPE_HYBRID);
                                preferenceEditor.apply();
                                break;
                            default:
                                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                checkItemMapType = 0;
                                preferenceEditor = preferenceSettings.edit();
                                preferenceEditor.putInt("googleMapType", GoogleMap.MAP_TYPE_NORMAL);
                                preferenceEditor.apply();
                        }
                        dialog.dismiss();
                    }
                }
        ).show();
    }

    private void showClearMapDialog() {
        Log.i(TAG, "showClearMapDialog");
        new android.app.AlertDialog.Builder(this, R.style.YourAlertDialogTheme)
                .setTitle("Smazat?")
                .setMessage("Opravdu si přejete vyčistit mapu?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        googleMap.clear();
                        viewDistance.setVisibility(View.INVISIBLE);
                        isMarkerFollow = false;
                        Toast.makeText(getApplicationContext(), "CLEAR!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void isEnableGPS() {
        Log.i(TAG, "isEnableGPS_check");
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new android.app.AlertDialog.Builder(this, R.style.YourAlertDialogTheme)
                    .setTitle("GPS je vypnuta")
                    .setMessage("Zapněte prosím modul GPS pro přesnější zjištění vaší polohy!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void findSelectedCache(Intent data) {
        Log.i(TAG, "findSelectedCache");
        int id = data.getIntExtra("id", 0);
        Context ctx = getApplicationContext();
        Caches_DB caches_db = new Caches_DB(ctx);
        Cache cache = caches_db.getCache((id));
        ArrayList<Waypoint> wpts = caches_db.getWpts(cache.getId());
        caches_db.close();
        if (markers != null)
            markers.clear();
        else
            markers = new ArrayList<>();
        googleMap.clear();
        LatLng destinationCoordCache = new LatLng(cache.getLat(), cache.getLon());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(destinationCoordCache);
        markerOptions.title(cache.getName());
        markerOptions.zIndex(0.51f);
        markerOptions.snippet("Click for more info!");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_select_orange));
        viewDistance.setVisibility(View.VISIBLE);
        marker = googleMap.addMarker(markerOptions);
        marker.setTag(cache);

        for (int i = 0; i < wpts.size(); i++) {
            LatLng destCoord = new LatLng(wpts.get(i).getLat(), wpts.get(i).getLon());
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.position(destCoord);
            mOptions.title(wpts.get(i).getDesc());
            mOptions.zIndex(0.5f);
            mOptions.snippet("Click for more info!");
            if (wpts.get(i).getSym().contains("Parking")) {
                mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_parking_blue));
            } else {
                if (wpts.get(i).getId_list() == LIST_NALEZENE) {
                    mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wpt_green));
                } else {
                    mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wpt_grey));
                }

            }
            marker = googleMap.addMarker(mOptions);
            marker.setTag(wpts.get(i));
            markers.add(marker);
        }

        followCache = cache;
        isMarkerFollow = true;
        CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(destinationCoordCache, previousZoomLevel);
        googleMap.animateCamera(loc);
        planningShortestRoute(markers);
        try {
            if (isGeofencingEnabled) {
                if (mGoogleApiClient.isConnected())
                    startGeofence();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findCaches(int filterFind, int filterType) {
        Log.i(TAG, "findCachesFilter");
        Context ctx = getApplicationContext();
        Caches_DB caches_db = new Caches_DB(ctx);
        ArrayList<Cache> caches = caches_db.getCachesByFilter(filterFind, filterType);
        ArrayList<Waypoint> wpts = caches_db.getWptsFilter(filterFind);
        caches_db.close();
        googleMap.clear();
        viewDistance.setVisibility(View.INVISIBLE);
        isMarkerFollow = false;
        if (markers == null) {
            markers = new ArrayList<>();
        } else {
            markers.clear();
        }

        for (int i = 0; i < caches.size(); i++) {
            LatLng destinationCoord = new LatLng(caches.get(i).getLat(), caches.get(i).getLon());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(destinationCoord);
            markerOptions.zIndex(1.0f);
            markerOptions.title(caches.get(i).getName());
            markerOptions.snippet("Click for more info!");
            if (caches.get(i).getId_list() == LIST_NALEZENE) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_success_green));
            } else {
                if (caches.get(i).getType().contains("Traditional")) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_traditional));
                } else if (caches.get(i).getType().contains("Multi")) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_multi));
                } else if (caches.get(i).getType().contains("Unknown")) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_mystery));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_default));
                }
            }
            marker = googleMap.addMarker(markerOptions);
            marker.setTag(caches.get(i));
            markers.add(marker);
        }
        if (isShowWptsEnabled) {
            if (filterFind == FILTER_ALL) {
                for (int i = 0; i < wpts.size(); i++) {
                    LatLng destinationCoord = new LatLng(wpts.get(i).getLat(), wpts.get(i).getLon());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(destinationCoord);
                    markerOptions.zIndex(0.5f);
                    markerOptions.title(wpts.get(i).getDesc());
                    markerOptions.snippet("Click for more info!");
                    if (wpts.get(i).getSym().contains("Parking")) {
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_parking_blue));
                    } else {
                        if (wpts.get(i).getId_list() == LIST_NALEZENE) {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wpt_green));
                        } else {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wpt_grey));
                        }

                    }
                    marker = googleMap.addMarker(markerOptions);
                    marker.setTag(wpts.get(i));
                    markers.add(marker);
                }
            } else {

            }
        }
    }

    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = googleMap.getCameraPosition();
        if (previousZoomLevel != cameraPosition.zoom) {
            isZooming = true;
            previousZoomLevel = cameraPosition.zoom;
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            if (previousZoomLevel != cameraPosition.zoom) {
                isZooming = true;
                previousZoomLevel = cameraPosition.zoom;
            }
            isMoving = true;
            //Toast.makeText(this, "The user gestured on the map. = MOVE", Toast.LENGTH_SHORT).show();
        }
        /*
        else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {
            //Toast.makeText(this, "The user tapped something on the map. = CLICK BUTTON",Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
            //Toast.makeText(this, "The app moved the camera.", Toast.LENGTH_SHORT).show();
        }
        */
    }

    @Override
    public boolean onMyLocationButtonClick() {
        isMoving = false;
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        try {
            if (isShortestWayEnabled) {
                AlertDialog dialog = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme)
                        .setTitle("Trasa")
                        .setIcon(R.drawable.ico_geo)
                        .setMessage("Pocet bodu trasy:       " + routePoints.size())
                        .setNegativeButton("Zavřít", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getApplicationContext(), "Dialog close!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton("Route", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                planningShortestRoute(routePoints);

                            }
                        }).setNeutralButton("Zrusit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isShortestWayEnabled = false;
                                routePoints.clear();
                                recolorMarkers();
                                viewDistance.setVisibility(View.INVISIBLE);
                            }
                        }).show();
            } else {

                if (marker.getTag() instanceof Cache) {
                    final Cache cache = (Cache) marker.getTag();
                    System.out.println("MARKER CACHE: " + cache.getName());
                    LatLng myLocLatLon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    LatLng destinationCoord = new LatLng(cache.getLat(), cache.getLon());
                    //viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");

                    String typeCache = cache.getType();
                    String sizeCache = cache.getSize();
                    String helpCache = cache.getHelp();
                    String diffCache;
                    String terrCache;
                    if (typeCache == null) typeCache = "neuvedeno";
                    if (sizeCache == null) sizeCache = "neuvedeno";
                    if (helpCache == null) helpCache = "neuvedeno";
                    if (cache.getDifficulty() == 0.0) diffCache = "neuvedeno";
                    else diffCache = String.valueOf(cache.getDifficulty());
                    if (cache.getTerrain() == 0.0) terrCache = "neuvedeno";
                    else terrCache = String.valueOf(cache.getTerrain());

                    AlertDialog dialog = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme)
                            .setTitle(cache.getName())
                            .setIcon(R.drawable.ico_geo)
                            .setMessage("Type:       " + typeCache + "\n"
                                    + "Size:       " + sizeCache + "\n"
                                    + "Terrain:    " + terrCache + "\n"
                                    + "Difficulty: " + diffCache + "\n"
                                    + "Distance:   " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km\n"
                                    + "Help:       " + helpCache
                            )
                            .setPositiveButton("Zavřít", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Toast.makeText(getApplicationContext(), "Hodne stesti!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Detail", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Toast.makeText(getApplicationContext(), "Click Detail!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), SelectDetailCacheActivity.class);
                                    intent.putExtra("id", cache.getId());
                                    startActivityForResult(intent, 10);
                                }
                            }).setNeutralButton("Navigovat", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        SensorManager mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
                                        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null && mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                                            Intent intent = new Intent(getApplicationContext(), PointToPointActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("lat", cache.getLat());
                                            intent.putExtra("long", cache.getLon());
                                            getApplicationContext().startActivity(intent);
                                        } else {
                                            Log.e(TAG, "Device doesn't support sensors for this feature!");
                                            Toast.makeText(getApplicationContext(), "Device doesn't support sensors for this feature!", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).show();

                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    if (textView != null) textView.setTypeface(Typeface.MONOSPACE);
                } else if (marker.getTag() instanceof Waypoint) {
                    final Waypoint wpt = (Waypoint) marker.getTag();
                    LatLng myLocLatLon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    LatLng destinationCoord = new LatLng(wpt.getLat(), wpt.getLon());
                    viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");

                    String cmtWpt = wpt.getCmt();
                    String nameWpt = wpt.getName();
                    String symWpt = wpt.getSym();

                    if (cmtWpt == null) cmtWpt = "neuvedeno";
                    if (nameWpt == null) nameWpt = "neuvedeno";
                    if (symWpt == null) symWpt = "neuvedeno";

                    Drawable myDrawable;
                    if (wpt.getType().contains("Parking")) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            myDrawable = getResources().getDrawable(R.drawable.ico_parking, getTheme());
                        } else {
                            myDrawable = getResources().getDrawable(R.drawable.ico_parking);
                        }
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            myDrawable = getResources().getDrawable(R.drawable.ico_wpt, getTheme());
                        } else {
                            myDrawable = getResources().getDrawable(R.drawable.ico_wpt);
                        }
                    }
                    String cacheName;
                    if (wpt.getId_cache() > -1) {
                        Caches_DB cache_db = new Caches_DB(getApplicationContext());
                        Cache cache = cache_db.getCache(wpt.getId_cache());
                        cacheName = cache.getName();
                        cache_db.close();
                    } else {
                        cacheName = "nesparovano";
                    }


                    AlertDialog dialog = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme)
                            .setTitle(wpt.getDesc())
                            .setIcon(myDrawable)
                            .setMessage("Type: " + "Waypoint" + "\n"
                                    + "Sym:  " + symWpt + "\n"
                                    + "Code: " + nameWpt + "\n"
                                    + "Cmt:  " + cmtWpt + "\n"
                                    + "Distance:   " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km\n"
                                    + "Cache: " + cacheName
                            )
                            .setPositiveButton("Zavřít", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Toast.makeText(getApplicationContext(), "Hodne stesti!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Detail", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Toast.makeText(getApplicationContext(), "Click Detail!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), DetailWptActivity.class);
                                    intent.putExtra("id", wpt.getId());
                                    startActivityForResult(intent, 10);
                                }
                            }).setNeutralButton("Navigovat", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        SensorManager mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
                                        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null && mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                                            Intent intent = new Intent(getApplicationContext(), PointToPointActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("lat", wpt.getLat());
                                            intent.putExtra("long", wpt.getLon());
                                            getApplicationContext().startActivity(intent);
                                        } else {
                                            Log.e(TAG, "Device doesn't support sensors for this feature!");
                                            Toast.makeText(getApplicationContext(), "Device doesn't support sensors for this feature!", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).show();

                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    if (textView != null) textView.setTypeface(Typeface.MONOSPACE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void planningShortestRoute(ArrayList<Marker> routePoints){
        //TODO get distances matrix
        int matrixSize = routePoints.size();
        double[][] matrix = new double[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrix[i][j] = Utils.CalculationByDistance(routePoints.get(i).getPosition(), routePoints.get(j).getPosition());
            }
        }

        //TODO display matrix and routePoints
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                System.out.print("\t" + matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("///////////////////////////////");
        for (int i = 0; i < routePoints.size(); i++) {
            System.out.println(routePoints.get(i).getTitle());
        }
        System.out.println("///////////////////////////////");


        //TODO PLANNING ROUTE
        ArrayList<Marker> shortestRoutePoints = new ArrayList<>();
        double totalDistance = 0;
        try {
            shortestRoutePoints.add(routePoints.get(0));
            pomIndex = 0;
            while (shortestRoutePoints.size() != routePoints.size()) {
                double min = 0;
                int pomJ;
                int i = pomIndex;
                for (int j = 0; j < matrixSize; j++) {
                    pomJ = 0;
                    while(min == 0){
                        if(!shortestRoutePoints.contains(routePoints.get(pomJ))) {
                            min = matrix[i][pomJ];
                            pomIndex = pomJ;
                        }
                        if(min==0)
                            pomJ += 1;
                    }
                    if (matrix[i][j] <= min && matrix[pomIndex][j] != 0) {
                        if(!shortestRoutePoints.contains(routePoints.get(j))){
                            min = matrix[i][j];
                            pomIndex = j;
                        }
                    }
                }
                totalDistance += min;
                shortestRoutePoints.add(routePoints.get(pomIndex));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewDistance.setText("Celkova vzdalenost: " + totalDistance);

        //TODO display shortest Route Points
        for (int i = 0; i < shortestRoutePoints.size(); i++) {
            System.out.println(shortestRoutePoints.get(i).getTitle());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i(TAG, "onMarkerClick");
        try {
            isMoving = true;
            isMarkerFollow = true;

            if (isShortestWayEnabled) {
                Log.i(TAG, "isShortestWayEnabled");
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_select_orange));
                marker.setSnippet("Click for ROUTE options!");
                if (routePoints.contains(marker)) {
                    Log.i(TAG, "routePoints remove");
                    routePoints.remove(marker);
                    recolorMarker(marker);
                    viewDistance.setVisibility(View.VISIBLE);
                    String desc = "Seznam bodu trasy: \n";
                    for (int i = 0; i < routePoints.size(); i++) {
                        desc = desc + routePoints.get(i).getTitle() + " \n";
                    }
                    viewDistance.setText(desc);
                } else {
                    Log.i(TAG, "routePoints add");
                    routePoints.add(marker);
                    viewDistance.setVisibility(View.VISIBLE);
                    String desc = "Seznam bodu trasy: \n";
                    for (int i = 0; i < routePoints.size(); i++) {
                        desc = desc + routePoints.get(i).getTitle() + " \n";
                    }
                    viewDistance.setText(desc);
                }


            } else {
                Log.i(TAG, "shortestWayDisabled");
                this.marker = marker;
                if (markers != null) recolorMarkers();
                viewDistance.setVisibility(View.VISIBLE);

                if (marker.getTag() instanceof Cache) {
                    this.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_select_orange));
                    followCache = (Cache) marker.getTag();
                    followWpt = null;
                    LatLng myLocLatLon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    LatLng destinationCoord = new LatLng(followCache.getLat(), followCache.getLon());
                    viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");
                } else if (marker.getTag() instanceof Waypoint) {
                    followWpt = (Waypoint) marker.getTag();
                    followCache = null;
                    if (followWpt.getSym().contains("Parking")) {
                        this.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_parking_blue));
                    } else {
                        this.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wpt_orange));
                    }
                    LatLng myLocLatLon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    LatLng destinationCoord = new LatLng(followWpt.getLat(), followWpt.getLon());
                    viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");
                } else {
                    LatLng myLocLatLon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    LatLng destinationCoord = marker.getPosition();
                    viewDistance.setText("Vzdálenost: " + Utils.CalculationByDistance(myLocLatLon, destinationCoord) + " km");
                }

                if (isGeofencingEnabled) {
                    startGeofence();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void recolorMarker(Marker marker) {
        if (marker.getTag() instanceof Cache) {
            Cache cache = (Cache) marker.getTag();
            if (cache.getId_list() == LIST_NALEZENE) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_success_green));
            } else {
                if (cache.getType().contains("Traditional")) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_traditional));
                } else if (cache.getType().contains("Multi")) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_multi));
                } else if (cache.getType().contains("Unknown")) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_mystery));
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cache_default));
                }
            }
        } else {
            Waypoint wpt = (Waypoint) marker.getTag();
            if (wpt.getSym().contains("Parking")) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_parking_blue));
            } else {
                if (wpt.getId_list() == LIST_NALEZENE) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wpt_green));
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wpt_grey));
                }

            }
        }
    }

    private void recolorMarkers() {
        for (int i = 0; i < markers.size(); i++) {
            Marker marker = markers.get(i);
            recolorMarker(marker);
        }
    }

    private Geofence createGeofence(LatLng latLng, float radius, Marker marker) {
        Log.d(TAG, "createGeofence");
        if (marker.getTag() instanceof Cache) {
            Cache cache = (Cache) marker.getTag();
            return new Geofence.Builder()
                    .setRequestId("cache " + cache.getName())
                    .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                    .setExpirationDuration(GEO_DURATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                            | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        } else if (marker.getTag() instanceof Waypoint) {
            Waypoint wpt = (Waypoint) marker.getTag();
            return new Geofence.Builder()
                    .setRequestId("Waypoint " + wpt.getName())
                    .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                    .setExpirationDuration(GEO_DURATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                            | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        } else {
            return new Geofence.Builder()
                    .setRequestId("Marker address")
                    .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                    .setExpirationDuration(GEO_DURATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                            | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        }
    }

    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkLocationPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            Log.i(TAG, "onResult: Status.isSuccess");
            if (isGeofencingEnabled) {
                drawGeofence();
            } else {
                if (geoFenceLimits != null)
                    geoFenceLimits.remove();
            }
        } else {
            Log.i(TAG, "onResult: FAIL");
        }
    }

    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(marker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = googleMap.addCircle(circleOptions);
    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (marker != null) {
            Geofence geofence = createGeofence(marker.getPosition(), GEOFENCE_RADIUS, marker);
            Log.i(TAG, "startGeofence().createGeofence");
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            Log.i(TAG, "startGeofence().createGeofenceRequest");
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    private void stopGeofence() {
        Log.i(TAG, "stopGeofence()");
        if (marker != null) {
            Log.i(TAG, "stopGeofenceIF");
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } else {
            Log.e(TAG, "Geofence didn't stopped!");
        }
    }

}
