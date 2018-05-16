package com.hyjy.music.common;
/**
 * Created by WangYiWen on 2018/5/6.
 */
import android.content.Context;


public class DensityUtil {

    /**
     * 根据手机的分辨率从dp转成为px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从px(像素)转成为dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}