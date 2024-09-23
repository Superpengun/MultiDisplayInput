/*
 * Copyright (C) 2016 Evgenii Zagumennyi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zqy.sdk.voiceview.animators;


import android.graphics.drawable.Drawable;
import android.util.Log;

import com.zqy.sdk.voiceview.RecognitionBar;


public class XAnimator implements BarParamsAnimator {
    private static final String TAG= XAnimator.class.getSimpleName();
    private BarRmsAnimator barAnimator;
    private Drawable cicle;
    private int startHeight,startWidth;
    private int parentWidth,parentHeight;
    private XAnimationListener listener;

    public void setListener(XAnimationListener listener) {
        this.listener = listener;
    }

    public XAnimator(RecognitionBar recognitionBar, Drawable drawable, int width, int height) {
        barAnimator=new BarRmsAnimator(recognitionBar);
        cicle= drawable;

        startHeight=cicle.getIntrinsicHeight();
        startWidth=cicle.getIntrinsicWidth();

        float temp=(height*0.2f)/startHeight;

        startHeight=(int)(temp*startHeight);
        startWidth=(int)(temp*startWidth);
        parentWidth=width;
        parentHeight=height;
    }

    int maxIndexTemp=0;
    int sub=10;
    private void resetCicleSize(float scale){
        Log.d(TAG, "resetCicleSize: scale="+scale);
        //获取想要缩放的matrix
        scale/=10;

        if (scale>5){
            if (maxIndexTemp>6&&maxIndexTemp%sub==1){
                scale=4.9f;
                sub--;
                if (sub<=2)sub=2;
                if (listener!=null){
                    listener.addAnimation();
                }
            }else{
                scale=5f;
            }
            maxIndexTemp++;
        }else{
            maxIndexTemp=0;
            sub=10;
        }
        int w=(int)(startWidth*scale);
        int h=(int)(startHeight*scale);
        cicle.setBounds(parentWidth/2-w/2,parentHeight/2-h/2,parentWidth/2+w/2,parentHeight/2+h/2);
    }

    @Override
    public void start() {
        barAnimator.start();
    }

    @Override
    public void stop() {
        barAnimator.stop();
    }

    @Override
    public void animate() {
        barAnimator.animate();
        resetCicleSize(barAnimator.getBar().getHeight());
    }

    public void onRmsChanged(float rmsDB) {
        barAnimator.onRmsChanged(rmsDB);
    }

    public Drawable getCicle() {
        return cicle;
    }
}