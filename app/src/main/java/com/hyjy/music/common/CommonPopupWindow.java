package com.hyjy.music.common;
/**
 * Created by WangYiWen on 2018/5/1.
 */
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.hyjy.music.R;

import java.util.List;

/**
 * 弹出PopupWindow
 */
public abstract class CommonPopupWindow<T> extends PopupWindow {

    private List<T> mList;

    public CommonPopupWindow(Context context, List<T> list) {
        super(context);
        this.mList = list;
        View view = View.inflate(context, R.layout.popuwindow, null);
        ListView listView = (ListView) view.findViewById(R.id.lv);
        listView.setDividerHeight(0);
        listView.setAdapter(new CommonAdapter<T>(context, mList, R.layout.popwindow_item) {
            @Override
            public void convert(ViewHolder holder, T item, int position) {
                if (item instanceof String) {
                    String s = (String) item;
                    holder.setTextView(R.id.tv, s);
                }
                if (position == mList.size() - 1) {
                    holder.getItemView(R.id.line).setVisibility(View.GONE);
                } else {
                    holder.getItemView(R.id.line).setVisibility(View.VISIBLE);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClick(mList.get(position));
                dismiss();
            }
        });

        //设置PopupWindow的View
        setContentView(view);
        //设置PopupWindow的宽高
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);

        //这里如果setFocusable(false),部分手机无法响应ListView的点击事件
        setFocusable(true);

        //设置弹出窗体动画效果
        //setAnimationStyle(R.style.Animation_holiday);

        ColorDrawable colorDrawable = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(colorDrawable);
    }

    public abstract void onClick(T t);

//    //重写此方法解决Android7.0系统总是显示全屏的问题
//    @Override
//    public void showAsDropDown(View anchor) {
//        if (Build.VERSION.SDK_INT >= 24) {
//            Rect visibleFrame = new Rect();
//            anchor.getGlobalVisibleRect(visibleFrame);
//            int height = anchor.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
//            setHeight(height);
//        }
//        super.showAsDropDown(anchor);
//    }
}