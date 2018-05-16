package com.hyjy.music;
/**
 * Created by WangYiWen on 2018/5/6.
 */
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hyjy.music.bean.MusicBean;
import com.hyjy.music.common.FileUtil;
import com.hyjy.music.common.SPUtil;
import com.hyjy.music.ui.PlayMusicActivity;

import java.lang.ref.SoftReference;
import java.util.Random;

public class MusicService extends Service {

    private static final String TAG = "MusicService";

    private MediaPlayer mediaPlayer;

    private boolean isFirstPlaying = true;

    private MyHandler mHandler;

    public static final String LAST_PLAY = "lastplay";

    public static class MyHandler extends Handler {
        private SoftReference<MusicService> sr;

        public MyHandler(MusicService mMusicService) {
            this.sr = new SoftReference<>(mMusicService);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (sr.get() != null && sr.get().mediaPlayer != null) {
                        int currentPosition = sr.get().mediaPlayer.getCurrentPosition();
                        //播放时TextView实时更新
                        onProgessChangedListener.onChange(currentPosition);
                        Log.i(TAG, "更新播放进度条...");
                        sendEmptyMessageDelayed(0, 100);
                    }
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder(); // 服务被绑定了
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        mHandler = new MyHandler(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.stop();
            mediaPlayer.release();// 释放播放器.
            SPUtil.putInt(this, LAST_PLAY, currentPosition);
            mediaPlayer = null;
        }
        Log.i(TAG, "取消延时消息,保存播放记录以便下次继续播放。");
        mHandler.removeCallbacksAndMessages(null);
        if (MyApplication.position != -1) {
            MusicBean musicBean = PlayMusicActivity.musicList.get(MyApplication.position);
            boolean b = FileUtil.saveObject(this, LAST_PLAY, musicBean);
            Log.i(TAG, b ? "保存播放记录成功" : "保存播放记录失败");
        }
        super.onDestroy();
    }

    // 播放的方法
    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        //每隔100毫秒跟新进度条
        mHandler.sendEmptyMessage(0);
    }

    // 暂停的方法
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.i(TAG, "取消延时消息");
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    // 停止的方法
    public void stop() {
        Log.i(TAG, "取消延时消息");
        mHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    //暴露播放,暂停等方法
    private class MyBinder extends Binder implements IService {

        @Override
        public MediaPlayer getMediaPlayer() {
            return mediaPlayer;
        }

        @Override
        public MyHandler getMyHandlerr() {
            return mHandler;
        }

        @Override
        public void setOnProgessChangedListener(OnProgessChangedListener listener) {
            onProgessChangedListener = listener;
        }

        @Override
        public void callplay(int playingPosition) {

            //停止播放后，mediaPlayer变为null了
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            final String path = PlayMusicActivity.musicList.get(playingPosition).getUrl();

            if (isFirstPlaying) {
                Log.i(TAG, "首次播放");
                isFirstPlaying = false;
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare(); //同步准备
                    //设置播放总时长
                    sendBroadcastToActivity(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(MusicService.this, "播放失败,错误代码:" + what, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                //播放完毕的监听,根据播放模式做相应处理
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        queryPlayMode();
                    }
                });
            }

            play();

            //歌词显示
            sendBroadcastToActivity(2);
        }

        @Override
        public void callpause() {
            pause();
        }

        @Override
        public void callstop() {
            stop();
            //这里赋值为true不能丢
            isFirstPlaying = true;
        }
    }

    /**
     * 播放模式
     */
    private void queryPlayMode() {
        int mode = SPUtil.getInt(this, "mode", 0);
        switch (mode % 3) {
            case 0:     //单曲循环
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                //单曲循环下不需要再显示歌词
                break;
            case 1:     //列表循环
                MyApplication.position++;
                if (MyApplication.position > PlayMusicActivity.musicList.size() - 1) {
                    MyApplication.position = 0;
                }

                String url = PlayMusicActivity.musicList.get(MyApplication.position).getUrl();

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();

                    play();

                    sendBroadcastToActivity(0);

                    sendBroadcastToActivity(1);

                    //歌词显示
                    sendBroadcastToActivity(2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:     //随机播放
                int size = PlayMusicActivity.musicList.size();
                //[0 - n)内生成随机数.
                //注意:包含0不包含n
                int random = new Random().nextInt(size);
                String urlRD = PlayMusicActivity.musicList.get(random).getUrl();

                //同步位置
                MyApplication.position = random;

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(urlRD);
                    mediaPlayer.prepare();

                    play();

                    sendBroadcastToActivity(0);

                    sendBroadcastToActivity(1);

                    //歌词显示
                    sendBroadcastToActivity(2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    /**
     * 发送广播,通知Activity更新UI
     *
     * @param type 标示类型,接收端用以区分不同的动作
     */
    private void sendBroadcastToActivity(int type) {
        Intent intent = new Intent(MyApplication.ACTION);
        intent.putExtra("type", type);
        sendBroadcast(intent);
    }

    private static OnProgessChangedListener onProgessChangedListener;

    public interface OnProgessChangedListener {
        void onChange(int progess);
    }
}