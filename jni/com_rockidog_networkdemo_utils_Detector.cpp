#include "com_rockidog_networkdemo_utils_Detector.h"

#include <opencv2/contrib/detection_based_tracker.hpp>
#include <opencv2/core/core.hpp>

#include <string>
#include <vector>

using namespace std;
using namespace cv;

JNIEXPORT jlong JNICALL Java_com_rockidog_networkdemo_utils_Detector_nativeCreate
(JNIEnv* jenv, jobject , jstring cascade) {
    string classifier(jenv->GetStringUTFChars(cascade, 0));
    jlong detector = 0;

    try {
        DetectionBasedTracker::Parameters detector_params;
        detector = (jlong)new DetectionBasedTracker(classifier, detector_params);
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
        return 0;
    }
    return detector;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector_nativeStart
(JNIEnv* jenv, jobject, jlong detector) {
    try {
        ((DetectionBasedTracker*)detector)->run();
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
    }
    return;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector_nativeDetect
(JNIEnv* jenv, jobject, jlong detector, jlong frame, jlong faces) {
    try {
        vector<Rect> rect_of_faces;
        ((DetectionBasedTracker*)detector)->process(*((Mat*)frame));
        ((DetectionBasedTracker*)detector)->getObjects(rect_of_faces);
        *((Mat*)faces) = Mat(rect_of_faces, true);
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
    }
    return;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector_nativeStop
(JNIEnv* jenv, jobject detector, jlong) {
    try {
        ((DetectionBasedTracker*)detector)->stop();
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
    }
    return;
}

JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector_nativeDestroy
(JNIEnv* jenv, jobject, jlong detector) {
    try {
        if (0 != detector) {
            ((DetectionBasedTracker*)detector)->stop();
            delete (DetectionBasedTracker*)detector;
        }
    }
    catch (cv::Exception& e) {
        jclass java_exception = jenv->FindClass("org/opencv/core/CvException");
        if (0 == java_exception) {
            java_exception = jenv->FindClass("org/opencv/core/CvException");
        }
        jenv->ThrowNew(java_exception, e.what());
    }
    catch (...) {
        jclass java_exception = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(java_exception, "Unknow exception");
    }
    return;
}
