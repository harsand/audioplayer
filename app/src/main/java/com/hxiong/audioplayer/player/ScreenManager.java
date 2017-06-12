package com.hxiong.audioplayer.player;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.hxiong.audioplayer.R;
import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.Error;

/**
 * Created by hxiong on 2017/5/19 23:14.
 * Email 2509477698@qq.com
 */

public class ScreenManager implements View.OnClickListener,View.OnTouchListener{

      protected final static String ACTION_CLICK_EVENT = "com.hxiong.intent.action.click";
      protected final static String INTENT_EXTRA_TAG = "click_event_id";
      public final static int INTENT_EXTRA_PLAY_ID = 1;
      public final static int INTENT_EXTRA_NEXT_ID = 2;
      public final static int INTENT_EXTRA_LYRIC_ID = 3;
      public final static int INTENT_EXTRA_CLICKED_ID = 4;
      public final static int INTENT_EXTRA_DELETED_ID = 5;
      public final static int INTENT_EXTRA_MSG_DELAY = 6;   //延时执行某个消息
      public final static int INTENT_EXTRA_MSG_REMOVE = 7;  //移除上面的消息

      protected static final int SCREEN_NOTIFICATION_ID = 100;
      protected static final int MSG_HIDDEN_DELAY = 2000;

      private NotificationManager mNotificationManager;  //
      private WindowManager mWindowManager; //
      private AppOpsManager mAppOpsManager; //权限管理相关
      private Notification mNotification;
      private WindowManager.LayoutParams mParams;
      private View mWindowLyric;
      private ImageView mLockView;
      private ImageView mCloseView;
      private TextView mLyricView;

      private Context mContext;
      private NotificationReceiver mNotificationReceiver;
      private ScreenListener mScreenListener;
      private boolean isDesktopLyric;   //下拉通知栏是否允许显示歌词
      private boolean isLyricVisible;   //应用是否允许显示歌词
      private boolean isShowLyric;  //是否真正显示歌词
      private int mLyricColor;
      private boolean isCanClear;   //作用标志，应用退出后，可以通过通知栏结束service
      private boolean isPlay;
      private boolean isMove;    //是否发生了移动
      private boolean isLyricLock; //是否允许移动歌词显示位置
      private int mPosY;  //按下时控件的起始y坐标
      private float mRawPosY;


      public ScreenManager(Context context){
          this.mContext=context;
          this.mNotificationManager=(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
          this.mWindowManager=(WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
          this.mAppOpsManager=(AppOpsManager)mContext.getSystemService(Context.APP_OPS_SERVICE);
          isDesktopLyric=false;
          isLyricVisible=false;
          isShowLyric=false;
          mLyricColor= Color.parseColor("#e36f79");
          isCanClear=false;
          isPlay=false;
      }

      public void init(){
          /**
           * 注册广播
           */
          mNotificationReceiver=new NotificationReceiver();
          IntentFilter intentFilter = new IntentFilter();
          intentFilter.addAction(ACTION_CLICK_EVENT);
          mContext.registerReceiver(mNotificationReceiver,intentFilter);

          /**
           * 通知栏 显示控制面板
           */
          Notification.Builder builder=new Notification.Builder(mContext);
          RemoteViews mRemoteViews= new RemoteViews(mContext.getPackageName(),R.layout.audio_notification_layout);
          mNotification = builder.getNotification();
          mNotification.contentView = mRemoteViews;
          mNotification.flags = Notification.FLAG_ONGOING_EVENT;
          mNotification.icon = R.mipmap.dialog_icon;// 设置下拉图标
          mNotification.deleteIntent=createEventIntent(INTENT_EXTRA_DELETED_ID,PendingIntent.FLAG_ONE_SHOT);

          /**
           * 按钮的监听
           */
          setClickListener(mRemoteViews,INTENT_EXTRA_PLAY_ID,R.id.audio_notify_control_play);
          setClickListener(mRemoteViews,INTENT_EXTRA_NEXT_ID,R.id.audio_notify_control_next);
          setClickListener(mRemoteViews,INTENT_EXTRA_LYRIC_ID,R.id.audio_notify_control_lyric);
          setClickListener(mRemoteViews,INTENT_EXTRA_CLICKED_ID,R.id.audio_notify_control_layout);  //捕获点击事件

          /**
           * 桌面歌词
           */
          initAlertWindow();

          mNotificationManager.notify(SCREEN_NOTIFICATION_ID,mNotification);
      }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.window_lyric_lock_btn:
                setLyricLock();
                break;
            case R.id.window_lyric_close_btn:
                closeDesktopLyric();
                break;
            case R.id.window_lyric_desktop:
                setAlertWindowState(true);  //让锁定按钮和关闭按钮显示出来
                break;
            default:    break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPosY=mParams.y;
                mRawPosY=event.getRawY();
                isMove=false;
                break;
            case MotionEvent.ACTION_MOVE:
                if(!isLyricLock) {  //如果歌词被锁定，无法移动位置
                    int yPos = mPosY + (int) (event.getRawY() - mRawPosY);
                    updateLyricPosition(0, yPos);
                }
                isMove=true;  //有移动的趋势，仍然过滤点击事件
                break;
            case MotionEvent.ACTION_UP:

                return isMove;  //发生移动后不再有点击事件
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return false;
    }

      private PendingIntent createEventIntent(int EXTRA_CLICK_ID,int flags){
          Intent intent = new Intent(ACTION_CLICK_EVENT);
          intent.putExtra(INTENT_EXTRA_TAG, EXTRA_CLICK_ID);
          PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, EXTRA_CLICK_ID, intent, flags);
          return pendingIntent;
      }

