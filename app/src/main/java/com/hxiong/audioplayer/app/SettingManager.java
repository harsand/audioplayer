package com.hxiong.audioplayer.app;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.hxiong.audioplayer.R;
import com.hxiong.audioplayer.util.SharedPreferencesUtils;

/**
 * Created by hxiong on 2017/8/7 21:54.
 * Email 2509477698@qq.com
 */

public class SettingManager implements CompoundButton.OnCheckedChangeListener{

    private Context mContext;
    private LinearLayout mSettingLayout;

    private Switch mExitSwitch;

    public SettingManager(Context context, LinearLayout linearLayout){
         this.mContext=context;
         this.mSettingLayout=linearLayout;
         init();
    }

    private void init(){
        mExitSwitch=(Switch)mSettingLayout.findViewById(R.id.switch_exit_btn);
        mExitSwitch.setChecked(SharedPreferencesUtils.get().getExitFlag());
        mExitSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.switch_exit_btn:
                handleExitSwitch(isChecked);
                break;
            default: break;
        }
    }

    private void handleExitSwitch(boolean isChecked){
        SharedPreferencesUtils.get().setExitFlag(isChecked);
    }
}
