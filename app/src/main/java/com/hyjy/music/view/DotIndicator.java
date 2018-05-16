package com.hyjy.music.view;
/**
 * Created by WangYiWen on 2018/4/26.
 */
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hyjy.music.R;
import com.hyjy.music.common.DensityUtil;

/**
 * ViewPager小圆点指示器
 */
public class DotIndicator extends FrameLayout {

    private static final String TAG = "ViewPagerIndicator";

    private LinearLayout ll_container;
    private ImageView iv_red_point;

    public DotIndicator(Context context) {
        super(context);
        init();
    }

    public DotIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DotIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.indicator_vp, this);
        ll_container = (LinearLayout) view.findViewById(R.id.ll_container);
        iv_red_point = (ImageView) view.findViewById(R.id.iv_red_point);
    }

    /**
     * 给ViewPager设置了适配器之后才能调用
     *
     * @param viewPager ViewPager
     * @param mode      小圆点的大小
     */
    public void setViewPager(ViewPager viewPager, Mode mode) {
        if (viewPager == null) {
            throw new IllegalArgumentException("ViewPager不能为空呀");
        }
        if (viewPager.getAdapter() == null) {
            throw new RuntimeException("请先设置ViewPager的适配器哦");
        }
        int green;
        int red;
        int dp;
        switch (mode) {
            case small:
                green = R.drawable.small_green_point;
                red = R.drawable.small_red_point;
                dp = DensityUtil.dip2px(getContext(), 6.0f);
                break;
            case middle:
                green = R.drawable.middle_green_point;
                red = R.drawable.middle_red_point;
                dp = DensityUtil.dip2px(getContext(), 9.0f);
                break;
            case large:
                green = R.drawable.large_green_point;
                red = R.drawable.large_red_point;
                dp = DensityUtil.dip2px(getContext(), 12.0f);
                break;
            default:
                green = R.drawable.small_green_point;
                red = R.drawable.small_red_point;
                dp = DensityUtil.dip2px(getContext(), 6.0f);
                break;
        }
        iv_red_point.setBackgroundResource(red);

        ImageView imageView;
        LinearLayout.LayoutParams params;
        int count = viewPager.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            // 向LinearLayout中添加小圆点
            imageView = new ImageView(getContext());
            imageView.setBackgroundResource(green);
            //初始化布局参数
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                //第一个点不设置左边距
                params.leftMargin = dp;
            }
            imageView.setLayoutParams(params);
            ll_container.addView(imageView);
        }
        //设置页面改变的监听,实时移动小红点
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //ViewPager页面滑动时，根据滑动百分比，小圆点也跟着移动
            int left0 = ll_container.getChildAt(0).getLeft();
            int left1 = ll_container.getChildAt(1).getLeft();
            int d = left1 - left0;
            iv_red_point.setTranslationX(d * positionOffset + d * position);
        }

        @Override
        public void onPageSelected(int position) {
            Log.i(TAG, "当前位置:" + position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public enum Mode {
        small,
        middle,
        large
    }

}