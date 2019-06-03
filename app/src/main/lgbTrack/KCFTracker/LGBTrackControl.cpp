//
//  LGBTrackControl.cpp
//  TrackDemo
//
//  Created by gxapp01 on 2019/5/29.
//  Copyright © 2019 hoeoi. All rights reserved.
//

#include "LGBTrackControl.hpp"

LGBTrackControl::LGBTrackControl(LGBTrackControlCallback callback){
    this->maxEle = 0.78;
    this->maxRudd = 0.5;
    this->xKp = 1.5;
    this->xKd = 1.0;
    this->yKp = 2.0;
    this->yKd = 1.5;
    setImageSize(cv::Size(640,480));
    this->callback = callback;
}

void LGBTrackControl::setImageSize(cv::Size size){
    this->imageSize = size;
    this->targetCenter = cv::Point(size.width/2,size.height/2);
}

void LGBTrackControl::update(cv::Rect rect){
    
        int x = rect.x;
        int y = rect.y;
        int width = rect.width;
        int height = rect.height;

        cv::Point center;
        center.x = x + width/2;
        center.y = y + height/2;
        float Ex = (center.x - this->targetCenter.x)/(float)this->imageSize.width;
        float lastEx = (this->lastCenter.x - this->targetCenter.x)/(float)this->imageSize.width;
        float Ey = (center.y - this->targetCenter.y)/(float)this->imageSize.height;
        float lastEy = (lastCenter.y - this->targetCenter.y)/(float)this->imageSize.height;
        float Outx = this->xKp * Ex+ this->xKd * (Ex - lastEx);
        float Outy = this->yKp * Ey+ this->yKd * (Ey - lastEy);
        Outx = Outx > this->maxRudd ? this->maxRudd:Outx;
        Outx = Outx < -this->maxRudd ? -this->maxRudd:Outx;
        
        Outy = Outy > this->maxEle ? this->maxEle:Outy;
        Outy = Outy < -this->maxEle ? -this->maxEle :Outy;
        
        lastCenter = center;
        if (this->callback) {
            float dx,dy;
            if (width == 0) {
                dx = 0;
                dy = 0;
            }
            else{
                dx = Outx*this->vectorLen;
                dy = Outy*this->vectorLen;
            }
            this->callback(dx,dy);
        }
    
}

void LGBTrackControl::reset(){
    lastCenter = this->targetCenter;
}
