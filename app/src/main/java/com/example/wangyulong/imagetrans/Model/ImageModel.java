package com.example.wangyulong.imagetrans.Model;

import android.databinding.Observable;
import android.graphics.ImageFormat;
import android.media.Image;

import com.example.wangyulong.imagetrans.Constant.ControlConstants;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

public class ImageModel
{
    // region Fields and Const
    private int num_features = 0;
    private boolean is_clear = true;
    private Mat mat_img = null;
    // endregion Fields and Const

    // region Properties

    public int getNum_features()
    {
        return num_features;
    }

    public void setNum_features(int num_features)
    {
        this.num_features = num_features;
    }

    public boolean get_is_clear()
    {
        return (this.num_features < ControlConstants.ORB_FEATURE_DETECTION_THRESHOLD);
    }

    public Mat getMat_img()
    {
        return mat_img;
    }

    public void setMat_img(Mat mat_img)
    {
        this.mat_img = mat_img;
    }

    // endregion Properties

    // region Constructors
    public ImageModel()
    {
        super();
    }

    public ImageModel(Image img)
    {
        super();

        this.mat_img = this.imageToMat(img);
    }
    // endregion Constructors

    // region Methods
    private Mat imageToMat(Image image)
    {
        // init
        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int width = image.getWidth();
        int height = image.getHeight();
        int offset = 0;

        // configure image to live feed snapshot formats
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        // transform with byteBuffer
        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                int bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
                if (pixelStride == bytesPerPixel) {
                    int length = w * bytesPerPixel;
                    buffer.get(data, offset, length);

                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                    offset += length;
                } else {


                    if (h - row == 1) {
                        buffer.get(rowData, 0, width - pixelStride + 1);
                    } else {
                        buffer.get(rowData, 0, rowStride);
                    }

                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
            }
        }

        // retrieve mat result
        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC1);
        mat.put(0, 0, data);

        return mat;
    }
    // endregion Methods
}
