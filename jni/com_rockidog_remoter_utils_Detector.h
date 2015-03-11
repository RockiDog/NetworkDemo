/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_rockidog_remoter_utils_Detector */

#ifndef _Included_com_rockidog_remoter_utils_Detector
#define _Included_com_rockidog_remoter_utils_Detector
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_rockidog_remoter_utils_Detector
 * Method:    nativeCreate
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_rockidog_remoter_utils_Detector_nativeCreate
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_rockidog_remoter_utils_Detector
 * Method:    nativeStart
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_remoter_utils_Detector_nativeStart
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_rockidog_remoter_utils_Detector
 * Method:    nativeDetect
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_remoter_utils_Detector_nativeDetect
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     com_rockidog_remoter_utils_Detector
 * Method:    nativeStop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_remoter_utils_Detector_nativeStop
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_rockidog_remoter_utils_Detector
 * Method:    nativeDestroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_remoter_utils_Detector_nativeDestroy
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif