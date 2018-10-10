package com.example.wangyulong.imagetrans;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.wangyulong.imagetrans.Constant.ControlConstants;
import com.example.wangyulong.imagetrans.Controller.MainScreenController;
import com.example.wangyulong.imagetrans.databinding.ActivityMainScreenBinding;

import org.opencv.android.OpenCVLoader;

public class MainScreenActivity extends AppCompatActivity
{
    //region Fields and Const
    private ActivityMainScreenBinding binding;     // binding class holding all view outlets
    private MainScreenController controller;
    //endregion Fields and Const

    //region Overrides
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // context setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // init binding
        this.databinding_setup();
        this.initialize_UI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        // check if camera access was granted
        if (requestCode == ControlConstants.CAMERA_ACCESS_REQ_DEFAULT)
        {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                this.show_snackbar_msg("No permission to use the Camera!");
                finish();
            }
            else
            {
                this.show_snackbar_msg("Camera access granted!");
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //TODO: Resume camera background thread if necessary
    }
    //endregion Overrides

    //region Methods

    /* initiate binding to view */
    private void databinding_setup()
    {
        // relate binding instance to layout page
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main_screen);
    }

    /* Set up and connect initial UI elements */
    private void initialize_UI()
    {
        this.controller = MainScreenController.get_instance();

        // TODO: Link up to event
        this.binding.textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener()
        {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1)
            {

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1)
            {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
            {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture)
            {

            }
        });

        //TODO: Replace hardcoded values with calculations
        this.binding.textureView.setRotation(ControlConstants.HARDCODED_ROTATION);
        this.binding.textureView.setScaleX(ControlConstants.HARDCODED_SCALING_X);

        // attach button click events
        this.binding.beginFeedButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                run_permission_check();

                controller.OpenCamera((CameraManager) getSystemService(Context.CAMERA_SERVICE), binding.textureView.getSurfaceTexture(), getWindowManager().getDefaultDisplay().getRotation());
            }
        });

        this.controller.warning.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback()
        {
            @Override
            public void onPropertyChanged(final Observable sender, int propertyId)
            {
                // run on UIThread, as UI elements can only be changed by UIThread
                runOnUiThread(new Runnable() {

                    @Override
                    public void run()
                    {
                        // set warning text
                        binding.warningTxt.setText(((ObservableField<String>) sender).get());
                    }
                });
            }
        });
}

    /* checks real-time permission for camera access */
    private void run_permission_check()
    {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            // request for camera access permission with default request code 200
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, ControlConstants.CAMERA_ACCESS_REQ_DEFAULT);
        }
    }

    /* displays msg in a snackbar pop up */
    private void show_snackbar_msg(String msg)
    {
        Snackbar snackbar = Snackbar.make((RelativeLayout) findViewById(R.id.mainScreenRelativeLayout), msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    //endregion Methods

    // region Load External Modules
    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("native-lib");
    }

    static
    {
        System.loadLibrary("opencv_java3");
    }

    static
    {
        if (!OpenCVLoader.initDebug())
        {
            Log.d("MainScreenActivity : ", "OpenCV not loaded");
        }
        else
        {
            Log.d("MainScreenActivity : ", "OpenCV loaded");
        }
    }

    // endregion Load External Modules
}
