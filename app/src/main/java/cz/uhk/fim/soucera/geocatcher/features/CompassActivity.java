package cz.uhk.fim.soucera.geocatcher.features;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cz.uhk.fim.soucera.geocatcher.R;

public class CompassActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = CompassActivity.class.getName();
    private ImageView image;
    private float currentAzimut = 0f;
    private SensorManager mSensorManager;
    private TextView tvHeading;
    Sensor accelerometer;
    Sensor magnetometer;
    private float azimut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        Log.i(TAG, "onCreate");

        image = (ImageView) findViewById(R.id.iv_compass);
        tvHeading = (TextView) findViewById(R.id.tv_heading_compass);
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            Log.i(TAG, "Sensor for MAGNETIC FIELD is available!");
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else {
            Log.e(TAG, "No sensor for MAGNETIC FIELD!");
            Toast.makeText(this, "No sensor for MAGNETIC FIELD!", Toast.LENGTH_LONG).show();
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            Log.i(TAG, "Sensor for ACCELEROMETER is available!");
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else {
            Log.e(TAG, "No sensor for ACCELEROMETER!");
            Toast.makeText(this, "No sensor for ACCELEROMETER!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        //mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void  onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values;
            //System.out.println("TYPE_ACCELEROMETER");
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            mGeomagnetic = event.values;
            //System.out.println("TYPE_MAGNETIC_FIELD");
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(success){
                //System.out.println("SUCCESS");
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                azimut = (float) Math.toDegrees(orientation[0]); // orientation
                azimut = (azimut + 360) % 360;
            }

        }
        String direction;
        if(azimut < 11.25) direction = "N";
        else if(azimut <= 33.75) direction = "NNE";
        else if(azimut <= 56.25) direction = "NE";
        else if(azimut <= 78.75) direction = "ENE";
        else if(azimut <= 101.25) direction = "E";
        else if(azimut <= 123.75) direction = "ESE";
        else if(azimut <= 146.25) direction = "SE";
        else if(azimut <= 168.75) direction = "SSE";
        else if(azimut <= 191.25) direction = "S";
        else if(azimut <= 213.75) direction = "SSW";
        else if(azimut <= 236.25) direction = "SW";
        else if(azimut <= 258.75) direction = "WSW";
        else if(azimut <= 281.25) direction = "W";
        else if(azimut <= 303.75) direction = "WNW";
        else if(azimut <= 326.25) direction = "NW";
        else if(azimut <= 348.75) direction = "NNW";
        else if(azimut <= 360.00) direction = "N";
        else direction = "None";
        tvHeading.setText("Stupně: " + (int)azimut + "°" + "\n" + "Direction: " + direction);
        RotateAnimation ra = new RotateAnimation(
                -currentAzimut,
                -azimut,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(10000);
        ra.setInterpolator(new LinearInterpolator());
        ra.setFillAfter(true);
        ra.setRepeatCount(Animation.INFINITE);
        ra.setRepeatMode(Animation.INFINITE);
        ra.setFillEnabled(true);
        image.startAnimation(ra);
        currentAzimut = azimut;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
