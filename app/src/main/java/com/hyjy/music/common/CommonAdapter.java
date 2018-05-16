package com.hyjy.music.common;
/**
 * Created by WangYiWen on 2018/4/26.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * ListView通用的适配器
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;
    protected int mItemLayoutRes;

    public CommonAdapter(Context context, List<T> mDatas, int itemLayoutRes) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mDatas = mDatas;
        this.mItemLayoutRes = itemLayoutRes;
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas == null ? null : mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.newInstance(mContext, convertView, parent, mItemLayoutRes, position);
        convert(holder, getItem(position),position);    //传position过去防止位置错乱
        return holder.getConvertView();
    }

    public abstract void convert(ViewHolder holder, T item, int position);


    public void refreshData(List<T> list) {
        if (list == null) {
            this.mDatas.clear();
        } else {
            this.mDatas = list;
        }
        notifyDataSetChanged();
    }
}