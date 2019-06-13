//
//  LGBTrack.cpp
//  TrackDemo
//
//  Created by gxapp01 on 2019/5/29.
//  Copyright © 2019 hoeoi. All rights reserved.
//

#include "LGBTrack.hpp"
#include <unistd.h>

#define cmtTh 0.6
#define image_scale (1.0/10.0)

using namespace cv;
using namespace std;

void* processImageThreadBridge(void *arg);
void* cmtCheckThreadBridge(void *arg);



LGBTrack::LGBTrack(DidLossTargetCallback didLossTargetCallback,
                   DidUpdatedTargetRectCallback didUpdatedTargetRectCallback,
                   LGBTrackControlCallback trackControlCallback){
    
    this->lossTargetFunc = didLossTargetCallback;
    this->didUpdatedTargetRectFunc = didUpdatedTargetRectCallback;
    trackControl = trackControlInit(trackControlCallback);
    //创建线程

    imageUpdateSem = false;
    isProcessing = false;
    isProcessImageThreadOut = false;
    isCmtCheckThreadOut = false;

    pthread_mutex_init(&imageMutex,NULL);
    pthread_mutex_init(&currentImageRectPairMutex,NULL);
    
}

LGBTrack::~LGBTrack(){
    stopKcfTrack();
    stopCmtTrack();
}

void LGBTrack::stopKcfTrack(){
    isProcessImageThreadRun = false;
    while (!isProcessImageThreadOut) {
        usleep(1000);
    }
    delete kcfTracker;
    kcfTracker = NULL;
}

void LGBTrack::stopCmtTrack(){
    isCmtCheckThreadRun = false;
    while (!isCmtCheckThreadOut) {
        usleep(1000);
    }
    delete cmtTracker;
    cmtTracker = NULL;
}

void LGBTrack::setTrackTarget(cv::Rect target){
    cv::Mat image;
    do {
        image = getImage();
        usleep(2000);
    } while (image.cols == 0);
    
    targetRect = target;

    currentImageRectPair.image = image;
    currentImageRectPair.rect = target;

    imageSize = cv::Size(image.cols,image.rows);
    kcfTracker = kcfInit(image,target);
    cmtTracker = cmtInit(image,target);
    cmtInitKeyPoints = (int)cmtTracker->points_active.size();
    isTargetLost = false;
    isProcessImageThreadRun = true;
    pthread_create(&processImageThreadHandle, NULL, processImageThreadBridge, this);
    isCmtCheckThreadRun = true;
    pthread_create(&cmtCheckThreadHandle, NULL, cmtCheckThreadBridge, this);
}

KCFTracker* LGBTrack::kcfInit(cv::Mat image ,cv::Rect target){
    int HOG = true;
    int FIXEDWINDOW = true;
    int MULTISCALE = true;
    int LAB = false;
    KCFTracker* tracker = new KCFTracker(HOG, FIXEDWINDOW, MULTISCALE, LAB);
    tracker->init(target, image);
    return tracker;
}

void LGBTrack::restartKcfTrack(cv::Rect newTarget){
    stopKcfTrack();
    cv::Mat image;
    do {
        image = getImage();
        usleep(2000);
    } while (image.cols == 0);
    targetRect = newTarget;
    pthread_mutex_lock(&currentImageRectPairMutex);;
    currentImageRectPair.image = image;
    currentImageRectPair.rect = newTarget;
    pthread_mutex_unlock(&currentImageRectPairMutex);;
    kcfTracker = kcfInit(image,newTarget);
    isTargetLost = false;
    isProcessImageThreadRun = true;
     isProcessImageThreadOut = false;
    pthread_create(&processImageThreadHandle, NULL, processImageThreadBridge, this);
}

cmt::CMT* LGBTrack::cmtInit(cv::Mat image, cv::Rect target){
    Mat img_gray;
    cvtColor(image,img_gray,COLOR_BGR2GRAY);
    cmtTracker = new cmt::CMT();
    cmtTracker->initialize(img_gray,targetRect);
    printf("cmt track init!");
    return cmtTracker;
}

LGBTrackControl* LGBTrack::trackControlInit(LGBTrackControlCallback callback){
    LGBTrackControl* trackControl = new LGBTrackControl(callback);
    trackControl->setImageSize(this->imageSize);
    trackControl->vectorLen=0x80;
    trackControl->xKp=1.5;
    trackControl->xKd=1.0;
    trackControl->maxEle=1;
    trackControl->maxEle=1;
    return trackControl;
}

