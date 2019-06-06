#include <jni.h>
#include "../lgbTrack/LGBTrack.hpp"
#include "opencv2/opencv.hpp"
#include <android/log.h>

void didLossTargetCallback();
void didUpdatedTargetRectCallback(cv::Rect rect);
void lgbTrackControlCallback(float dx,float dy);

static LGBTrack *lgbTrack;
static JNIEnv *s_env;
static jobject s_instance;





extern "C"
JNIEXPORT jlong JNICALL
Java_com_hoeoi_lgbTrack_LgbTrack_lgbTrackNativeDeinit(JNIEnv
                                                    *env, jobject instance
)
{
    delete lgbTrack;
    lgbTrack = NULL;
}



void didLossTargetCallback(){
    jclass myclass = s_env->FindClass("com/hoeoi/lgbTrack/LgbTrack");
    jmethodID mid = s_env->GetMethodID(myclass,"didLossTargetCallback","()V");
    s_env->CallVoidMethod(s_instance,mid);
}

void didUpdatedTargetRectCallback(cv::Rect rect){
    jclass myclass = s_env->FindClass("com/hoeoi/lgbTrack/LgbTrack");
    jmethodID mid = s_env->GetMethodID(myclass,"didUpdatedTargetRectCallback","(I;I;I;I;)V");
    s_env->CallVoidMethod(s_instance,mid,rect.x,rect.y,rect.x+rect.width,rect.y+rect.height);
}

void lgbTrackControlCallback(float dx,float dy){
    jclass myclass = s_env->FindClass("com/hoeoi/lgbTrack/LgbTrack");
    jmethodID mid = s_env->GetMethodID(myclass,"lgbTrackControlCallback","(F;F;)V");
    s_env->CallVoidMethod(s_instance,mid,dx,dy);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hoeoi_lgbTrack_LgbTrack_lgbTrackNativeSetTrackTarget(JNIEnv *env, jobject instance, jint x,
                                                              jint y, jint width, jint height) {

    // TODO
    if(lgbTrack != nullptr){
        lgbTrack->setTrackTarget(cv::Rect(x,y,width,height));
    }

}extern "C"
JNIEXPORT void JNICALL
Java_com_hoeoi_lgbTrack_LgbTrack_lgbTrackNativeProcess(JNIEnv *env, jobject instance,
                                                       jbyteArray data_,
                                                       jint width,
                                                       jint height) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    // TODO
    cv::Mat image(height+height/2,width,CV_8UC1,data);
    cv::Mat imageBGR;
    imageBGR.create(height,width,CV_8UC3);
    cv::cvtColor(image, imageBGR, COLOR_YUV420sp2BGR);
    if(lgbTrack != nullptr){
       lgbTrack->process(imageBGR);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_hoeoi_lgbTrack_LgbTrack_lgbTrackNativeInit(JNIEnv *env, jobject instance) {

    lgbTrack = new LGBTrack(didLossTargetCallback,
                                didUpdatedTargetRectCallback,
                                lgbTrackControlCallback);
    s_env = env;
    s_instance = instance;
    __android_log_print(ANDROID_LOG_ERROR, "JNITag", "Error....");
    return 0;

}