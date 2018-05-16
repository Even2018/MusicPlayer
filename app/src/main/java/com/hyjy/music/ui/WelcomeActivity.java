package com.hyjy.music.ui;
/**
 * Created by WangYiWen on 2018/5/4.
 */
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.hyjy.music.R;
import com.hyjy.music.bean.MusicBean;
import com.hyjy.music.common.MusicUtil;

import java.util.ArrayList;

/**
 * 扫描媒体库,把音乐集合传给下一个界面
 */
public class WelcomeActivity extends Activity {

    private static final int WRITE_PERMISSIONS = 123;

    private MusicUtil musicUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        musicUtil = new MusicUtil();

        Animation set = AnimationUtils.loadAnimation(this, R.anim.set);
        View view = findViewById(R.id.rl);
        view.startAnimation(set);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //如果是6.0版本以上,动态申请权限
                if (Build.VERSION.SDK_INT >= 23) {
                    int permission = ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSIONS);
                    } else {
                        //开启异步任务扫描歌曲
                        new MyAsynctask().execute();
                    }
                } else {
                    new MyAsynctask().execute();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    new MyAsynctask().execute();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    /**
     * 异步任务类,耗时操作不能放UI线程
     */
    public class MyAsynctask extends AsyncTask<Void, Void, ArrayList<MusicBean>> {

        @Override
        protected ArrayList<MusicBean> doInBackground(Void... params) {
            return musicUtil.getMusicData(getApplicationContext());
        }

        @Override
        protected void onPostExecute(ArrayList<MusicBean> musicBeans) {
            super.onPostExecute(musicBeans);
            Toast.makeText(WelcomeActivity.this, "找到" + musicBeans.size() + "首歌", Toast.LENGTH_SHORT).show();
            //将数据传给下一个界面
            Intent intent = new Intent(WelcomeActivity.this, PlayMusicActivity.class);
            intent.putParcelableArrayListExtra("musicBeans", musicBeans);
            startActivity(intent);
            finish();
        }
    }
}