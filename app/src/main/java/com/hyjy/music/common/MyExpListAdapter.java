package com.hyjy.music.common;
/**
 * Created by WangYiWen on 2018/5/6.
 */
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.List;

/**
 * ExpandableListView通用适配器
 */
public abstract class MyExpListAdapter<G, C> extends BaseExpandableListAdapter {
    //上下文
    protected Context mContext;

    //分组条目上的数据
    protected List<G> groupArray;

    //分组下孩子的数据
    protected List<List<C>> childArray;

    //分组上的布局
    protected int groupLayout;

    //子条目的布局
    protected int childLayout;

    public MyExpListAdapter(Context context, int groupLayout, int childLayout, List<G> groupArray, List<List<C>> childArray) {
        this.mContext = context;
        this.groupArray = groupArray;
        this.childArray = childArray;
        this.groupLayout = groupLayout;
        this.childLayout = childLayout;
    }

    public abstract void convertGroup(ViewHolder holder, G g, int groupPosition, boolean isExpanded);

    public abstract void convertChild(ViewHolder holder, C c, int groupPosition, int childPosition, boolean isLastChild);

    @Override
    public int getGroupCount() {
        return groupArray == null ? 0 : groupArray.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (childArray == null || childArray.size() <= groupPosition || childArray.get(groupPosition) == null) ?
                0 : childArray.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupArray == null ? null : groupArray.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return (childArray == null || childArray.size() <= groupPosition || childArray.get(groupPosition) == null) ?
                null : childArray.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.newInstance(mContext, convertView, parent, groupLayout, groupPosition);
        convertGroup(holder, groupArray.get(groupPosition), groupPosition, isExpanded);
        return holder.getConvertView();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.newInstance(mContext, convertView, parent, childLayout, groupPosition);
        convertChild(holder, childArray.get(groupPosition).get(childPosition), groupPosition, childPosition, isLastChild);
        return holder.getConvertView();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}