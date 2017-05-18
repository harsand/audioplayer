package com.hxiong.audioplayer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by hxiong on 2017/5/7 20:33.
 * Email 2509477698@qq.com
 */

public class BaseActivity extends Activity {

    private static final String TAG="AudioPlayerActivity";
    private static final boolean ENABLE_LOG=true;

    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);//以代码的形式去掉标题栏
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_enter, R.anim.left_exit);
    }

    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(R.anim.left_enter, R.anim.right_exit);
    }

    protected void printLog(String log){
        if(ENABLE_LOG&&log!=null) Log.d(TAG,log);
    }
}