      private void setClickListener(RemoteViews remoteViews,int EXTRA_CLICK_ID,int viewId){
          Intent intent = new Intent(ACTION_CLICK_EVENT);
          intent.putExtra(INTENT_EXTRA_TAG, EXTRA_CLICK_ID);
          PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, EXTRA_CLICK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
          remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
      }

      private void initAlertWindow(){
          mWindowLyric= LayoutInflater.from(mContext).inflate(R.layout.audio_lyrics_window,null);
          mLockView=(ImageView)mWindowLyric.findViewById(R.id.window_lyric_lock_btn);
          mCloseView=(ImageView)mWindowLyric.findViewById(R.id.window_lyric_close_btn);
          mLyricView=(TextView)mWindowLyric.findViewById(R.id.window_lyric_text_view);
          mLockView.setOnClickListener(this);
          mCloseView.setOnClickListener(this);
          mWindowLyric.setOnClickListener(this);
          mWindowLyric.setOnTouchListener(this);

          //显示位置参数
          mParams = new WindowManager.LayoutParams();
          mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
          mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
          mParams.format = PixelFormat.RGBA_8888;
          mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
          mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
          mParams.gravity = Gravity.LEFT | Gravity.TOP;
          mParams.x=0;
          mParams.y=500;

          checkAlertWindow();  // need or no need
          isLyricLock=false;
          setLyricBtnVisible(false);
      }

      //注意是不显示，而不是消失
      private void setLyricBtnVisible(boolean isVisible){
          mLockView.setVisibility(isVisible?View.VISIBLE:View.INVISIBLE);
          mCloseView.setVisibility(isVisible?View.VISIBLE:View.INVISIBLE);
      }

      public void updateAudioInfo(AudioEntity audioEntity){
        if(audioEntity!=null) {
            mNotification.contentView.setTextViewText(R.id.audio_notify_control_title, audioEntity.title);
            mNotification.contentView.setTextViewText(R.id.audio_notify_control_singer,audioEntity.artist);
            mNotificationManager.notify(SCREEN_NOTIFICATION_ID, mNotification);
        }
    }

      public void updatePlayState(int state){
        switch (state){
            case AudioPlayer.PLAYER_STATE_START:
                mNotification.contentView.setImageViewResource(R.id.audio_notify_control_play,R.mipmap.audio_btn_play);
                mNotification.flags=Notification.FLAG_ONGOING_EVENT; //播放的时候不可以停止service
                mNotificationManager.notify(SCREEN_NOTIFICATION_ID,mNotification);
                isPlay=true;
                break;
            case AudioPlayer.PLAYER_STATE_PAUSE:
                mNotification.contentView.setImageViewResource(R.id.audio_notify_control_play,R.mipmap.audio_btn_stop);
                if(isCanClear) mNotification.flags=Notification.FLAG_AUTO_CANCEL;  //应用已经退出，这时候暂停，可以停止service
                mNotificationManager.notify(SCREEN_NOTIFICATION_ID,mNotification);
                isPlay=false;
                break;
            default:  break;
        }
    }


      public void updateAlertWindow(boolean isVisible){
        Log.d("ScreenManager"," updateAlertWindow is "+isVisible);
        if(isShowLyric) {
            setLyricBtnVisible(isVisible);  //
            mWindowLyric.setBackgroundColor(isVisible ? Color.parseColor("#33666666") : Color.TRANSPARENT);
        }
    }

      private void setAlertWindowState(boolean isVisible){
        Log.d("ScreenManager"," setAlertWindowState is "+isVisible);
          if(isShowLyric) {
              screenNotify(INTENT_EXTRA_MSG_REMOVE,"",0);  //移除之前可能发送的2秒隐藏消息
              setLyricBtnVisible(isVisible);  //
              mWindowLyric.setBackgroundColor(isVisible ? Color.parseColor("#33666666") : Color.TRANSPARENT);
              screenNotify(INTENT_EXTRA_MSG_DELAY,"",MSG_HIDDEN_DELAY);  //重新发送隐藏消息
          }

      }

