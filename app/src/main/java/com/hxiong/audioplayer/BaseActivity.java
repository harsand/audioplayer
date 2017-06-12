package com.hxiong.audioplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by hxiong on 2017/5/7 20:33.
 * Email 2509477698@qq.com
 */

public class BaseActivity extends Activity {

    private static final String TAG="AudioPlayerActivity";
    private static final boolean ENABLE_LOG=true;
    protected static final int PERMISSION_REQUEST_CODE = 19;
    protected static final int ACTIVITY_REQUEST_CODE = 20;

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

    protected void checkAndRequestPermission(String permission){
        checkAndRequestPermission(permission,PERMISSION_REQUEST_CODE);
    }

    protected void checkAndRequestPermission(String permission,int requestCode){
        printLog("checkAndRequestPermission.");
        if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
                printLog("shouldShowRequestPermissionRationale.");
                onShowPermissionRationale(permission,requestCode);
            }else {
                onRequestPermissions(permission,requestCode);
            }
        }
    }

    protected void onRequestPermissions(String permission,int requestCode){
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          onPermissionsResult(requestCode,permissions, grantResults);
    }

    protected void onShowPermissionRationale(final String permission,final int requestCode){
        new AlertDialog.Builder(this)
                .setTitle("提示信息")
                .setIcon(R.mipmap.dialog_icon)
                .setMessage("此应用需要如下权限，请允许。\n"+permission)
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onRequestPermissions(permission,requestCode);
                    }
                }).show();
    }

    protected void onPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults){
         printLog("onPermissionsResult call.Build.VERSION.SDK_INT is:"+ Build.VERSION.SDK_INT);
         for(int i=0;i<permissions.length;i++){
             printLog("permission ("+permissions[i]+") grantResult is"+grantResults[i]);

         }
    }

    protected void printLog(String log){
        if(ENABLE_LOG&&log!=null) Log.d(TAG,log);
    }
}
