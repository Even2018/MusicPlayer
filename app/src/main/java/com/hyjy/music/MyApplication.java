package com.hyjy.music;
/**
 * Created by WangYiWen on 2018/5/10.
 */
import android.app.Application;

public class MyApplication extends Application {

    /**
     * 当前音乐的播放位置,public static修饰方便全局调用
     */
    public static int position = -1;

    /**
     * 发广播的action
     */
    public static final String ACTION = "com.hyjy.music.UI";

}