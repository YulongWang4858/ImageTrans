package com.example.wangyulong.imagetrans.Helper;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import com.example.wangyulong.imagetrans.Constant.ControlConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraDeviceHelper
{
    // region Fields and Const
    private static CameraDeviceHelper _instance = null;

    private Size dimension;
    private CameraDevice m_camera_device;   // static camera info
    private CaptureRequest.Builder capture_request_builder;
    private CameraCaptureSession mCamera_capture_session;    // image stream
    private Handler background_handler;
    private String cam_id;
    private SurfaceTexture texture;    // capture & stores preview images
    private int net_orientation = 0;
    private ImageReader image_reader;
    private HandlerThread background_handler_thread;

    // new image event
    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener()
    {
        @Override
        public void onImageAvailable(ImageReader imageReader)
        {
            // called whenever a new img is available
            Image new_image = image_reader.acquireLatestImage();

            if (new_image != null)
            {
                Log.d("CameraDeviceHelper : ", "OpenCamera -> New image read successfully");

                //TODO: Remove after testing

                new_image.close();
            }
        }
    };

    // camera rotation lookup table
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray(4);
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // event handler based on camera access result
    private CameraDevice.StateCallback state_callback = new CameraDevice.StateCallback()
    {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice)
        {
            m_camera_device = cameraDevice;

            // camera open success, attempt to open live feed channel
            if (open_live_feed(texture))
            {
                Log.d("MainScreenController ", "CameraDevice state_callback -> live feeding");
            }
            else
            {
                Log.d("MainScreenController ", "CameraDevice state_callback -> no live feed");
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice)
        {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i)
        {
            cameraDevice.close();
            m_camera_device = null;

            Log.d("CameraDeviceHelper : ", "StateCallback -> Camera Open Failed!");
        }
    };

    // endregion Fields and Const

    // region Properties
    public static CameraDeviceHelper getInstance()
    {
        if (_instance == null)
        {
            _instance = new CameraDeviceHelper();
        }

        return _instance;
    }

    //endregion Properties

    // region Constructor
    private CameraDeviceHelper()
    {
    }
    // endregion Constructor

    // region APIs
    @SuppressLint("MissingPermission")
    public boolean OpenCamera(CameraManager manager, SurfaceTexture texture, int deviceRotation)
    {
        try
        {
            this.texture = texture;
            this.cam_id = manager.getCameraIdList()[0];

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(this.cam_id);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map == null)
            {
                return false;
            }

            Size[] sizes = map.getOutputSizes(ImageReader.class);

            // check for invalid map
            if (sizes.length <= 0)
            {
                return false;
            }

            // begin background thread
            background_handler_thread = new HandlerThread("Camera Background");
            background_handler_thread.start();
            background_handler = new Handler(background_handler_thread.getLooper());

            // retrieve image size and instantiate image reader
            Size image_size = new Size(sizes[0].getWidth(), sizes[0].getHeight());
            this.image_reader = ImageReader.newInstance(image_size.getWidth(), image_size.getHeight(), ImageFormat.YUV_420_888, ControlConstants.MAX_NUM_OF_IMAGES_PER_PROCESS_INTERVAL);
            this.image_reader.setOnImageAvailableListener(this.imageAvailableListener, background_handler);

            // compute preview rotation based on current sensor rotation and device rotation
            this.net_orientation = calculate_rotation(characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION), deviceRotation);

            // obtain resolution
            this.dimension = map.getOutputSizes(SurfaceTexture.class) [0];

            // attempt to open camera
            manager.openCamera(cam_id, state_callback, null);

            return true;
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

        return false;
    }
    // endregion APIs

    // region Methods
    /* direct live feed on texture */
    private boolean open_live_feed(SurfaceTexture texture)
    {
        if (texture != null)
        {
            try
            {
                // set live feed display dimensions
                texture.setDefaultBufferSize(dimension.getWidth(), dimension.getHeight());

                // configure all surfaces
                List all_surfaces = new ArrayList();
                Surface surface = new Surface(texture);
                Surface reader_surface = this.image_reader.getSurface();
                all_surfaces.add(surface);
                all_surfaces.add(reader_surface);

                // request device on camera live feed
                this.capture_request_builder = this.m_camera_device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                this.capture_request_builder.addTarget(surface);
                this.capture_request_builder.addTarget(reader_surface);
                //this.capture_request_builder.set(CaptureRequest.JPEG_ORIENTATION, 0);

                this.m_camera_device.createCaptureSession(all_surfaces, new CameraCaptureSession.StateCallback()
                {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession)
                    {
                        mCamera_capture_session = cameraCaptureSession;
                        update_live_feed();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession)
                    {
                        Log.d("MainScreenController ", "OpenCamera -> CaptureSession Configuration Failed!");
                    }
                }, null);
            }
            catch (CameraAccessException e)
            {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    private void update_live_feed()
    {
        // only update for existing camera device
        if (this.m_camera_device != null)
        {
            // configure capture req
            this.capture_request_builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

            try
            {
                // attempt to access camera stream
                mCamera_capture_session.setRepeatingRequest(capture_request_builder.build(), null, background_handler);
            }
            catch (CameraAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    //TODO: Use after testing
    private int calculate_rotation(int sensorRotation, int deviceRotation)
    {
//        return (ORIENTATIONS.get(deviceRotation) + sensorRotation + 270) % 360;
        return 0;
    }


    // endregion Methods
}
