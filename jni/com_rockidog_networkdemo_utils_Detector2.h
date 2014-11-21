/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_rockidog_networkdemo_utils_Detector2 */

#ifndef _Included_com_rockidog_networkdemo_utils_Detector2
#define _Included_com_rockidog_networkdemo_utils_Detector2
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_rockidog_networkdemo_utils_Detector2
 * Method:    nativeCreate
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeCreate
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_rockidog_networkdemo_utils_Detector2
 * Method:    nativeDestroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeDestroy
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_rockidog_networkdemo_utils_Detector2
 * Method:    nativeStart
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeStart
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_rockidog_networkdemo_utils_Detector2
 * Method:    nativeStop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeStop
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_rockidog_networkdemo_utils_Detector2
 * Method:    nativeDetect
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_com_rockidog_networkdemo_utils_Detector2_nativeDetect
  (JNIEnv *, jobject, jlong, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif