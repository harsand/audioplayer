package com.hxiong.audioplayer.util;

/**
 * Created by hxiong on 2017/5/15 23:58.
 * Email 2509477698@qq.com
 */

public class LyricsList {

    //base
    private int[] keys;
    private String[] values;
    private int size;

    //

    public LyricsList(){
        this(10);
    }

    public LyricsList(int length){
        keys=new int[length];
        values=new String[length];
        size=0;
    }

    public void put(int key,String value){

    }

    public int size(){
        return size();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
