//
//  LGBTrackControl.hpp
//  TrackDemo
//
//  Created by gxapp01 on 2019/5/29.
//  Copyright © 2019 hoeoi. All rights reserved.
//

#ifndef LGBTrackControl_hpp
#define LGBTrackControl_hpp

#include <stdio.h>
#include "opencv2/opencv.hpp"


using namespace cv;

typedef void (*LGBTrackControlCallback)(float dx,float dy);

class LGBTrackControl
{
public:

    float xKp;
    float xKd;
    float yKp;
    float yKd;
    float vectorLen;
    float maxRudd;//0-1，最大的旋转控制力度比例
    float maxEle;//0-1, 最大的前后飞控制力度比例
    
    LGBTrackControl(LGBTrackControlCallback);
    void update(cv::Rect rect);
    LGBTrackControlCallback callback;
    void setImageSize(cv::Size);
    void reset();
private:
    cv::Size imageSize;
    cv::Point targetCenter;
    cv::Point lastCenter;
    
};
#endif /* LGBTrackControl_hpp */
