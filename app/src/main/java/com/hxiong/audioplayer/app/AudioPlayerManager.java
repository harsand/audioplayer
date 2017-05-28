package com.hxiong.audioplayer.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hxiong.audioplayer.aidl.IAudioPlayer;
import com.hxiong.audioplayer.aidl.IAudioPlayerListener;
import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.Error;

/**
 * Created by hxiong on 2017/5/7 20:37.
 * Email 2509477698@qq.com
 */

public class AudioPlayerManager {

    /**
     *  keep the same witch service action
     */
    private static final String AUDIO_PLAYER_SERVICE="com.hxiong.audioplayer.player.AudioPlayerService";
    private static final String TAG="AudioPlayerManager";
    private static final boolean ENABLE_LOG=true;

    /**
     * please keep the same witch com.hxiong.audioplayer.player.AudioPlayer
     */
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
    public static final int EVENT_TYPE_SYNC = 5;
    public static final int EVENT_TYPE_STATE = 6;

    private static AudioPlayerManager mAudioPlayerManager=new AudioPlayerManager();
    private IAudioPlayer mAudioPlayer;
    private AudioPlayerListener mAudioPlayerListener;

    //only allow one to bind service
    private Context mContext;
    private ConnectionListener mConnectionListener;

    private boolean isBindService;

     //not allow
     private AudioPlayerManager(){
         isBindService=false;
     }

     public static AudioPlayerManager get(){
         return mAudioPlayerManager;
     }

    /**
     *
     * @param listener
     */
    public synchronized boolean connect(Context context,ConnectionListener listener){
        if(context==null||listener==null){
            printLog("context or listener is null.");
            return false;
        }
        //确保service 是先被start，然后再bind
         if(!startPlayerService(context)){
             printLog("startPlayerService fail.");
             return false;
         }
         return bindPlayerService(context,listener);
    }

    /**
     *
     */
    public void disconnect(){
         if(isBindService){
             removePlayerListener();  //remove listener
             if(mContext!=null) mContext.unbindService(mServiceConnection);
         }
        //if(mConnectionListener!=null) mConnectionListener.onDisconnected();
        isBindService=false;
        mAudioPlayer=null; //need to set null
        mContext=null;     //need to set null
        mConnectionListener=null;  //need to set null
    }

    public void setAudioPlayerListener(AudioPlayerListener listener){
        mAudioPlayerListener=listener;
    }

    public String getCurAudioListName(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.getCurAudioListName();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public AudioEntity[] getAudioEntityList(String audioListName){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.getAudioEntityList(audioListName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getCurPlayId(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.getDataSource();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int getPlayerState(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.getPlayerState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int stop(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.stop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int setDataSource(int playId){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.setDataSource(playId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int start(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.start();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int pause(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int seekTo(int position){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.seekTo(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int getDuration(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.getDuration();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public int getCurrentPosition(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.getCurrentPosition();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    public String getLyrics(){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.getLyrics();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void removePlayerListener(){
        if(isServiceAlive()){
            try {
                mAudioPlayer.removeListener(mPlayerListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int setPlayOrder(int order){
        if(isServiceAlive()){
            try {
                return mAudioPlayer.setPlayOrder(order);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Error.RETURN_ERROR;
    }

    private boolean isServiceAlive(){
        if(mAudioPlayer==null){
            printLog("audio player service is not init.");
            return false;
        }
        return true;
    }

    /**
     *  importance listener
     */
    private IAudioPlayerListener mPlayerListener=new IAudioPlayerListener.Stub(){

        @Override
        public void notifyListener(int event, String arg0, int arg1, int arg2) throws RemoteException {
            if(mAudioPlayerListener!=null){
                mAudioPlayerListener.onNotify(event, arg0, arg1, arg2);
            }
        }
    };

    private ServiceConnection mServiceConnection=new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                isBindService = true;
                mAudioPlayer = IAudioPlayer.Stub.asInterface(service);
                mAudioPlayer.addListener(mPlayerListener);
                if (mConnectionListener != null)
                    mConnectionListener.onConnected(mAudioPlayerManager);
                printLog("binder service successful.");
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            printLog("onServiceDisconnected had call.");
            // need or no need
            if(mConnectionListener!=null) mConnectionListener.onDisconnected();
            isBindService=false;
            mAudioPlayer=null; //need to set null
            mContext=null;     //need to set null
            mConnectionListener=null;  //need to set null
        }
    }
   ;
    private boolean startPlayerService(Context context){
        try {
            Intent intent = new Intent(AUDIO_PLAYER_SERVICE);
            intent.setPackage(context.getPackageName());   //need after L
            return  context.startService(intent) != null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean bindPlayerService(Context context,ConnectionListener listener){
        try {
            if (isBindService) {
                if (context == mContext) { // just change listener
                    mConnectionListener = listener;
                    mConnectionListener.onConnected(mAudioPlayerManager);
                    return true;
                } else {
                    //need to unbind service
                    if (mContext != null) mContext.unbindService(mServiceConnection);
                    isBindService = false;
                }
            }
            mContext = context;
            mConnectionListener = listener;
            Intent intent = new Intent(AUDIO_PLAYER_SERVICE);
            intent.setPackage(context.getPackageName());   //need after L
            mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private void printLog(String log){
        if(ENABLE_LOG&&log!=null) Log.d(TAG,log);
    }
    /**
     *
     */
    public interface ConnectionListener{
          void onConnected(AudioPlayerManager manager);
          void onDisconnected();
    }

    /**
     *
     */
    public interface AudioPlayerListener{
          void onNotify(int event,String arg0,int arg1,int arg2);
    }

}
