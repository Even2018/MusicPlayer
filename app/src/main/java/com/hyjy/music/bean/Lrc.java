package com.hyjy.music.bean;
/**
 * Created by WangYiWen on 2018/4/16.
 */
import android.support.annotation.NonNull;

/**
 * 歌词Bean
 */
public class Lrc implements Comparable<Lrc> {

    public String content;  //歌词内容
    public long startTime;  //歌词时间

    @Override
    public int compareTo(@NonNull Lrc another) {
        return (int) (this.startTime - another.startTime);
    }
}