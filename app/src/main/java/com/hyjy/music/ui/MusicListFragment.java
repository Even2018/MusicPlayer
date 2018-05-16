package com.hyjy.music.ui;
/**
 * Created by WangYiWen on 2018/5/1.
 */
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hyjy.music.MyApplication;
import com.hyjy.music.R;
import com.hyjy.music.bean.MusicBean;
import com.hyjy.music.common.CommonAdapter;
import com.hyjy.music.common.ViewHolder;

/**
 * 音乐列表Fragment
 */
public class MusicListFragment extends BaseFragment {

    private ListView lv;
    public CommonAdapter<MusicBean> adapter;
    public TextView tvCount;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(playMusicActivity, resId, null);
        tvCount = (TextView) view.findViewById(R.id.tv);
        String number = "音乐列表(" + PlayMusicActivity.musicList.size() + "首)";
        tvCount.setText(number);
        lv = (ListView) view.findViewById(R.id.lv);
        return view;
    }

    @Override
    protected void initData() {
        adapter = new CommonAdapter<MusicBean>(playMusicActivity, PlayMusicActivity.musicList, R.layout.item) {
            @Override
            public void convert(ViewHolder holder, MusicBean item, int position) {
                holder.setTextView(R.id.music_item_name, item.getTitle())
                        .setTextView(R.id.music_item_singer, item.getSinger())
                        .setTextView(R.id.music_item_time, PlayMusicActivity.toTime((int) item.getTime()));

                //给正在播放的歌曲设置一个动画
                ImageView imageView = holder.getItemView(R.id.iv_playing);
                AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
                if (position == MyApplication.position) {
                    imageView.setVisibility(View.VISIBLE);
                    if (myBinder != null) {
                        //防止空指针异常,onServiceConnected回调方法是异步的.
                        //绑定服务了myBinder可能还未被赋值了
                        MediaPlayer mediaPlayer = myBinder.getMediaPlayer();
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                animationDrawable.start();
                            } else {
                                animationDrawable.stop();
                            }
                        }
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        };
        lv.setAdapter(adapter);

        //设置listview条目的点击监听
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaPlayer mediaPlayer = myBinder.getMediaPlayer();
                if (mediaPlayer != null) {      //防止按停止后,再按条目报空指针
                    boolean playing = mediaPlayer.isPlaying();
                    if (position == MyApplication.position) {   //点击的条目就是当前播放的
                        if (!playing) {     //没有播放
                            mediaPlayer.start();    //没必要调callplay()方法,只需start()
                            playMusicActivity.bt_play.setBackgroundResource(R.drawable.pause);
                            MusicBean bean = PlayMusicActivity.musicList.get(position);
                            Bitmap bitmap = musicUtil.getAlbumBitmap(playMusicActivity, bean.getUrl(), R.drawable.baby);
                            playMusicActivity.photoFragment.setPlayStatus(true, bitmap);

                            ImageView imageView = (ImageView) view.findViewById(R.id.iv_playing);
                            AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
                            animationDrawable.start();

                            myBinder.getMyHandlerr().sendEmptyMessage(0);
                            String path = PlayMusicActivity.musicList.get(MyApplication.position).getUrl();
                            playMusicActivity.lrcFragment.showLrc(path, true);
                        }
                    } else {    //点击的条目不是当前播放的
                        //记录播放位置
                        MyApplication.position = position;
                        MusicBean bean = PlayMusicActivity.musicList.get(position);
                        String title = bean.getTitle();
                        String siger = bean.getSinger();

                        //先停止再播放,调整状态
                        myBinder.callstop();
                        myBinder.callplay(position);
                        playMusicActivity.bt_play.setBackgroundResource(R.drawable.pause);
                        playMusicActivity.lrcFragment.lrcView.setVisibility(View.VISIBLE);

                        playMusicActivity.photoFragment.setShow(true);

                        playMusicActivity.tv_title.setText(title);
                        playMusicActivity.tv_siger.setText(siger);
                        //设置封面的图片
                        Bitmap bitmap = musicUtil.getAlbumBitmap(playMusicActivity, bean.getUrl(), R.drawable.baby);
                        playMusicActivity.photoFragment.setPlayStatus(true, bitmap);

                        //刷新适配器,在ListView中显示正在播放的歌曲
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    //记录播放位置
                    MyApplication.position = position;
                    MusicBean bean = PlayMusicActivity.musicList.get(position);
                    String title = bean.getTitle();
                    String siger = bean.getSinger();

                    //直接播放(不用停止)
                    myBinder.callplay(position);
                    playMusicActivity.bt_play.setBackgroundResource(R.drawable.pause);
                    playMusicActivity.lrcFragment.lrcView.setVisibility(View.VISIBLE);
                    playMusicActivity.photoFragment.setShow(true);
                    playMusicActivity.tv_title.setText(title);
                    playMusicActivity.tv_siger.setText(siger);
                    //设置封面的图片
                    Bitmap bitmap = musicUtil.getAlbumBitmap(playMusicActivity, bean.getUrl(), R.drawable.baby);
                    playMusicActivity.photoFragment.setPlayStatus(true, bitmap);

                    //刷新适配器,在ListView中显示正在播放的歌曲
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    //设置ListView被选中的位置
    public void setListViewPosition() {
        //直接设置无效,用post方式
        lv.post(new Runnable() {
            @Override
            public void run() {
                int lastVisiblePosition = lv.getLastVisiblePosition();
                int firstVisiblePosition = lv.getFirstVisiblePosition();
                if (lastVisiblePosition < MyApplication.position || firstVisiblePosition > MyApplication.position) {
                    lv.setSelection(MyApplication.position);
                }
            }
        });
    }

    public static MusicListFragment newInstance() {
        MusicListFragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putInt(KEY, R.layout.musiclist);
        fragment.setArguments(args);
        return fragment;
    }
}