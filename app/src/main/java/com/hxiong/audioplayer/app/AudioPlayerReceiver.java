package com.hxiong.audioplayer.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hxiong.audioplayer.AudioPlayerActivity;

/**
 * Created by hxiong on 2017/5/25 23:33.
 * Email 2509477698@qq.com
 */

public class AudioPlayerReceiver extends BroadcastReceiver {

    //注册成了静态广播
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AudioPlayerReceiver",intent.getAction());
        //收到广播后，把AudioPlayerActivity 运行起来
        try {
            Intent activity = new Intent(context, AudioPlayerActivity.class);
            activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
            context.startActivity(activity);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
