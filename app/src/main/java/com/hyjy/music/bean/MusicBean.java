package com.hyjy.music.bean;
/**
 * Created by WangYiWen on 2018/5/6.
 */
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class MusicBean implements Parcelable, Serializable {

    private static final long serialVersionUID = 123L;

    private String name;
    private String title;
    private String singer;
    private String album;
    private String url;
    private long size;
    private long time;

    public long id;
    public long albumId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicBean musicBean = (MusicBean) o;
        return size == musicBean.size &&
                time == musicBean.time &&
                id == musicBean.id &&
                albumId == musicBean.albumId &&
                name.equals(musicBean.name) &&
                title.equals(musicBean.title) &&
                singer.equals(musicBean.singer) &&
                album.equals(musicBean.album) &&
                url.equals(musicBean.url);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + singer.hashCode();
        result = 31 * result + album.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (albumId ^ (albumId >>> 32));
        return result;
    }

    public MusicBean() {

    }

    protected MusicBean(Parcel in) {
        name = in.readString();
        title = in.readString();
        singer = in.readString();
        album = in.readString();
        url = in.readString();
        size = in.readLong();
        time = in.readLong();
        id = in.readLong();
        albumId = in.readLong();
    }

    public static final Creator<MusicBean> CREATOR = new Creator<MusicBean>() {
        @Override
        public MusicBean createFromParcel(Parcel in) {
            return new MusicBean(in);
        }

        @Override
        public MusicBean[] newArray(int size) {
            return new MusicBean[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(title);
        dest.writeString(singer);
        dest.writeString(album);
        dest.writeString(url);
        dest.writeLong(size);
        dest.writeLong(time);
        dest.writeLong(id);
        dest.writeLong(albumId);
    }
}