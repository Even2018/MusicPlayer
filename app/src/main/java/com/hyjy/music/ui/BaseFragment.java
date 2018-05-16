package com.hyjy.music.ui;
/**
 * Created by WangYiWen on 2018/4/13.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyjy.music.IService;
import com.hyjy.music.common.MusicUtil;

/**
 * 三个Fragment的基类
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Fragment所依附的Activity
     */
    protected PlayMusicActivity playMusicActivity;

    protected static final String KEY = "qwerty";

    /**
     * Fragment布局资源id
     */
    protected int resId;

    protected IService myBinder;

    protected MusicUtil musicUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playMusicActivity = (PlayMusicActivity) getActivity();
        musicUtil = new MusicUtil();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        resId = getArguments().getInt(KEY);
        return initView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected abstract View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected abstract void initData();
}