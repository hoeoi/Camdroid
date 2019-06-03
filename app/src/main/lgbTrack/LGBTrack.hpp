//
//  LGBTrack.hpp
//  TrackDemo
//
//  Created by gxapp01 on 2019/5/29.
//  Copyright © 2019 hoeoi. All rights reserved.
//

#ifndef LGBTrack_hpp
#define LGBTrack_hpp
#include "opencv2/opencv.hpp"
#include <stdio.h>
#include "CMT.h"
#include "kcftracker.hpp"
#include "LGBTrackControl.hpp"
#include <pthread.h>
#include <semaphore.h>
typedef void (*DidLossTargetCallback)();
typedef void (*DidUpdatedTargetRectCallback)(cv::Rect rect);
typedef cv::Mat* (*GetImageFunc)();

typedef struct {
    cv::Mat image;
    cv::Rect rect;
}ImageRectPairType;

class LGBTrack
{
public:
    LGBTrack(DidLossTargetCallback didLossTargetCallback,
             DidUpdatedTargetRectCallback didUpdatedTargetRectCallback,
             LGBTrackControlCallback callback);
    ~LGBTrack();
    void process(cv::Mat);
    void setTrackTarget(cv::Rect target);
    
    void processImageThread();
    void cmtCheckThread();
    static float percentPointsInRect(std::vector<cv::Point2f> points, cv::Rect rect);
    cv::Size getImageSize();
    void setRectScale(float);
private:
    pthread_mutex_t imageMutex;
    pthread_mutex_t currentImageRectPairMutex;
    cv::Rect targetRect;
    cv::Rect currentRect;
    KCFTracker *kcfTracker;
    cmt::CMT *cmtTracker;
    ImageRectPairType currentImageRectPair;
    pthread_t processImageThreadHandle;
    pthread_t cmtCheckThreadHandle;
    KCFTracker* kcfInit(cv::Mat image ,cv::Rect target);
    cmt::CMT* cmtInit(cv::Mat image , cv::Rect target);
    LGBTrackControl* trackControlInit(LGBTrackControlCallback);
    cv::Mat currentImage;
    cv::Size imageSize;
    int cmtInitKeyPoints;
    bool isTargetLost;
    bool imageUpdateSem;  //图像的信号量
    LGBTrackControl* trackControl;
    DidLossTargetCallback lossTargetFunc;
    DidUpdatedTargetRectCallback didUpdatedTargetRectFunc;
    cv::Mat getImage();
    bool isProcessImageThreadRun;
    bool isProcessImageThreadOut;
    bool isCmtCheckThreadRun;
    bool isCmtCheckThreadOut;
    bool cmtTrackUpdate(cv::Mat image,cv::Rect &rect);
    bool isNeedToCorrectTarget(cv::Mat &image,cv::Rect currentRect);
    void stopKcfTrack();
    void stopCmtTrack();
    void restartKcfTrack(cv::Rect newTarget);
};


#endif /* LGBTrack_hpp */
