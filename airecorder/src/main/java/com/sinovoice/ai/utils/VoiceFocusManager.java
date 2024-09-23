package com.sinovoice.ai.utils;

import android.content.Context;
import android.media.AudioManager;

import com.orhanobut.logger.Logger;


/**
 * Created by liqiang on 2019/6/4.
 **/
public class VoiceFocusManager {
    public static final String TAG = VoiceFocusManager.class.getSimpleName();
    private AudioManager mAudioManager;
    private boolean mAudioFocus;

    private OnFocusLossListener focusLossListener;

    public interface OnFocusLossListener {
        void OnFocusLoss(int reason);
    }

    public void setFocusLossListener(OnFocusLossListener focusLossListener) {
        this.focusLossListener = focusLossListener;
    }

    private VoiceFocusManager() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new VoiceFocusManager();
        }
    }

    public static VoiceFocusManager getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    private static VoiceFocusManager instance = null;


    public void init(Context context) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
    }

    public void release() {
        mAudioManager = null;
    }

    public void requestAudioFocus() {
        if (!mAudioFocus) {
            int result = mAudioManager.requestAudioFocus(afChangeListener,
                    AudioManager.STREAM_MUSIC, // Use the music stream.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = true;
            } else {
                Logger.e(TAG + "AudioManager request Audio Focus result = " + result, "startTrans in error state :$state");
            }
        }
    }

    public void requestBlueToothRecord() {
        mAudioManager.stopBluetoothSco();
        mAudioManager.startBluetoothSco();
    }

    public void releaseBlueToothRecord() {
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        }
    }

    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Logger.d("AudioFocusChange AUDIOFOCUS_GAIN");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    Logger.d("AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Logger.d("AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Logger.d("AudioFocusChange AUDIOFOCUS_LOSS");
                    notifyFocusLoss(AudioManager.AUDIOFOCUS_LOSS);
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Logger.d("AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT");
                    notifyFocusLoss(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Logger.d("AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    notifyFocusLoss(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                default:
                    Logger.d("AudioFocusChange focus = " + focusChange);
                    break;
            }
        }

    };

    private void notifyFocusLoss(int reason) {
        if (focusLossListener != null) {
            focusLossListener.OnFocusLoss(reason);
        }
    }

    public void abandonAudioFocus() {
        Logger.v(TAG + "abandonAudioFocus mAudioFocus = " + mAudioFocus);
        if (mAudioFocus) {
            mAudioManager.abandonAudioFocus(afChangeListener);
            mAudioFocus = false;
        }
    }

}
