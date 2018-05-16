package com.hyjy.music.ui;
/**
 * Created by WangYiWen on 2018/5/2.
 */
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.hyjy.music.R;
import com.hyjy.music.VoiceReceiver;
import com.hyjy.music.view.CircleImageView;

/**
 * 封面专辑Fragment
 */
public class PhotoFragment extends BaseFragment {

    private View rootView;
    private LinearLayout ll;
    private SeekBar voiceSeekBar;
    private CircleImageView circleImageView;

    private VoiceReceiver voiceReceiver;

    //音量进度条是否显示
    private boolean isShow = true;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = View.inflate(playMusicActivity, resId, null);
        ll = (LinearLayout) rootView.findViewById(R.id.ll);
        voiceSeekBar = (SeekBar) rootView.findViewById(R.id.voiceSeekBar);
        circleImageView = (CircleImageView) rootView.findViewById(R.id.circleImageView);
        return rootView;
    }

    @Override
    protected void initData() {
        final AudioManager audioManager = (AudioManager) playMusicActivity.getSystemService(Context.AUDIO_SERVICE);
        int maxVoice = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVoice = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        voiceSeekBar.setMax(maxVoice);
        voiceSeekBar.setProgress(currentVoice);
        voiceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //第三个参数如果传1,则滑动SeekBar时,系统的音量调节界面也会显示出来
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //点击时显示/隐藏音量
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShow = !isShow;
                ll.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
            }
        });

        //注册广播接收者
        voiceReceiver = new VoiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        playMusicActivity.registerReceiver(voiceReceiver, filter);
        voiceReceiver.setOnVoiceChangedListener(new VoiceReceiver.OnVoiceChangedListener() {
            @Override
            public void setProgress(int progress) {
                ll.setVisibility(View.VISIBLE);
                voiceSeekBar.setProgress(progress);
                isShow = true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playMusicActivity != null) {
            playMusicActivity.unregisterReceiver(voiceReceiver);
        }
        circleImageView.clearAnimation();
    }

    public static PhotoFragment newInstance() {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt(KEY, R.layout.photo);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPlayStatus(boolean isPlaying, Bitmap bitmap) {
        if (isPlaying) {
            circleImageView.clearAnimation();
            circleImageView.startAnimation(playMusicActivity.playAnimation);
            circleImageView.setImageBitmap(bitmap);
        } else {
            circleImageView.clearAnimation();
        }
    }

    public void setShow(boolean show) {
        circleImageView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}