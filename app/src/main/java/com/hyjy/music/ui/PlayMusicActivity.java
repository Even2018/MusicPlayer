package com.hyjy.music.ui;
/**
 * Created by WangYiWen on 2018/5/3.
 */
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyjy.music.IService;
import com.hyjy.music.MusicService;
import com.hyjy.music.MyApplication;
import com.hyjy.music.R;
import com.hyjy.music.bean.MusicBean;
import com.hyjy.music.common.FileUtil;
import com.hyjy.music.common.MusicUtil;
import com.hyjy.music.common.SPUtil;
import com.hyjy.music.view.DotIndicator;

import java.util.ArrayList;
import java.util.List;

public class PlayMusicActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PlayMusicActivity";

    public static List<MusicBean> musicList;

    MusicUtil musicUtil;
    IService myBinder;
    ContentResolver contentResolver;
    MyBroadcastReceiver receiver;
    MyObserver observer;
    Myconn myconn;

    //Fragment相关
    List<BaseFragment> fragments;
    MusicListFragment musicListFragment;
    PhotoFragment photoFragment;
    LrcFragment lrcFragment;

    //歌名
    TextView tv_title;

    //歌手
    TextView tv_siger;

    //播放模式的图标
    ImageView iv_mode;

    //歌曲总时长
    TextView tv_total;

    //当前播放的进度
    TextView tv_current;

    //播放的进度条
    SeekBar seekBar;

    DotIndicator vp_indicator;
    ViewPager vp;
    ImageButton bt_play;
    ImageButton bt_next;
    ImageButton bt_pre;

    //动画
    Animation scAnimation;
    Animation playAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        init();

        registerAndBind();

        addFragmentToViewPager();

        setListener();

        showLast();
    }

    /**
     * 初始化
     */
    private void init() {
        musicUtil = new MusicUtil();
        iv_mode = (ImageView) findViewById(R.id.iv_mode);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_siger = (TextView) findViewById(R.id.tv_siger);
        vp = (ViewPager) findViewById(R.id.vp);
        vp_indicator = (DotIndicator) findViewById(R.id.vp_indicator);

        tv_current = (TextView) findViewById(R.id.tv_current);
        tv_total = (TextView) findViewById(R.id.tv_total);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        bt_play = (ImageButton) findViewById(R.id.bt_playOrpause);
        bt_next = (ImageButton) findViewById(R.id.bt_next);
        bt_pre = (ImageButton) findViewById(R.id.bt_pre);

        //点击按钮的动画
        scAnimation = AnimationUtils.loadAnimation(PlayMusicActivity.this, R.anim.sc);
        //播放时的动画
        playAnimation = AnimationUtils.loadAnimation(PlayMusicActivity.this, R.anim.play);

        //接收欢迎界面传过来的music实体类
        musicList = getIntent().getParcelableArrayListExtra("musicBeans");
    }

    //绑定服务,注册观察者,广播
    private void registerAndBind() {
        //绑定音乐服务
        Intent intent = new Intent(this, MusicService.class);
        myconn = new Myconn();
        bindService(intent, myconn, BIND_AUTO_CREATE);

        //注册广播接收者
        receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyApplication.ACTION);
        registerReceiver(receiver, filter);

        //注册内容观察者,当媒体数据库发生变化时,更新音乐列表
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        observer = new MyObserver(new Handler());
        contentResolver = getContentResolver();
        contentResolver.registerContentObserver(uri, true, observer);
    }

    private void addFragmentToViewPager() {
        fragments = new ArrayList<>();
        fragments.add(MusicListFragment.newInstance());
        fragments.add(PhotoFragment.newInstance());
        fragments.add(LrcFragment.newInstance());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        vp.setAdapter(adapter);
        //绑定ViewPager
        vp_indicator.setViewPager(vp, DotIndicator.Mode.middle);
        //设置页面缓存数
        vp.setOffscreenPageLimit(fragments.size() - 1);
        //得到三个Fragment对象
        musicListFragment = (MusicListFragment) adapter.getItem(0);
        photoFragment = (PhotoFragment) adapter.getItem(1);
        lrcFragment = (LrcFragment) adapter.getItem(2);
    }

    //回显状态
    private void showLast() {
        //回显播放模式的图标
        int mode = SPUtil.getInt(this, "mode", 0);
        switch (mode % 3) {
            case 0:     //单曲循环
                iv_mode.setBackgroundResource(R.drawable.danqu);
                break;

            case 1:     //列表循环
                iv_mode.setBackgroundResource(R.drawable.liebiao);
                break;

            case 2:     //随机播放
                iv_mode.setBackgroundResource(R.drawable.suiji);
                break;
        }
    }


    private int findLastPlayPosition(MusicBean musicBean) {
        int position = -1;
        for (int i = 0; i < musicList.size(); i++) {
            if (musicBean.equals(musicList.get(i))) {
                position = i;
                break;
            }
        }
        return position;
    }

    //设置监听
    private void setListener() {
        bt_play.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        bt_pre.setOnClickListener(this);
        //每点击一次,mode自增1,对3取余来判断是哪种播放模式
        iv_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mode = SPUtil.getInt(PlayMusicActivity.this, "mode", 0);
                mode++;
                switch (mode % 3) {
                    case 0:     //单曲循环
                        Toast.makeText(PlayMusicActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        iv_mode.setBackgroundResource(R.drawable.danqu);
                        break;

                    case 1:     //列表循环
                        Toast.makeText(PlayMusicActivity.this, "列表循环", Toast.LENGTH_SHORT).show();
                        iv_mode.setBackgroundResource(R.drawable.liebiao);
                        break;

                    case 2:     //随机播放
                        Toast.makeText(PlayMusicActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
                        iv_mode.setBackgroundResource(R.drawable.suiji);
                        break;
                }
                SPUtil.putInt(PlayMusicActivity.this, "mode", mode);
            }
        });

        //快进快退
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // fromUser判断是用户改变的滑块的值
                if (fromUser) {
                    this.progress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { //开始滑动的回调
                MediaPlayer mediaPlayer = myBinder.getMediaPlayer();
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    MusicService.MyHandler myHandler = myBinder.getMyHandlerr();
                    myHandler.removeCallbacksAndMessages(null);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {  //停止滑动的回调
                if (MyApplication.position == -1) {
                    return;
                }
                MediaPlayer mediaPlayer = myBinder.getMediaPlayer();
                if (mediaPlayer == null) {
                    return;
                }
                mediaPlayer.seekTo(progress);
                if (mediaPlayer.isPlaying()) {
                    MusicService.MyHandler myHandler = myBinder.getMyHandlerr();
                    myHandler.sendEmptyMessage(0);
                    //继续滚动歌词
                    String url = musicList.get(MyApplication.position).getUrl();
                    lrcFragment.showLrc(url, false);
                }
            }
        });
    }

    /**
     * 回调方法中的参数service就是服务那边onBind()方法的返回值
     */
    private class Myconn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "绑定成功");
            myBinder = (IService) service;
            musicListFragment.myBinder = (IService) service;
            photoFragment.myBinder = (IService) service;
            lrcFragment.myBinder = (IService) service;

            myBinder.setOnProgessChangedListener(new MusicService.OnProgessChangedListener() {
                @Override
                public void onChange(int progess) {
                    seekBar.setProgress(progess);
                    int duration = myBinder.getMediaPlayer().getDuration();
                    if (progess > 0 && progess <= duration) {
                        //防止播放显示的时间大于总时间
                        tv_current.setText(toTime(progess));
                    }
                }
            });

            showMyDialog("温馨提示", "是否根据上次的记录继续播放?", "是", "否", 1);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "失去连接了");
        }
    }

    @Override
    public void onClick(View v) {

        //点击图标播放动画
        v.startAnimation(scAnimation);

        if (MyApplication.position == -1) {
            Toast.makeText(PlayMusicActivity.this, "请选择一首歌", Toast.LENGTH_SHORT).show();
            return;
        }

        if (musicList.size() == 0) {   //防止歌曲删完后点击图标角标越界
            return;
        }

        boolean playing = myBinder.getMediaPlayer().isPlaying();

        switch (v.getId()) {
            case R.id.bt_playOrpause: // 播放或者暂停
                if (playing) {
                    myBinder.callpause();
                } else {
                    myBinder.callplay(MyApplication.position);
                }
                break;

            case R.id.bt_pre: // 上一曲
                //先停止当前播放的
                myBinder.callstop();

                //再播放上一首
                MyApplication.position--;
                if (MyApplication.position < 0) {
                    MyApplication.position = musicList.size() - 1;
                }

                musicListFragment.setListViewPosition();

                myBinder.callplay(MyApplication.position);
                break;

            case R.id.bt_next: // 下一曲
                //先停止当前播放的
                myBinder.callstop();

                //再播放下一首
                MyApplication.position++;
                if (MyApplication.position > musicList.size() - 1) {
                    MyApplication.position = 0;
                }

                musicListFragment.setListViewPosition();

                myBinder.callplay(MyApplication.position);
                break;
        }
        updateUI();
    }

    @Override
    public void onBackPressed() {
        showMyDialog("温馨提示", "是否在后台继续播放音乐?", "继续播放", "停止播放", 2);
    }

    private void showMyDialog(String title, String message, String positiveText, String negativeText, final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(type == 2);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (type == 2) {
                    moveTaskToBack(true);
                } else if (type == 1) {
                    //根据保存的播放进度继续播放
                    MusicBean musicBean = FileUtil.getObject(PlayMusicActivity.this, MusicService.LAST_PLAY);
                    if (musicBean == null) {
                        Toast.makeText(PlayMusicActivity.this, "暂无播放记录", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int position = findLastPlayPosition(musicBean);
                    if (position == -1) {
                        Toast.makeText(PlayMusicActivity.this, "暂无播放记录", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i(TAG, "根据上次播放的进度继续播放");
                    MyApplication.position = position;
                    myBinder.callplay(position);
                    MediaPlayer mediaPlayer = myBinder.getMediaPlayer();
                    int i = SPUtil.getInt(PlayMusicActivity.this, MusicService.LAST_PLAY, -1);
                    mediaPlayer.seekTo(i);
                    updateUI();
                    musicListFragment.setListViewPosition();
                }
            }
        });
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (type == 2) {
                    finish();
                } else if (type == 1) {
                    Log.i(TAG, "播放第0首歌曲");
                    MyApplication.position = 0;
                    myBinder.callplay(0);
                    updateUI();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {

        //注销内容观察者
        contentResolver.unregisterContentObserver(observer);

        //注销广播接收者
        unregisterReceiver(receiver);

        unbindService(myconn);

        super.onDestroy();
    }

    /**
     * 时间格式转换
     */
    public static String toTime(int time) {
        time /= 1000;
        int minute = time / 60;
//        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    /**
     * 内容观察者,观察媒体数据库的变化,实时更新音乐列表
     */
    class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Toast.makeText(PlayMusicActivity.this, "刷新音乐列表", Toast.LENGTH_SHORT).show();
            new MyAsynctask().execute();
        }
    }

    /**
     * 异步任务类
     */
    public class MyAsynctask extends AsyncTask<Void, Void, List<MusicBean>> {

        @Override
        protected List<MusicBean> doInBackground(Void... params) {
            return musicUtil.getMusicData(getApplicationContext());
        }

        @Override
        protected void onPostExecute(List<MusicBean> musicBeans) {
            super.onPostExecute(musicBeans);
            MusicBean currentMusicBean = musicList.get(MyApplication.position);
            musicList = musicBeans;
            String text = "音乐列表(" + musicBeans.size() + "首)";
            musicListFragment.tvCount.setText(text);

            //防止新增歌曲后显示不正确
            MyApplication.position = findLastPlayPosition(currentMusicBean);
            musicListFragment.adapter.refreshData(musicBeans);
            musicListFragment.setListViewPosition();
        }
    }

    /**
     * 根据音乐的播放状态,更新UI
     */
    public void updateUI() {
        MusicBean bean = musicList.get(MyApplication.position);
        tv_title.setText(bean.getTitle());
        tv_siger.setText(bean.getSinger());
        boolean playing = myBinder.getMediaPlayer().isPlaying();
        if (playing) {
            bt_play.setBackgroundResource(R.drawable.pause);
            //设置封面的图片
            Bitmap bitmap = musicUtil.getAlbumBitmap(PlayMusicActivity.this, bean.getUrl(), R.drawable.baby);
            photoFragment.setPlayStatus(true, bitmap);
        } else {
            photoFragment.setPlayStatus(false, null);
            bt_play.setBackgroundResource(R.drawable.play);
        }
        musicListFragment.adapter.notifyDataSetChanged();
    }

    /**
     * 接收Service里发来的广播,更新UI
     */
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            Log.i(TAG, "自定义广播");
            Log.i(TAG, "type:" + type);
            switch (type) {
                case 0:     //播放完毕UI更新
                    MusicBean bean = musicList.get(MyApplication.position);
                    String url = bean.getUrl();
                    Bitmap bitmap = musicUtil.getAlbumBitmap(PlayMusicActivity.this, url, R.drawable.baby);
                    tv_title.setText(bean.getTitle());
                    tv_siger.setText(bean.getSinger());
                    //刷新适配器,显示ListView中的动画
                    musicListFragment.adapter.notifyDataSetChanged();
                    MediaPlayer mediaPlayer = myBinder.getMediaPlayer();
                    tv_total.setText(toTime(mediaPlayer.getDuration()));
                    photoFragment.setPlayStatus(true, bitmap);

                    //设置lv被选中的位置
                    musicListFragment.setListViewPosition();
                    break;

                case 1:     //设置总时长
                    MediaPlayer player = myBinder.getMediaPlayer();
                    int duration = player.getDuration();
                    seekBar.setMax(duration);
                    tv_total.setText(toTime(duration));
                    break;

                case 2:     //显示歌词
                    String path = musicList.get(MyApplication.position).getUrl();
                    Log.i(TAG, path);
                    lrcFragment.showLrc(path, true);
                    break;
                default:
                    break;
            }
        }
    }
}