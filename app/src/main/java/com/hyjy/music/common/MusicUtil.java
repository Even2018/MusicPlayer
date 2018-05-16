package com.hyjy.music.common;
/**
 * Created by WangYiWen on 2018/4/26.
 */
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import com.hyjy.music.bean.MusicBean;

import java.util.ArrayList;


public class MusicUtil {

    private ArrayList<MusicBean> temList;


    public ArrayList<MusicBean> getMusicData(Context context) {
        ContentResolver cr = context.getContentResolver();
        temList = new ArrayList<>();

        if (cr != null) {
            //获取所有歌曲
            Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (null == cursor) {
                return null;
            }

            if (cursor.moveToFirst()) {
                MusicBean bean;
                do {
                    long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    if (time < 60 * 1000) continue;     //过滤时长小于60秒的

                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if ("<unknown>".equals(singer)) {
                        singer = "未知艺术家";
                    }
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    if (isRepeat(title, singer, album)) continue;    //过滤重复歌曲

                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    Long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                    bean = new MusicBean();
                    bean.setTitle(title);
                    bean.setSinger(singer);
                    bean.setAlbum(album);
                    bean.setSize(size);
                    bean.setTime(time);
                    bean.setUrl(url);
                    bean.setName(name);
                    bean.id = id;
                    bean.albumId = albumId;
                    temList.add(bean);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return temList;
    }


    private boolean isRepeat(String title, String artist, String album) {
        for (MusicBean music : temList) {
            if (title.equals(music.getTitle()) && artist.equals(music.getSinger()) && album.equals(music.getAlbum())) {
                return true;
            }
        }
        return false;
    }



    public Bitmap getAlbumBitmap(Context context, String url, int defaultRes) {
        Bitmap bitmap;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(url); //设置数据源
        byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
        if (embedPic == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), defaultRes);
        } else {
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
        }
        retriever.release();
        return bitmap;
    }
}