      private void closeDesktopLyric(){
          screenNotify(INTENT_EXTRA_MSG_REMOVE,"",0);  //移除之前可能发送的2秒隐藏消息
          updateAlertWindow(false); //设置为默认状态
          setDesktopLyric(); //关闭桌面歌词
      }

      private void setLyricLock(){
          isLyricLock=!isLyricLock;
          mLockView.setImageResource(isLyricLock?R.mipmap.audio_lock:R.mipmap.audio_unlock);
      }

      private void updateLyricPosition(int x,int y){
          mParams.x=x;
          mParams.y=y;
          try {
              mWindowManager.updateViewLayout(mWindowLyric,mParams);
          } catch (Exception e) {
              e.printStackTrace();
          }
      }

      public void updateDesktopLyric(int lyricIndex,String lyric){
          if(isShowLyric&&lyricIndex!=-1){
              mLyricView.setText(lyric);
          }
      }

      public void setDesktopLyric(){
          isDesktopLyric=!isDesktopLyric;
          //#b6bcb6
          int color=isDesktopLyric?mLyricColor:Color.parseColor("#b6bcb6");
          mNotification.contentView.setTextColor(R.id.audio_notify_control_lyric,color);
          mNotificationManager.notify(SCREEN_NOTIFICATION_ID, mNotification);
          updateDesktopLyricState();
      }

      public int setLyricsVisible(boolean isVisible){
          isLyricVisible=isVisible;
          updateDesktopLyricState();
          return Error.RETURN_OK;
      }

      private void updateDesktopLyricState(){
         try{
            if(checkAlertWindow()){
                if(isDesktopLyric&&isLyricVisible){
                    mWindowManager.addView(mWindowLyric,mParams);
                    isShowLyric=true;
                }else{
                    if(isShowLyric) {
                        mWindowManager.removeView(mWindowLyric);
                        isShowLyric=false;
                    }
                }
            }
         }catch (Exception e){
             e.printStackTrace();
         }
      }

    /**
     * 队列中没有客户端的listener ，就认为没有应用再使用这个服务
     *  如果当前不是播放状态，可以取消
     * @param isAlive
     */
     public void notifyAppState(boolean isAlive){
         isCanClear=!isAlive;
         if(isCanClear&&!isPlay){
             mNotification.flags = Notification.FLAG_AUTO_CANCEL;
             mNotificationManager.notify(SCREEN_NOTIFICATION_ID,mNotification);
         }else{
             mNotification.flags=Notification.FLAG_ONGOING_EVENT;
             mNotificationManager.notify(SCREEN_NOTIFICATION_ID,mNotification);
         }
     }

     private boolean checkAlertWindow(){
         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
             try {
                 ApplicationInfo appInfo=mContext.getApplicationInfo();
                 int ret=mAppOpsManager.startOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,appInfo.uid,appInfo.packageName);
                 if(ret==AppOpsManager.MODE_ALLOWED){
                     return true;
                 }
                 if (!Settings.canDrawOverlays(mContext)) {
                     Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                             Uri.parse("package:" + mContext.getPackageName()));
                     mContext.startActivity(intent);
                     return false;    //需要等到下一次check的时候才知道，是否具有权限
                 }
             }catch (Exception e){
                 e.printStackTrace();
             }
             return true;
         }else {  //6.0以下不需要检测了吧 ？？
             return true;
         }
     }



     public void setScreenListener(ScreenListener listener){
         mScreenListener=listener;
     }

     private void screenNotify(int event,String arg0,int arg1){
          Log.d("ScreenManager"," event is "+event +" arg0 is "+arg0+" arg1 is "+arg1);
          if(mScreenListener!=null){
              mScreenListener.onScreenNotify(event, arg0, arg1);
          }
     }

      public void destroy(){
          if(mNotificationReceiver!=null){
              mContext.unregisterReceiver(mNotificationReceiver);
          }
          try {
              mNotificationManager.cancelAll();
              mWindowManager.removeView(mWindowLyric);
          }catch (Exception e){
              e.printStackTrace();
          }
      }

      class NotificationReceiver extends BroadcastReceiver{
          @Override
          public void onReceive(Context context, Intent intent) {
              String action = intent.getAction();
              if(action.equals(ACTION_CLICK_EVENT)){
                  int clickEvent=intent.getIntExtra(INTENT_EXTRA_TAG,0);
                  screenNotify(clickEvent,"",0);
              }
          }
      }

      public interface ScreenListener{
          void onScreenNotify(int event,String arg0,int arg1);
      }


}
