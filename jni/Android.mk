LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=on
#OPENCV_LIB_TYPE:=SHARED
include /usr/local/adt/OpenCV/sdk/native/jni/OpenCV.mk
LOCAL_SRC_FILES := com_rockidog_networkdemo_utils_Detector.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -ldl
LOCAL_MODULE    := CvDetector

include $(BUILD_SHARED_LIBRARY)
