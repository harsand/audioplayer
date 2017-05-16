package com.hxiong.audioplayer.player;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.hxiong.audioplayer.util.CommonUtils;
import com.hxiong.audioplayer.util.Error;
import com.hxiong.audioplayer.util.LyricsList;

/**
 * Created by hxiong on 2017/5/11 23:41.
 * Email 2509477698@qq.com
 */

public class AudioPlayer {

      //audio player state
      public static final int PLAYER_STATE_IDLE = 0;
      public static final int PLAYER_STATE_SETUP = 1;
      public static final int PLAYER_STATE_PREPARE = 2;
      public static final int PLAYER_STATE_START = 3;
      public static final int PLAYER_STATE_PAUSE= 4;
      public static final int PLAYER_STATE_DESTROY = 5;

      //notify event type
      public static final int EVENT_TYPE_PREPARE = 0;
      public static final int EVENT_TYPE_COMPLETION = 1;
      public static final int EVENT_TYPE_SEEK_COMPLETE = 2;
      public static final int EVENT_TYPE_INFO = 3;
      public static final int EVENT_TYPE_ERROR = 4;
      public static final int EVENT_TYPE_LYRICS = 5;

      //AudioPlayerHandler msg
      public static final int MSG_INIT_LYRICS = 0;
      public static final int MSG_PULL_LYRICS = 1;

      protected  static final int PLAYER_PULL_DELAY = 1000;

      private MediaPlayer mMediaPlayer;
      private AudioPlayerListener mPlayerListener;
      private HandlerThread mHandlerThread;
      private AudioPlayerHandler mHandler;
      private LyricsList lyrics;
      private Object lyricsLock;
      private int mState;

      public void init(){
          mMediaPlayer=new MediaPlayer();
          mState = PLAYER_STATE_IDLE;
          mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
              @Override
              public void onPrepared(MediaPlayer mp) {
                  onNotifyListener(EVENT_TYPE_PREPARE,"onPrepared",0,0);
              }
          });
          mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
              @Override
              public void onCompletion(MediaPlayer mp) {
                  onNotifyListener(EVENT_TYPE_COMPLETION,"onCompletion",0,0);
              }
          });
          mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
              @Override
              public void onSeekComplete(MediaPlayer mp) {
                  onNotifyListener(EVENT_TYPE_SEEK_COMPLETE,"onSeekComplete",0,0);
              }
          });
          mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
              @Override
              public boolean onInfo(MediaPlayer mp, int what, int extra) {
                  onNotifyListener(EVENT_TYPE_INFO,"onInfo",what,extra);
                  return false;
              }
          });
          mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
              @Override
              public boolean onError(MediaPlayer mp, int what, int extra) {
                  onNotifyListener(EVENT_TYPE_ERROR,"onError",what,extra);
                  return false;
              }
          });
          //
          lyricsLock=new Object();
          mHandlerThread=new HandlerThread("AudioPlayer");
          mHandler=new AudioPlayerHandler(mHandlerThread.getLooper());
      }

      public void setAudioPlayerListener(AudioPlayerListener listener){
          mPlayerListener=listener;
      }

      public synchronized int getPlayerState(){
        return mState;
      }

      public synchronized int getDuration(){
          if(mState!=PLAYER_STATE_IDLE&&mState!=PLAYER_STATE_DESTROY){
              try {
                  return mMediaPlayer.getDuration();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          return Error.RETURN_ERROR;
      }

      public synchronized int getCurrentPosition(){
        if(mState!=PLAYER_STATE_IDLE&&mState!=PLAYER_STATE_DESTROY){
            try {
                return mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

      public synchronized int setDataSource(String path){
          if(mState==PLAYER_STATE_IDLE) {
              try {
                  mMediaPlayer.setDataSource(path);
                  mState=PLAYER_STATE_SETUP;
                  initLyrics(path);
                  return Error.RETURN_OK;
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          return Error.RETURN_ERROR;
      }

      protected int prepare(){
          if(mState==PLAYER_STATE_SETUP){
              try {
                  mMediaPlayer.prepare();
                  mState=PLAYER_STATE_PREPARE;
                  return Error.RETURN_OK;
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          return Error.RETURN_ERROR;
      }

      public synchronized int start(){
          prepare();  //prepare
          if(mState==PLAYER_STATE_PREPARE||mState==PLAYER_STATE_PAUSE){
              try {
                  mMediaPlayer.start();
                  mState=PLAYER_STATE_START;
                  startPullLyrics();
                  return Error.RETURN_OK;
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          return Error.RETURN_ERROR;
      }

      public synchronized int pause(){
          if(mState==PLAYER_STATE_START){
              try {
                  mMediaPlayer.pause();
                  mState=PLAYER_STATE_PAUSE;
                  stopPullLyrics();
                  return Error.RETURN_OK;
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          return Error.RETURN_ERROR;
      }

      public synchronized int stop(){
          if(mState!=PLAYER_STATE_IDLE&&mState!=PLAYER_STATE_DESTROY){
              try {
                  mMediaPlayer.reset();
                  mState=PLAYER_STATE_IDLE;
                  stopPullLyrics();
                  return Error.RETURN_OK;
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          return Error.RETURN_ERROR;
      }

      public void release(){    // never call
        try {
            mHandlerThread.quit(); // importance
            mMediaPlayer.release();
            mState=PLAYER_STATE_DESTROY;
        } catch (Exception e) {
            e.printStackTrace();
        }
      }

      public String getLyrics(){
          synchronized (lyricsLock) {
              return lyrics == null ? null : lyrics.toString();
          }
      }

      private void initLyrics(String path){
           Message message=Message.obtain();
           message.what=MSG_INIT_LYRICS;
           message.obj=path;
           mHandler.sendMessage(message);
      }

      private void onInitLyrics(String path){
          synchronized (lyricsLock){
              int lastIndex=path.lastIndexOf('.');
              if(lastIndex>0){
                 String lyricsPath=path.substring(0,lastIndex)+ CommonUtils.LYRIC_SUFFIX;
                 lyrics=CommonUtils.readLyrics(lyricsPath);
              }
          }
      }

      private void startPullLyrics(){
          mHandler.sendEmptyMessage(MSG_PULL_LYRICS);
      }

      private void stopPullLyrics(){
          mHandler.removeMessages(MSG_PULL_LYRICS);
      }

      private void pullLyrics(){
          synchronized (lyricsLock) {
              if (lyrics == null) return;   //no lyrics
              int curPosition = getCurrentPosition();

              onNotifyListener(EVENT_TYPE_LYRICS, "pullLyrics", curPosition, 0);
              mHandler.sendEmptyMessageDelayed(MSG_PULL_LYRICS, PLAYER_PULL_DELAY);  //after one second
          }
      }

      class AudioPlayerHandler extends Handler{

          public AudioPlayerHandler(Looper looper){
              super(looper);
          }

          @Override
          public void handleMessage(Message msg) {
              switch (msg.what){
                  case MSG_INIT_LYRICS:
                      onInitLyrics((String)msg.obj);
                      break;
                  case MSG_PULL_LYRICS:
                      pullLyrics();
                      break;
                   default:  break;
              }
              super.handleMessage(msg);
          }
      }

      private void onNotifyListener(int event,String arg0,int arg1,int arg2){
          if(mPlayerListener!=null){
              mPlayerListener.onNotifyListener(event, arg0, arg1, arg2);
          }
      }

    /**
     *
     */
    public interface AudioPlayerListener{
        void onNotifyListener(int event,String arg0,int arg1,int arg2);
    }
}
