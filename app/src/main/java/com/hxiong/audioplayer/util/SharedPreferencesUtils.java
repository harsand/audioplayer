package com.hxiong.audioplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
/**
 * Created by hxiong on 2017/5/9 22:27.
 * Email 2509477698@qq.com
 */

public class SharedPreferencesUtils {

      public static final String AUDIO_SHARE_NAME="audio_share_name";

      public static final String AUDIO_PLAYER_ITEM="audio_player_item";

      public static final String AUDIO_CONTROL_FLAG="audio_control_flag";

      public static final String AUDIO_ORDER_FLAG="audio_order_flag";

      public static final String AUDIO_EXIT_FLAG="audio_exit_flag";

      private SharedPreferences mSharedPreferences;

       private static SharedPreferencesUtils mSharedPreferencesUtils;
       private SharedPreferencesUtils(){ }

       public synchronized static SharedPreferencesUtils get(){
           if(mSharedPreferencesUtils==null){
               mSharedPreferencesUtils=new SharedPreferencesUtils();
           }
           return mSharedPreferencesUtils;
       }

    /**
     * must call before call other functions
     * @param context
     */
    public void init(Context context){
        if(mSharedPreferences==null) {
            mSharedPreferences = context.getSharedPreferences(AUDIO_SHARE_NAME, Context.MODE_PRIVATE);
        }
    }


    public int getLastPlayItem(){
        return mSharedPreferences==null?-1:mSharedPreferences.getInt(AUDIO_PLAYER_ITEM,0);
    }

    public boolean setLastPlayItem(int id){
        if(mSharedPreferences!=null){
           Editor editor=mSharedPreferences.edit();
            editor.putInt(AUDIO_PLAYER_ITEM,id);
            return editor.commit();
        }
        return false;
    }

    public boolean isShowInfo(){
        return mSharedPreferences==null?false:mSharedPreferences.getBoolean(AUDIO_CONTROL_FLAG,false);
    }

    public boolean setShowInfo(boolean isShow){
        if(mSharedPreferences!=null){
            Editor editor=mSharedPreferences.edit();
            editor.putBoolean(AUDIO_CONTROL_FLAG,isShow);
            return editor.commit();
        }
        return false;
    }

    public int getPlayOrder(){
        return mSharedPreferences==null?1:mSharedPreferences.getInt(AUDIO_ORDER_FLAG,1);
    }

    public boolean setPlayOrder(int order){
        if(mSharedPreferences!=null){
            Editor editor=mSharedPreferences.edit();
            editor.putInt(AUDIO_ORDER_FLAG,order);
            return editor.commit();
        }
        return false;
    }

    public boolean setExitFlag(boolean isExit){
        if(mSharedPreferences!=null){
            Editor editor=mSharedPreferences.edit();
            editor.putBoolean(AUDIO_EXIT_FLAG,isExit);
            return editor.commit();
        }
        return false;
    }

    public boolean getExitFlag(){
        return mSharedPreferences==null?false:mSharedPreferences.getBoolean(AUDIO_EXIT_FLAG,false);
    }
}
