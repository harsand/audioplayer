package com.hxiong.audioplayer.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.hxiong.audioplayer.R;
import com.hxiong.audioplayer.bean.AudioEntity;

/**
 * Created by hxiong on 2017/5/19 23:14.
 * Email 2509477698@qq.com
 */

public class ScreenManager {

      protected final static String ACTION_CLICK_ENVET = "com.hxiong.intent.action.click";
      protected final static String INTENT_EXTRA_TAG = "click_event_id";
      public final static int INTENT_EXTRA_PLAY_ID = 1;
      public final static int INTENT_EXTRA_NEXT_ID = 2;
      public final static int INTENT_EXTRA_LYRIC_ID = 3;
      public final static int INTENT_EXTRA_CLICKED_ID = 4;
      public final static int INTENT_EXTRA_DELETED_ID = 5;

      protected static final int SCREEN_NOTIFICATION_ID = 100;

      private NotificationManager mNotificationManager;  //
      private WindowManager mWindowManager; //
      private Notification mNotification;

      private Context mContext;
      private NotificationReceiver mNotificationReceiver;
      private ScreenListener mScreenListener;
      private boolean isDesktopLyric;
      private boolean isCanClear;   //作用标志，应用退出后，可以通过通知栏结束service
      private boolean isPlay;


      public ScreenManager(Context context){
          this.mContext=context;
          this.mNotificationManager=(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
          this.mWindowManager=(WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
          isDesktopLyric=false;
          isCanClear=false;
          isPlay=false;
      }

      public void init(){
          /**
           * 注册广播
           */
          mNotificationReceiver=new NotificationReceiver();
          IntentFilter intentFilter = new IntentFilter();
          intentFilter.addAction(ACTION_CLICK_ENVET);
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

          mNotificationManager.notify(SCREEN_NOTIFICATION_ID,mNotification);

      }

      private PendingIntent createEventIntent(int EXTRA_CLICK_ID,int flags){
          Intent intent = new Intent(ACTION_CLICK_ENVET);
          intent.putExtra(INTENT_EXTRA_TAG, EXTRA_CLICK_ID);
          PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, EXTRA_CLICK_ID, intent, flags);
          return pendingIntent;
      }

      private void setClickListener(RemoteViews remoteViews,int EXTRA_CLICK_ID,int viewId){
          Intent intent = new Intent(ACTION_CLICK_ENVET);
          intent.putExtra(INTENT_EXTRA_TAG, EXTRA_CLICK_ID);
          PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, EXTRA_CLICK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
          remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
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

      public void updateLyricState(boolean isVisible){

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

     public void setScreenListener(ScreenListener listener){
         mScreenListener=listener;
     }

     private void screenNotify(int event){
          Log.d("ScreenManager"," event is "+event);
          if(mScreenListener!=null){
              mScreenListener.onScreenNotify(event, "", 0);
          }
     }

      public void destroy(){
          if(mNotificationReceiver!=null){
              mContext.unregisterReceiver(mNotificationReceiver);
          }
          mNotificationManager.cancelAll();
      }

      class NotificationReceiver extends BroadcastReceiver{
          @Override
          public void onReceive(Context context, Intent intent) {
              String action = intent.getAction();
              if(action.equals(ACTION_CLICK_ENVET)){
                  int clickEvent=intent.getIntExtra(INTENT_EXTRA_TAG,0);
                  screenNotify(clickEvent);
              }
          }
      }

      public interface ScreenListener{
          void onScreenNotify(int event,String arg0,int arg1);
      }


}
