package com.hxiong.audioplayer.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by hxiong on 2017/5/7 21:49.
 * Email 2509477698@qq.com
 */

public class AudioPagerAdapter extends PagerAdapter {

    private ArrayList<View> mViews;

    public AudioPagerAdapter(){
            mViews=new ArrayList<View>();  // not allow null
    }

    /**
     *  add view to viewpager
     * @param view
     * @return
     */
    public boolean addView(View view){
        if(view==null){
            return false;
        }
        mViews.add(view);
        return true;
    }

    /**
     *  remove view from viewpager
     * @param view
     * @return
     */
    public boolean removeView(View view){
        return mViews.remove(view);
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //return super.instantiateItem(container, position);
        ((ViewPager)container).addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
        ((ViewPager)container).removeView(mViews.get(position));
    }
}
