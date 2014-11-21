LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=off
#OPENCV_LIB_TYPE:=SHARED
include /usr/local/adt/OpenCV/sdk/native/jni/OpenCV.mk
LOCAL_SRC_FILES := com_rockidog_remoter_utils_Detector2.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE    := CvDetector2

include $(BUILD_SHARED_LIBRARY)
