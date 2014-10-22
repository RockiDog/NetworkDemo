LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include  $(OPENCV4ANDROID_HOME)/native/jni/OpenCV.mk
LOCAL_MODULE    := CvDetector
LOCAL_SRC_FILES := com_rockidog_networkdemo_utils_Detector.h
LOCAL_C_INCLUDES += $(LOCAL_PATH)

include $(BUILD_SHARED_LIBRARY)
