package com.example.wangyulong.imagetrans.Controller;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;

import com.example.wangyulong.imagetrans.Helper.CameraDeviceHelper;

public class MainScreenController extends BasicController
{
    //region Fields and Const
    private static MainScreenController _instance = null;
    private CameraDeviceHelper cameraDeviceHelper;
    //endregion Fields and Const

    //region Properties
    public static MainScreenController get_instance()
    {
        if (_instance == null)
        {
            _instance = new MainScreenController();
        }

        return _instance;
    }
    //endregion Properties

    //region Constructor
    private MainScreenController()
    {
        super();

        //init
        this.cameraDeviceHelper = CameraDeviceHelper.getInstance();
    }
    //endregion Constructor

    //region APIs

    // permission was to be granted prior to this API call
    @SuppressLint("MissingPermission")
    public void OpenCamera(CameraManager manager, SurfaceTexture texture, int deviceRotation)
    {
        this.cameraDeviceHelper.OpenCamera(manager, texture, deviceRotation);
    }
    //endregion APIs

    //region Methods


    //endregion Methods
}
