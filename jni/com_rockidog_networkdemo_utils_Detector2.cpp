#include "com_rockidog_networkdemo_utils_Detector2.h"

#include <opencv2/contrib/detection_based_tracker.hpp>
#include <opencv2/core/core.hpp>
#include <android/log.h>

#include <string>
#include <vector>

#define LOG_TAG "Detector2"
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

JNIEXPORT jlong JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeCreate
(JNIEnv* jenv, jobject , jstring cascade) {
    LOGI("Inside nativeCreate");
    string classifier(jenv->GetStringUTFChars(cascade, 0));
    jlong detector = 0;

    try {
        DetectionBasedTracker::Parameters detector_params;
        //detector_params.minObjectSize = 50;
        detector = (jlong)new DetectionBasedTracker(classifier, detector_params);
        LOGI("Native object created");
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
        LOGE("Fail to create");
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
        LOGE("Fail to create");
        return 0;
    }
    return detector;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeStart
(JNIEnv* jenv, jobject, jlong detector) {
    LOGI("Inside nativeStart");
    try {
        ((DetectionBasedTracker*)detector)->run();
        LOGI("Native detector started");
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
        LOGE("Fail to start");
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
        LOGE("Fail to start");
    }
    return;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeDetect
(JNIEnv* jenv, jobject, jlong detector, jlong frame, jlong faces) {
    //LOGI("Inside nativeDetect");
    try {
        vector<Rect> rect_of_faces;
        ((DetectionBasedTracker*)detector)->process(*((Mat*)frame));
        ((DetectionBasedTracker*)detector)->getObjects(rect_of_faces);
        *((Mat*)faces) = Mat(rect_of_faces, true);
        //LOGI("Detecting");
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
        LOGE("Fail to detect");
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
        LOGE("Fail to detect");
    }
    return;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeStop
(JNIEnv* jenv, jobject detector, jlong) {
    LOGI("Inside nativeStop");
    try {
        ((DetectionBasedTracker*)detector)->stop();
        LOGI("Native detector stopped");
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
        LOGE("Fail to stop");
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
        LOGE("Fail to stop");
    }
    return;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeDestroy
(JNIEnv* jenv, jobject, jlong detector) {
    LOGI("Inside nativeStop");
    try {
        if (0 != detector) {
            ((DetectionBasedTracker*)detector)->stop();
            delete (DetectionBasedTracker*)detector;
        }
        LOGI("Native detector destroyed");
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
        LOGE("Fail to destroy");
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
        LOGE("Fail to destroy");
    }
    return;
}
