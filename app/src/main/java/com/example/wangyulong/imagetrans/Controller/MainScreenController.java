package com.example.wangyulong.imagetrans.Controller;

import android.annotation.SuppressLint;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;

import com.example.wangyulong.imagetrans.Helper.CameraDeviceHelper;
import com.example.wangyulong.imagetrans.Model.ImageModel;

public class MainScreenController extends BasicController
{
    //region Fields and Const
    private static MainScreenController _instance = null;
    private CameraDeviceHelper cameraDeviceHelper;
    private ObservableField<ImageModel> imageModel;

    public ObservableField<String> warning;
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

        this.imageModel = new ObservableField<>();

        //update visual cue
        this.imageModel.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback()
        {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId)
            {
                // TODO: display visual cue here
                warning.set(((ObservableField<ImageModel>) sender).get().get_is_clear() ? "" : "WARNING!");
            }
        });

        this.cameraDeviceHelper = CameraDeviceHelper.getInstance(this.imageModel);
        this.warning = new ObservableField<>("");
    }
    //endregion Constructor

    //region APIs

    // permission was to be granted prior to this API call
    @SuppressLint("MissingPermission")
    public void OpenCamera(CameraManager manager, SurfaceTexture texture, int deviceRotation)
    {
        this.cameraDeviceHelper.OpenCamera(manager, texture, deviceRotation);
    }

    /* this API will close the camera */
    public void CloseCamera()
    {
        this.cameraDeviceHelper.CloseCamera();
    }
    //endregion APIs
}
