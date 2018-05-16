package com.hyjy.music.common;
/**
 * Created by WangYiWen on 2018/4/30.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 通用ViewHolder
 */
public class ViewHolder {

    private SparseArray<View> mViews;   // SparseArray效率比HashMap更高
    private View mConvertView;

    private ViewHolder(View convertView) {
        this.mViews = new SparseArray<View>();
        this.mConvertView = convertView;
        mConvertView.setTag(this);
    }

    /**
     * 获取ViewHolder对象
     */
    public static ViewHolder newInstance(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
            return new ViewHolder(convertView);
        }
        return (ViewHolder) convertView.getTag();
    }

    /**
     * 通过控件的Id获取对应的控件，如果没有则加入views
     */
    public <T extends View> T getItemView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 给TextView赋值,返回ViewHolder方便链式调用
     */
    public ViewHolder setTextView(int id, String text) {
        TextView textView = getItemView(id);
        textView.setText(text);
        return this;
    }

    /**
     * 给ImageView赋值
     */
    public ViewHolder setImageView(int id, Bitmap bitmap) {
        ImageView imageView = getItemView(id);
        imageView.setImageBitmap(bitmap);
        return this;
    }

}