package cz.uhk.fim.soucera.geocatcher.features;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import cz.uhk.fim.soucera.geocatcher.R;

public class FlashActivity extends AppCompatActivity {

    private static final String TAG = FlashActivity.class.getName();
    private CameraManager mCameraManager;
    private String mCameraId;
    private Boolean isFlashOn;
    private ImageView imageBulb;
    private Camera cam;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        Button flashOnOffButton = (Button) findViewById(R.id.button_on_off);
        isFlashOn = false;
        imageBulb = (ImageView) findViewById(R.id.imageBulb);
        imageBulb.setBackgroundResource((R.drawable.off_bulb_res));

        if (!isFlashAvailable()) {
            Log.e(TAG, "Device doesn't support flash light!");
            AlertDialog alert = new AlertDialog.Builder(FlashActivity.this).create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                    System.exit(0);
                }
            });
            alert.show();
            return;
        } else {
            Log.i(TAG, "Device support flash light!");
        }

        if (Build.VERSION.SDK_INT >= 23)
        {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            try {
                mCameraId = mCameraManager.getCameraIdList()[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            cam = Camera.open();

            //cam.startPreview();
        }



        flashOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isFlashOn) {
                        turnOffFlashLight();
                        isFlashOn = false;
                        imageBulb.setBackgroundResource((R.drawable.off_bulb_res));
                    } else {
                        turnOnFlashLight();
                        isFlashOn = true;
                        imageBulb.setBackgroundResource((R.drawable.on_bulb_res));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public Boolean isFlashAvailable(){
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    public void turnOnFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.i(TAG, "Turn ON flashLight!");
                mCameraManager.setTorchMode(mCameraId, true);
            } else {
                Log.i(TAG, "Turn ON flashLight!");
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void turnOffFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.i(TAG, "Turn OFF flashLight!");
                mCameraManager.setTorchMode(mCameraId, false);
            } else {
                Log.i(TAG, "Turn OFF flashLight!");
                Camera.Parameters params = cam.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                cam.setParameters(params);
                cam.stopPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isFlashOn){
            turnOffFlashLight();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isFlashOn){
            turnOffFlashLight();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFlashOn){
            turnOnFlashLight();
        }
    }
}
