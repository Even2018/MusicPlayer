package com.hyjy.music;
/**
 * Created by WangYiWen on 2018/5/6.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * 音量变化的广播接收者
 */
public class VoiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
            if (onVoiceChangedListener != null) {
                //对外暴露当前的媒体音量大小
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                onVoiceChangedListener.setProgress(currVolume);
            }
        }
    }

    private OnVoiceChangedListener onVoiceChangedListener;

    public void setOnVoiceChangedListener(OnVoiceChangedListener onVoiceChangedListener) {
        this.onVoiceChangedListener = onVoiceChangedListener;
    }

    public interface OnVoiceChangedListener {
        void setProgress(int progress);
    }
}