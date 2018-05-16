package com.hyjy.music.ui;
/**
 * Created by WangYiWen on 2018/4/26.
 */
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyjy.music.MusicService;
import com.hyjy.music.R;
import com.hyjy.music.common.CommonPopupWindow;
import com.hyjy.music.view.LrcView;

import java.util.Arrays;
import java.util.List;

/**
 * 歌词Fragment
 */
public class LrcFragment extends BaseFragment implements View.OnClickListener {

    public LrcView lrcView;

    //歌词颜色选择
    private View ll_color;

    //歌词字体大小
    private View ll_size;

    private View red;
    private View qing;
    private View blue;
    private View yellow;
    private View write;
    private View small;
    private View middle;
    private View large;

    private View size_close;
    private View color_close;

    private TextView tv_more;

    private List<String> stringList;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = View.inflate(playMusicActivity, resId, null);
        lrcView = (LrcView) rootView.findViewById(R.id.lrcView);
        ll_color = rootView.findViewById(R.id.ll_color);
        ll_size = rootView.findViewById(R.id.ll_size);
        red = rootView.findViewById(R.id.iv_red);
        qing = rootView.findViewById(R.id.iv_qing);
        blue = rootView.findViewById(R.id.iv_blue);
        yellow = rootView.findViewById(R.id.iv_yellow);
        write = rootView.findViewById(R.id.iv_write);
        small = rootView.findViewById(R.id.tv_small);
        middle = rootView.findViewById(R.id.tv_middle);
        large = rootView.findViewById(R.id.tv_large);
        tv_more = (TextView) rootView.findViewById(R.id.tv_more);
        size_close = rootView.findViewById(R.id.size_close);
        color_close = rootView.findViewById(R.id.color_close);
        return rootView;
    }

    @Override
    protected void initData() {
        red.setOnClickListener(this);
        qing.setOnClickListener(this);
        blue.setOnClickListener(this);
        yellow.setOnClickListener(this);
        write.setOnClickListener(this);
        small.setOnClickListener(this);
        middle.setOnClickListener(this);
        large.setOnClickListener(this);
        size_close.setOnClickListener(this);
        color_close.setOnClickListener(this);
        tv_more.setOnClickListener(clickListener);

        String[] array = getResources().getStringArray(R.array.more);
        stringList = Arrays.asList(array);
    }

    public static LrcFragment newInstance() {
        LrcFragment fragment = new LrcFragment();
        Bundle args = new Bundle();
        args.putInt(KEY, R.layout.show_lrc);
        fragment.setArguments(args);
        return fragment;
    }


    public void showLrc(final String url, boolean hasAnimation) {
        //防止角标越界
        lrcView.setLightIndex(0);
        lrcView.setLrcUrl(url, hasAnimation);
        final MusicService.MyHandler handler = myBinder.getMyHandlerr();
        handler.post(new Runnable() {
            @Override
            public void run() {
                MediaPlayer mediaPlayer = myBinder.getMediaPlayer();
                if (mediaPlayer == null) {
                    //防止空指针异常
                    return;
                }
                lrcView.roll(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
                handler.post(this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        //只有颜色的圈圈才播放动画,整个布局不播放动画
        v.startAnimation(playMusicActivity.scAnimation);
        switch (v.getId()) {
            case R.id.iv_red:
                lrcView.setLrcColor(0xFFFF4081);
                break;
            case R.id.iv_qing:
                lrcView.setLrcColor(0xFF00FFFF);
                break;
            case R.id.iv_blue:
                lrcView.setLrcColor(0xFF3F51B5);
                break;
            case R.id.iv_yellow:
                lrcView.setLrcColor(0xFFFFFF00);
                break;
            case R.id.iv_write:
                lrcView.setLrcColor(0xFFFFFFFF);
                break;
            case R.id.tv_small:
                lrcView.setLrcSize(0);
                break;
            case R.id.tv_middle:
                lrcView.setLrcSize(1);
                break;
            case R.id.tv_large:
                lrcView.setLrcSize(2);
                break;
            case R.id.color_close:
                ll_color.setVisibility(View.INVISIBLE);
                break;
            case R.id.size_close:
                ll_size.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private CommonPopupWindow<String> commonPopupWindow;

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (commonPopupWindow == null) {
                commonPopupWindow = new CommonPopupWindow<String>(playMusicActivity, stringList) {
                    @Override
                    public void onClick(String s) {
                        doSomeThing(s);
                    }
                };
            }
            commonPopupWindow.showAsDropDown(v);
        }
    };


    private void doSomeThing(String s) {
        if ("歌词大小".equals(s)) {
            ll_size.setVisibility(View.VISIBLE);
        }
    }

    public void startAnimator() {
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(tv_more, "alpha",
                0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f);
        oa1.setDuration(5000);
        oa1.setRepeatCount(ObjectAnimator.INFINITE);
        oa1.setRepeatMode(ObjectAnimator.REVERSE);

        ObjectAnimator oa2 = ObjectAnimator.ofFloat(tv_more, "rotationY",
                0, 45, 90, 135, 180, 225, 270, 315, 360);
        oa2.setDuration(5000);
        oa2.setRepeatCount(ObjectAnimator.INFINITE);
        oa2.setRepeatMode(ObjectAnimator.REVERSE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(oa1, oa2);
        set.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAnimator();
        Log.i("LrcFragment", "开启动画");
    }

    @Override
    public void onPause() {
        super.onPause();
        tv_more.clearAnimation();
        Log.i("LrcFragment", "清除动画");
    }
}