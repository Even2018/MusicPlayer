package com.hyjy.music.view;
/**
 * Created by WangYiWen on 2018/4/29.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.hyjy.music.R;
import com.hyjy.music.bean.Lrc;
import com.hyjy.music.common.LrcParser;

import java.util.ArrayList;

/**
 * 歌词View
 */
public class LrcView extends TextView {

    private int DEFAULT_COLOR = Color.WHITE;    //普通歌词白色
    private static final int LIGHT_COLOR = Color.GREEN;      //高亮歌词绿色
    private float DEFAULT_SIZE;
    private float LIGHT_SIZE;
    private float LYRIC_ROW_HEIGHT;//每一行歌词的高度，要比歌词本身大一点，才会有空隙

    private Paint paint;
    private Rect bounds;
    private int width;
    private int height;

    //歌词集合
    private ArrayList<Lrc> lrcs;
    //高亮行歌词的索引
    private int lightIndex;
    private long currentPosition;//当前歌曲播放的位置
    private long totalDuration;//歌曲的总时间

    public LrcView(Context context) {
        super(context);
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        DEFAULT_SIZE = getResources().getDimension(R.dimen.size_default);
        LIGHT_SIZE = getResources().getDimension(R.dimen.size_light);
        LYRIC_ROW_HEIGHT = getResources().getDimension(R.dimen.size_row);
        paint = new Paint();
        paint.setColor(LIGHT_COLOR);
        paint.setTextSize(LIGHT_SIZE);
        paint.setAntiAlias(true);   //抗锯齿
        //paint.setTextAlign(Paint.Align.LEFT);//设置文本对齐方式

        //创建对象的操作不要放在onDraw方法中
        bounds = new Rect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lrcs == null || lrcs.size() == 0) {
            String text = "亲,去下载歌词吧";
            paint.getTextBounds(text, 0, text.length(), bounds);
            float y = height * 0.5f + bounds.height() * 0.5f;
            drawTextInCenter(canvas, text, y, true);
            return;
        }
        //绘制歌词
        drawLrc(canvas);
    }

    private void drawLrc(Canvas canvas) {
        //在歌词的时间内,向上滚动一个行高的距离
        Lrc lrc = lrcs.get(lightIndex);
        //前后两个的时间差即歌唱的时间,最后一个特殊处理
        float lightTime;    //long类型的赋值给float是可以的
        if (lightIndex == lrcs.size() - 1) {
            lightTime = totalDuration - lrc.startTime;
        } else {
            lightTime = lrcs.get(lightIndex + 1).startTime - lrc.startTime;
        }
        //已经唱了多少时间
        float singedTime = currentPosition - lrc.startTime;
        //根据百分比向上移动
        float percent = singedTime / lightTime;
        float dy = percent * LYRIC_ROW_HEIGHT;
        canvas.translate(0, -dy);

        //绘制多行文本,分三步
        String text;
        //1.先绘制高亮行歌词，得到高亮行歌词的y坐标
        text = lrcs.get(lightIndex).content;
        paint.getTextBounds(text, 0, text.length(), bounds);
        float y = height * 0.5f + bounds.height() * 0.5f;
        drawTextInCenter(canvas, text, y, true);

        //2.再绘制高亮行之前的歌词，就是根据y坐标向前偏移
        for (int i = 0; i < lightIndex; i++) {
            text = lrcs.get(i).content;
            paint.getTextBounds(text, 0, text.length(), bounds);
            float preY = y - (lightIndex - i) * LYRIC_ROW_HEIGHT;
            drawTextInCenter(canvas, text, preY, false);
        }

        //3.在绘制高亮行之后的歌词，也是根据y向后偏移
        for (int i = lightIndex + 1; i < lrcs.size(); i++) {
            text = lrcs.get(i).content;
            paint.getTextBounds(text, 0, text.length(), bounds);
            float nextY = y + (i - lightIndex) * LYRIC_ROW_HEIGHT;
            drawTextInCenter(canvas, text, nextY, false);
        }
    }

    /**
     * 绘制一行居中的文本
     */
    private void drawTextInCenter(Canvas canvas, String text, float y, boolean isLight) {
        paint.setTextSize(isLight ? LIGHT_SIZE : DEFAULT_SIZE);
        paint.setColor(isLight ? LIGHT_COLOR : DEFAULT_COLOR);
        // 计算文本x坐标
        float textWidth = paint.measureText(text);
        float x = width * 0.5f - textWidth * 0.5f;
        // 绘制文本
        canvas.drawText(text, x, y, paint);
    }

    /**
     * 动态滚动歌词
     */
    public void roll(long currentPosition, long totalDuration) {
        this.currentPosition = currentPosition;
        this.totalDuration = totalDuration;

        //1.根据歌曲播放的position，去计算lightLyricIndex
        if (lrcs != null && lrcs.size() > 0) {
            calculateLightLyricIndex();
        }

        //2.刷新重绘, 这里不能放在if语句里面,否则当没有歌词时,不会刷新
        invalidate();
    }

    /**
     * 计算高亮行歌词的索引
     * 逻辑：如果currentPosition>=我的startTime,并且小于我下一行歌词的startTime,
     * 那我的索引就是高亮行索引
     */
    private void calculateLightLyricIndex() {
        long startTime;
        for (int i = 0; i < lrcs.size(); i++) {
            startTime = lrcs.get(i).startTime;
            if (i == (lrcs.size() - 1)) {
                //只需要判断currentPosition否是大于当前startTime
                if (currentPosition >= startTime) {
                    lightIndex = i;
                }
            } else if (i < (lrcs.size() - 1)) {
                //获取下一行歌词的startTime
                long newstartTime = lrcs.get(i + 1).startTime;
                if (currentPosition >= startTime && currentPosition < newstartTime) {
                    lightIndex = i;
                }
            }
        }
    }

    /**
     * 设置歌词路径
     *
     * @param url          歌词路径
     * @param hasAnimation 是否带出场动画
     */
    public void setLrcUrl(String url, boolean hasAnimation) {
        this.lrcs = LrcParser.getLrcList(url);
        if (hasAnimation) {
            //设置歌词出来的动画
            setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.sc_down));
        }
    }

    /**
     * 设置歌词显示的颜色
     */
    public void setLrcColor(int color) {
        if (this.DEFAULT_COLOR != color) {
            this.DEFAULT_COLOR = color;
            postInvalidate();
        }
    }

    /**
     * 设置歌词字体大小
     */
    public void setLrcSize(int mode) {
        switch (mode) {
            case 0:
                DEFAULT_SIZE = getResources().getDimension(R.dimen.size_small);
                LIGHT_SIZE = getResources().getDimension(R.dimen.size_light_small);
                break;
            case 1:
                DEFAULT_SIZE = getResources().getDimension(R.dimen.size_default);
                LIGHT_SIZE = getResources().getDimension(R.dimen.size_light);
                break;
            case 2:
                DEFAULT_SIZE = getResources().getDimension(R.dimen.size_large);
                LIGHT_SIZE = getResources().getDimension(R.dimen.size_light_large);
                break;
            default:
                //默认字体,和1一样
                DEFAULT_SIZE = getResources().getDimension(R.dimen.size_default);
                LIGHT_SIZE = getResources().getDimension(R.dimen.size_light);
                break;
        }
        postInvalidate();
    }

    public void setLightIndex(int lightIndex) {
        this.lightIndex = lightIndex;
    }
}