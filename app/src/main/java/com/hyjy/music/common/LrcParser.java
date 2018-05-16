package com.hyjy.music.common;
/**
 * Created by WangYiWen on 2018/4/16.
 */
import com.hyjy.music.bean.Lrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 歌词解析的工具类
 */
public class LrcParser {

    private static final String TAG = "LrcParser";


    public static ArrayList<Lrc> getLrcList(String path) {
        File file;
        if (path.contains(".mp3")) {
            file = new File(path.replace(".mp3", ".lrc"));
            if (!file.exists()) {
                file = new File(path.replace(".mp3", ".txt"));
                if (!file.exists()) {
                    return null;
                }
            }
        } else if (path.contains(".flac")) {
            file = new File(path.replace(".flac", ".lrc"));
            if (!file.exists()) {
                file = new File(path.replace(".flac", ".txt"));
                if (!file.exists()) {
                    return null;
                }
            }
        } else {
            return null;
        }

        InputStream inputStream = null;
        ArrayList<Lrc> list = new ArrayList<>();
        try {
            inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            Lrc lrc;
            String line;
            //1.读取每一行歌词
            while ((line = reader.readLine()) != null) {
                //2.将歌词解析成bean,放入集合
                //  [03:32.43][02:02.72][00:00.31]月半小夜曲
                String[] arr = line.split("\\]");//转义字符
                //  [03:32.43   [02:02.72   [00:00.31   月半小夜曲
                for (int i = 0; i < arr.length - 1; i++) {
                    //最后一个元素是歌词,前面的是时间,注意循环的次数是arr.length - 1
                    lrc = new Lrc();
                    String content = arr[arr.length - 1];
                    if (content.contains("\uE3AC")) {
                        content = content.replace("\uE3AC", "");
                    }
                    lrc.content = content;
                    lrc.startTime = formatStartPoint(arr[i]);
                    list.add(lrc);
                }
            }
            //3.歌词排序
            Collections.sort(list);//从小到大
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }


    private static long formatStartPoint(String str) {
        str = str.replace("[", "");     //得到00:22.05
        String[] arr1 = str.split("\\:");//得到00   22.05
        String[] arr2 = arr1[1].split("\\.");//得到22    05
        int minute = Integer.parseInt(arr1[0]);//得到多分钟
        int second = Integer.parseInt(arr2[0]);//得到多少秒
        int mills;
        if (arr2.length == 2) {
            mills = Integer.parseInt(arr2[1]);//得到多少10毫秒(是10毫米)
        } else {
            mills = 0;
        }
        return minute * 60 * 1000 + second * 1000 + mills * 10;
    }

}