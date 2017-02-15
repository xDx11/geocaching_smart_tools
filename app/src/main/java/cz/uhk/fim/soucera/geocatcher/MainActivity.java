package cz.uhk.fim.soucera.geocatcher;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;

import cz.uhk.fim.soucera.geocatcher.caches.MainCachesActivity;
import cz.uhk.fim.soucera.geocatcher.features.CompassActivity;
import cz.uhk.fim.soucera.geocatcher.features.FlashActivity;
import cz.uhk.fim.soucera.geocatcher.imports.GPXparser;
import cz.uhk.fim.soucera.geocatcher.imports.ImportLoadingAsync;
import cz.uhk.fim.soucera.geocatcher.imports.ImportObject;
import cz.uhk.fim.soucera.geocatcher.imports.LOCparser;
import cz.uhk.fim.soucera.geocatcher.waypoints.Waypoint;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnImportLOC;
    private ImageButton btnImportGPX;
    private ImageButton btnCaches;
    private ImageButton btnMap;
    private ImageButton btnFlash;
    private ImageButton btnCompass;
    private ArrayList<Cache> caches;
    private ArrayList<Waypoint> waypoints;
    private ArrayList<ArrayList<Waypoint>> groupsOfWaypoints;
    private String Fpath;
    private String fileName;

    private static String TAG = MainActivity.class.getName();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_LOC = 100;
    private static final int REQUEST_CODE_GPX = 200;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_version2);
        Log.i(TAG, "onCreate");

        btnImportLOC = (ImageButton) findViewById(R.id.btn_importLOC);
        btnImportGPX = (ImageButton) findViewById(R.id.btn_importGPX);
        btnCaches = (ImageButton) findViewById(R.id.btn_caches);
        btnMap = (ImageButton) findViewById(R.id.btn_map);
        btnFlash = (ImageButton) findViewById(R.id.btn_flashlight);
        btnCompass = (ImageButton) findViewById(R.id.btn_compass);

        btnImportLOC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openFile("*/*", REQUEST_CODE_LOC);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Something wrong with data!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        });

        btnImportGPX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openFile("*/*", REQUEST_CODE_GPX);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Something wrong with data!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        btnCaches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getApplicationContext(), MainCachesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getApplicationContext(), FlashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SensorManager mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
                    if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null && mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                        Intent intent = new Intent(getApplicationContext(), CompassActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    } else {
                        Log.e(TAG, "Device doesn't support sensors for this feature!");
                        Toast.makeText(getApplicationContext(), "Device doesn't support sensors for this feature!", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void openFile(String minmeType, int request_code) {
        Log.i(TAG, "openFile");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            switch (request_code) {
                case REQUEST_CODE_LOC:
                    Log.i(TAG, "openFile_chooserIntent_LOC");
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkPermission()) {
                            startActivityForResult(chooserIntent, REQUEST_CODE_LOC);
                        } else {
                            requestPermission(); // Code for permission
                            startActivityForResult(chooserIntent, REQUEST_CODE_LOC);
                        }
                    } else {
                        startActivityForResult(chooserIntent, REQUEST_CODE_LOC);
                    }

                    break;
                case REQUEST_CODE_GPX:
                    Log.i(TAG, "openFile_chooserIntent_GPX");
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkPermission()) {
                            startActivityForResult(chooserIntent, REQUEST_CODE_GPX);
                        } else {
                            requestPermission(); // Code for permission
                            startActivityForResult(chooserIntent, REQUEST_CODE_GPX);
                        }
                    } else {
                        startActivityForResult(chooserIntent, REQUEST_CODE_GPX);
                    }
                    break;
            }

        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "No suitable File manager was found.");
            Toast.makeText(getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.i(TAG, "Activity result - start import");
            Fpath = "";
            String testPath = data.getDataString();
            fileName = data.getData().getLastPathSegment();
            if (testPath.contains("content://")) {
                try {
                    Fpath = GPXparser.getFilePath(getApplicationContext(), data.getData());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                Fpath = data.getData().getPath();
            }

            /*
            System.out.println("ACTIVITY_RESULT_BEGIN_FPATH: " + Fpath);
            System.out.println("getDataString:" + data.getDataString());
            System.out.println("getData:" + data.getData());
            System.out.println("getData.getPath:" + data.getData().getPath());
            System.out.println("getData.getLastPathSegment:" + data.getData().getLastPathSegment());
            System.out.println("getData.getEncodedPath:" + data.getData().getEncodedPath());
            System.out.println("Environment.toString:" + Environment.getExternalStorageDirectory().toString());
            System.out.println("Environment.getPath:" + Environment.getExternalStorageDirectory().getPath());
            System.out.println("Environment.getAbsoluthPath:" + Environment.getExternalStorageDirectory().getAbsolutePath());
            System.out.println("Environment.PARENT.AbsoluthFile:" + Environment.getExternalStorageDirectory().getParentFile().getAbsolutePath());
            //Fpath = Fpath.replace(Environment.getExternalStorageDirectory().toString(),"");
            //System.out.println(Environment.getExternalStorageDirectory().toString()+name);
            System.out.println(Environment.getExternalStorageState());
            //verifyStoragePermissions(this);
            */

            Context ctx = getApplicationContext();
            Caches_DB caches_db = new Caches_DB(ctx);
            try {
                switch (requestCode) {
                    case REQUEST_CODE_LOC:
                        Log.i(TAG, "ActivityResult - IMPORT LOC");
                        caches = LOCparser.getCachceFromFile(Fpath, fileName);
                        if (caches != null) {
                            for (int i = 0; i < caches.size(); i++) {
                                caches_db.insertCache(caches.get(i));
                            }
                        }
                        break;
                    case REQUEST_CODE_GPX:
                        Log.i(TAG, "ActivityResult - IMPORT GPX");
                        LoadingImportGPX loadingImport = new LoadingImportGPX();
                        loadingImport.execute();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            caches_db.close();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private class LoadingImportGPX extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Probíhá import keší, počkejte prosím.");
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(final String... args) {
            try {
                ImportObject importObject = GPXparser.getCacheFromFile(Fpath, fileName);
                caches = importObject.getCaches();
                waypoints = importObject.getWaypoints();
                groupsOfWaypoints = importObject.getGroupsOfWaypoints();
                Caches_DB caches_db = new Caches_DB(getApplicationContext());
                if (caches != null) {
                    for (int i = 0; i < caches.size(); i++) {
                        if (!caches_db.isCacheRecordFound(caches.get(i).getCode())) {
                            long id_cache = caches_db.insertCache(caches.get(i));
                            if (caches.get(i).getLogs().size() > 0) {
                                caches_db.insertLogs(caches.get(i).getLogs(), id_cache);
                            }
                            if (groupsOfWaypoints != null) {
                                if (groupsOfWaypoints.size() > 0)
                                    caches_db.insertWpts(groupsOfWaypoints.get(i), id_cache);
                            }
                            if (caches.size() == 1 && waypoints != null) {
                                caches_db.insertWpts(waypoints, id_cache);
                            }
                        } else {
                            Log.i(TAG, "Cache found in DB, not again insert!");
                        }
                    }
                }
                if (waypoints != null && groupsOfWaypoints == null && caches.size() > 1) {
                    for (int i = 0; i < waypoints.size(); i++) {
                        if (!caches_db.isWaypointRecordFound(waypoints.get(i).getName())) {
                            caches_db.insertWpt(waypoints.get(i), -1);
                        } else {
                            Log.i(TAG, "Waypoint found in DB, not again insert!");
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                Log.e("tag", "error", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (caches != null && caches.size() > 0 && groupsOfWaypoints != null && groupsOfWaypoints.size() > 0) {
                Log.i(TAG, "Import caches & wpts with pairing successfully done!");
                Toast.makeText(getApplicationContext(), "Import caches & wpts with pairing successfully done!", Toast.LENGTH_SHORT).show();
            } else if (caches != null && caches.size() > 0 && waypoints != null && waypoints.size() > 0) {
                Log.i(TAG, "Import caches & wpts successfully done!");
                Toast.makeText(getApplicationContext(), "Import caches & wpts successfully done!", Toast.LENGTH_SHORT).show();
            } else if (caches != null && caches.size() > 0 && waypoints == null) {
                Log.i(TAG, "Import caches successfully done!");
                Toast.makeText(getApplicationContext(), "Import caches successfully done!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Import unsuccessfully done! Something wrong with data file!");
                Toast.makeText(getApplicationContext(), "Import  unsuccessfully done! Something wrong with data file!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
