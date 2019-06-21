#include <jni.h>
#include "../lgbTrack/LGBTrack.hpp"
#include "opencv2/opencv.hpp"
#include <android/log.h>

void didLossTargetCallback();
void didUpdatedTargetRectCallback(cv::Rect rect);
void lgbTrackControlCallback(float dx,float dy);

static LGBTrack *lgbTrack;
static jobject s_instance;
JavaVM *g_jvm = NULL;

#define LOG_TAG "JNI_PRINT"
#define DEBUG_LOG_LEVEL    1

#define  LOGE(...)  \
    do{\
        if(DEBUG_LOG_LEVEL > 0)\
        {\
            __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__);\
        }\
    }while(0)


extern "C"
JNIEXPORT void JNICALL
Java_com_hoeoi_lgbTrack_LgbTrack_lgbTrackNativeDeinit(JNIEnv
                                                    *env, jobject instance
)
{

    delete lgbTrack;
    lgbTrack = NULL;
}



void didLossTargetCallback(){
    JNIEnv *env;
    if(g_jvm->AttachCurrentThread(&env, NULL) != JNI_OK)
    {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return;
    }
    jclass myclass = env->GetObjectClass(s_instance);
    jmethodID mid = env->GetMethodID(myclass,"didLossTargetCallback","()V");
    env->CallVoidMethod(s_instance,mid);
    g_jvm->DetachCurrentThread();
}

void didUpdatedTargetRectCallback(cv::Rect rect){

    JNIEnv *env;
    if(g_jvm->AttachCurrentThread(&env, NULL) != JNI_OK)
    {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return;
    }

    jclass myclass = env->GetObjectClass(s_instance);
    jmethodID mid = env->GetMethodID(myclass,"didUpdatedTargetRectCallback","(IIII)V");
    env->CallVoidMethod(s_instance,mid,rect.x,rect.y,rect.x+rect.width,rect.y+rect.height);
    g_jvm->DetachCurrentThread();
}

void lgbTrackControlCallback(float dx,float dy){
//    jclass myclass = s_env->FindClass("com/hoeoi/lgbTrack/LgbTrack");
    JNIEnv *env;
    if(g_jvm->AttachCurrentThread(&env, NULL) != JNI_OK)
    {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return;
    }
    jclass myclass = env->GetObjectClass(s_instance);
    jmethodID mid = env->GetMethodID(myclass,"lgbTrackControlCallback","(FF)V");
    env->CallVoidMethod(s_instance,mid,dx,dy);
    g_jvm->DetachCurrentThread();
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
    if(lgbTrack != nullptr ){
        if(lgbTrack->isProcessing){
            return;
        }
    }
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    // TODO
    cv::Mat image(height+height/2,width,CV_8UC1,data);
    cv::Mat imageResize;
    cv::resize(image, imageResize, cv::Size(), 0.25, 0.25);

    cv::Mat imageBGR;
    imageBGR.create(imageResize.rows,imageResize.cols,CV_8UC3);
    cv::cvtColor(imageResize, imageBGR, COLOR_YUV420sp2BGR);
    if(lgbTrack != nullptr){
       lgbTrack->process(imageBGR);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hoeoi_lgbTrack_LgbTrack_lgbTrackNativeInit(JNIEnv *env, jobject instance) {

    lgbTrack = new LGBTrack(didLossTargetCallback,
                                didUpdatedTargetRectCallback,
                                lgbTrackControlCallback);
    s_instance = env->NewGlobalRef(instance);
    env->GetJavaVM(&g_jvm);


}


