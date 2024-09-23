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


import android.util.Log;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.zqy.sdk.voiceview.RecognitionBar;
import com.zqy.sdk.voiceview.RecognitionProgressView;

public class RmsAnimator implements BarParamsAnimator {
    private static final String TAG= RmsAnimator.class.getSimpleName();
    final private List<BarRmsAnimator> barAnimators;


    public RmsAnimator(List<RecognitionBar> recognitionBars) {
        Log.d(TAG, "RmsAnimator: recognitionBarssize="+recognitionBars.size());
        this.barAnimators = new ArrayList<>();
        for (RecognitionBar bar : recognitionBars) {
            barAnimators.add(new BarRmsAnimator(bar));
        }
    }

    @Override
    public void start() {
        for (BarRmsAnimator barAnimator : barAnimators) {
            barAnimator.start();
        }
    }

    @Override
    public void stop() {
        for (BarRmsAnimator barAnimator : barAnimators) {
            barAnimator.stop();
        }
    }

    @Override
    public void animate() {
        for (BarRmsAnimator barAnimator : barAnimators) {
            barAnimator.animate();
        }
    }

    private int frequencyIndex =0;//每绘制五次换一个位置
    private int index;//波形图 左边开始左边
    private Random random=new Random();

    public void onRmsChanged(float rmsDB) {
        int baseLength= RecognitionProgressView.DEFAULT_BARS_HEIGHT_DP_BASE.length;
        int size=barAnimators.size();

//        Log.d(TAG, "onRmsChanged: baseLength="+baseLength+",size="+size);
        if (frequencyIndex >5) {
            frequencyIndex =0;
            //需要随机一个index 用于设定中心点
            int randNum=size-baseLength;
            if (randNum>0){
                index = random.nextInt(randNum);
            }
        }
        for (int i = 0; i < size; i++) {
            BarRmsAnimator barAnimator = barAnimators.get(i);
            int j=i-index;//偏移量
            float displayRmsDb;
            //靠左和靠右的都不要
            if (j<baseLength&&j>=0){
                displayRmsDb= RecognitionProgressView.DEFAULT_BARS_HEIGHT_DP_BASE[j]*rmsDB;
            }else{
                displayRmsDb=0;
            }
            barAnimator.onRmsChanged(displayRmsDb);
        }
//        for (BarRmsAnimator barAnimator : barAnimators) {
//            barAnimator.onRmsChanged(rmsDB);
//        }
        frequencyIndex++;
    }
}