package com.hyjy.music;

import android.media.MediaPlayer;

/**
 * 播放音乐接口
 */
public interface IService {

    void callplay(int currentPosition);

    void callpause();

    void callstop();

    MediaPlayer getMediaPlayer();

    MusicService.MyHandler getMyHandlerr();

    void setOnProgessChangedListener(MusicService.OnProgessChangedListener listener);
}