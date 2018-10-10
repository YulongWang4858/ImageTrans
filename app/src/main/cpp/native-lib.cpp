#include <jni.h>
#include <string>
#include <iostream>
#include <vector>
#include <opencv2/opencv.hpp>
#include "opencv2/core.hpp"
#include "opencv2/features2d.hpp"
#include "opencv2/highgui.hpp"

using namespace cv;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_wangyulong_imagetrans_MainScreenActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_wangyulong_imagetrans_Helper_CameraDeviceHelper_featureDetection(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jlong mat_img) {

    // TODO
    std::vector<KeyPoint> keypoints;
    Ptr<Feature2D> orb = ORB::create(500);
    Mat* mat = (Mat*) mat_img;
    Mat descriptor;
    orb->detectAndCompute(*mat, Mat(), keypoints, descriptor);
    return keypoints.size();
}