cv::Mat LGBTrack::getImage(){
    cv::Mat image ;
    int value = 0;

    pthread_mutex_lock(&imageMutex);
    image = currentImage;
    pthread_mutex_unlock(&imageMutex);
    return image;
}

void LGBTrack::process(cv::Mat image){
    if(0 != pthread_mutex_trylock(&imageMutex)){
        return;
    }

//    cv::Mat imageResize,image2;
//    cv::resize(image, imageResize, cv::Size(), image_scale, image_scale);
    
//    cvtColor(imageResize, image2, COLOR_BGRA2BGR);
    
    currentImage = image;
    pthread_mutex_unlock(&imageMutex);
    imageUpdateSem = true;
}



void LGBTrack::processImageThread(){
    while(isProcessImageThreadRun){
//        image = getImage();
//        if (image.cols == 0) {
//            continue;
//        }
        while((!imageUpdateSem)&isProcessImageThreadRun){
            usleep(500);
        }
        imageUpdateSem = false;
        
        pthread_mutex_lock(&imageMutex);;
        isProcessing = true;
        cv::Mat image = currentImage;

        cv::Rect rect(0,0,0,0);
        if(isTargetLost){
            if (lossTargetFunc != NULL) {
                lossTargetFunc();
            }
        }
        else{
            //开始跟随
            
            rect = kcfTracker->update(image);
            
            cv::Rect image2Rect = cv::Rect(0,0,image.cols,image.rows);

            if ((image2Rect&rect) != rect) {
                isTargetLost = true;
            }

            if(didUpdatedTargetRectFunc != NULL){
                didUpdatedTargetRectFunc(rect);
            }
            trackControl->update(rect);
        }
        pthread_mutex_lock(&currentImageRectPairMutex);;
        currentImageRectPair.image = image;
        currentImageRectPair.rect = rect;
        pthread_mutex_unlock(&currentImageRectPairMutex);;
        pthread_mutex_unlock(&imageMutex);
        isProcessing = false;
    }
    isProcessImageThreadOut = true;
}

void* processImageThreadBridge(void *arg){
    LGBTrack *track = (LGBTrack *)arg;
    track->processImageThread();
    return NULL;
}

void* cmtCheckThreadBridge(void *arg){
    LGBTrack *track = (LGBTrack *)arg;
    track->cmtCheckThread();
    return NULL;
}
void LGBTrack::cmtCheckThread(){
    while(isCmtCheckThreadRun){
        usleep(300000);
        ImageRectPairType pair;
        
        pthread_mutex_lock(&currentImageRectPairMutex);;
        pair = currentImageRectPair;
        pthread_mutex_unlock(&currentImageRectPairMutex);;
        
        bool isNeedCorrect = isNeedToCorrectTarget(pair.image, pair.rect);
        
        if(isNeedCorrect){
            printf("bug1: need to correctTarget\n");
            cv::Rect newTarget;
            newTarget = boundingRect(cmtTracker->points_active);
            restartKcfTrack(newTarget);
            
        }
        
    }
    isCmtCheckThreadOut = true;
}
    



bool LGBTrack::isNeedToCorrectTarget(cv::Mat &image,cv::Rect currentRect){
    cv::Rect newRect;
    bool isGetCmtRect = cmtTrackUpdate(image, newRect);
    if (isGetCmtRect) {
       if(LGBTrack::percentPointsInRect(cmtTracker->points_active, currentRect) < 0.6){
            return true;
        }
    }
    return false;
}

float LGBTrack::percentPointsInRect(std::vector<cv::Point2f> points, cv::Rect rect){
    float inRectPercent = 0;
    int inRectPointCnt = 0;
    for(size_t i = 0; i < points.size(); i++)
    {
        if(rect.contains(points[i])){
            inRectPointCnt ++;
        }
    }
    inRectPercent = inRectPointCnt*1.0/ points.size();
    return inRectPercent;
}

bool LGBTrack::cmtTrackUpdate(cv::Mat image,cv::Rect &rect){
    Mat img_gray;
    cvtColor(image,img_gray,COLOR_BGR2GRAY);
    cmtTracker->processFrame(img_gray);
    size_t cmtCurKeyPoint = cmtTracker->points_active.size();  //识别到的关键特征点的数量
    if (cmtCurKeyPoint > cmtInitKeyPoints*cmtTh){
        rect = cv::boundingRect(cmtTracker->points_active);
        return true;
    }
    return false;
    
}


