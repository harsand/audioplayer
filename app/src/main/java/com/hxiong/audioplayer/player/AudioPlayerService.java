package com.hxiong.audioplayer.player;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hxiong.audioplayer.aidl.IAudioPlayer;
import com.hxiong.audioplayer.aidl.IAudioPlayerListener;
import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.Error;

import java.util.ArrayList;

/**
 * Created by hxiong on 2017/5/7 20:36.
 * Email 2509477698@qq.com
 */

public class AudioPlayerService extends Service implements AudioPlayer.AudioPlayerListener{

    private static final String TAG="AudioPlayerService";
    private static final boolean ENABLE_LOG=true;



    private AudioPlayerBinder mBinder;
    private ArrayList<IAudioPlayerListener> mListener;
    private AudioEntityManager mAudioEntityManager;
    private AudioPlayer mAudioPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder=new AudioPlayerBinder(this);
        mListener=new ArrayList<IAudioPlayerListener>();
        mAudioEntityManager=new AudioEntityManager(getBaseContext());
        mAudioPlayer = new AudioPlayer();
        mAudioEntityManager.init();
        mAudioPlayer.setAudioPlayerListener(this);
        mAudioPlayer.init();
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinder.release();
        mListener.clear();
        mAudioPlayer.release();
        printLog("AudioPlayerService had destroy.");
    }




    //
    boolean addListener(final IAudioPlayerListener listener){
       if (mListener.contains(listener)) {
           printLog("listener has already added.");
           return false;
       }
       try {
           listener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
               @Override
               public void binderDied() {
                   //// FIXME: 2017/5/13
                   mListener.remove(listener);
               }
           },0);
       } catch (RemoteException e) {
           e.printStackTrace();
       }
       mListener.add(listener);
        return true;
    }

    boolean removeListener(IAudioPlayerListener listener){
        return mListener.remove(listener);
    }

    int getPlayerState(){
        return mAudioPlayer.getPlayerState();
    }

    String[] getAudioListName(){
        return new String[0];
    }

    String getCurAudioListName(){
        return mAudioEntityManager.getCurAudioListName();
    }

    int setCurAudioListName(String audioListName){
        return mAudioEntityManager.setCurAudioListName(audioListName);
    }

    int createAudioList(String audioListName){
        return 0;
    }

    int destroyAudioList(String audioListName){
        return 0;
    }

    AudioEntity[] getAudioEntityList(String audioListName){
        return mAudioEntityManager.getAudioEntityList(audioListName);
    }

    AudioEntity getAudioEntity(int index){
        return null;
    }

    int addAudioEntity(String audioListName,AudioEntity audioEntity){

        return 0;
    }

    int removeAudioEntity(String audioListName,int index){
        return 0;
    }

    int getDataSource(){
        return mAudioEntityManager.getCurPlayId();
    }

    int setDataSource(int playIndex){
        String path=mAudioEntityManager.getAudioEntityPath(playIndex);
        if(path==null){
            printLog("getAudioEntityPath index("+playIndex+") return null");
            return Error.RETURN_ERROR;
        }
        if(mAudioPlayer.setDataSource(path)==Error.RETURN_OK){
            mAudioEntityManager.setCurPlayId(playIndex); //
            return Error.RETURN_OK;
        }
        return Error.RETURN_ERROR;
    }

    int getDuration(){
        return  mAudioPlayer.getDuration();
    }

    int getCurrentPosition(){
        return mAudioPlayer.getCurrentPosition();
    }

    int seekTo(int msec){
        return mAudioPlayer.seekTo(msec);
    }

    String getLyrics(){
        return mAudioPlayer.getLyrics();
    }

    boolean isLyricsVisible(){

        return false;
    }

    int setLyricsVisible(boolean isVisible){
        return 0;
    }

    int setLyricsColor(int color){
        return 0;
    }

    //ops
    int start(){
        return  mAudioPlayer.start();
    }
    int pause(){
        return mAudioPlayer.pause();
    }
    int stop(){
        return mAudioPlayer.stop();
    }

    //
    void notifyListener(int event, String arg0, int arg1, int arg2){
        try {
            for (IAudioPlayerListener listener : mListener) {
                listener.notifyListener(event, arg0, arg1, arg2);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onNotifyListener(int event, String arg0, int arg1, int arg2) {
         switch (event){
             case AudioPlayer.EVENT_TYPE_COMPLETION:
                 if(doComletion()){
                     notifyListener(event, arg0, arg1, arg2);
                 }else{
                     notifyListener(AudioPlayer.EVENT_TYPE_ERROR, "error", arg1, arg2);  //发生了错误
                 }
                 break;
             default:
                 notifyListener(event, arg0, arg1, arg2);
                 break;
         }
    }

    private boolean doComletion(){   //顺序（或者随机）播放下一首哥
        if(mAudioPlayer.stop()!=Error.RETURN_OK){   //先reset
            printLog("doComletion stop fail.");
        }
        int nextPlayId=mAudioEntityManager.getNextPlayId();
        if(setDataSource(nextPlayId)!=Error.RETURN_OK){   //设置播放资源
            printLog("doComletion setDataSource fail.");
        }
        if(mAudioPlayer.start()!=Error.RETURN_OK){   //开始播放
            printLog("doComletion start fail.");
        }
        return true;
    }

    /**
     * private static class
     */
    private static class AudioPlayerBinder extends IAudioPlayer.Stub{

        private AudioPlayerService mService;

        public AudioPlayerBinder(AudioPlayerService audioPlayerService){
            this.mService=audioPlayerService;
        }

        public void release(){
            mService=null;
        }

        @Override
        public boolean addListener(IAudioPlayerListener listener)throws RemoteException{
            return mService==null?false:mService.addListener(listener);
        }

        @Override
        public boolean removeListener(IAudioPlayerListener listener) throws RemoteException {
            return mService!=null?false:mService.removeListener(listener);
        }

        @Override
        public int getPlayerState() throws RemoteException {
            return mService==null?0:mService.getPlayerState();
        }

        @Override
        public String[] getAudioListName() throws RemoteException {
            return mService==null?new String[0]:mService.getAudioListName();
        }

        @Override
        public String getCurAudioListName() throws RemoteException {
            return mService==null?null:mService.getCurAudioListName();
        }

        @Override
        public int setCurAudioListName(String audioListName) throws RemoteException {
            return mService==null?0:mService.setCurAudioListName(audioListName);
        }

        @Override
        public int createAudioList(String audioListName) throws RemoteException {
            return mService==null?0:mService.createAudioList(audioListName);
        }

        @Override
        public int destroyAudioList(String audioListName) throws RemoteException {
            return mService==null?0:mService.destroyAudioList(audioListName);
        }

        @Override
        public AudioEntity[] getAudioEntityList(String audioListName) throws RemoteException {
            return mService==null?new AudioEntity[0]:mService.getAudioEntityList(audioListName);
        }

        @Override
        public AudioEntity getAudioEntity(int index) throws RemoteException {
            return mService==null?null:mService.getAudioEntity(index);
        }

        @Override
        public int addAudioEntity(String audioListName, AudioEntity audioEntity) throws RemoteException {
            return mService==null?0:mService.addAudioEntity(audioListName,audioEntity);
        }

        @Override
        public int removeAudioEntity(String audioListName, int index) throws RemoteException {
            return mService==null?0:mService.removeAudioEntity(audioListName,index);
        }

        @Override
        public int getDataSource() throws RemoteException {
            return mService==null?0:mService.getDataSource();
        }

        @Override
        public int setDataSource(int playIndex) throws RemoteException {
            return mService==null?0:mService.setDataSource(playIndex);
        }

        @Override
        public int getDuration() throws RemoteException {
           return mService==null?0:mService.getDuration();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return mService==null?0:mService.getCurrentPosition();
        }

        @Override
        public int seekTo(int msec) throws RemoteException {
             return mService==null?0:mService.seekTo(msec);
        }

        @Override
        public String getLyrics() throws RemoteException {
            return mService==null?null:mService.getLyrics();
        }

        @Override
        public boolean isLyricsVisible() throws RemoteException {
            return mService==null?false:mService.isLyricsVisible();
        }

        @Override
        public int setLyricsVisible(boolean isVisible) throws RemoteException {
            return mService==null?0:mService.setLyricsVisible(isVisible);
        }

        @Override
        public int setLyricsColor(int color) throws RemoteException {
            return mService==null?0:mService.setLyricsColor(color);
        }

        @Override
        public int start() throws RemoteException {
            return mService==null?0:mService.start();
        }

        @Override
        public int pause() throws RemoteException {
            return mService==null?0:mService.pause();
        }

        @Override
        public int stop() throws RemoteException {
            return mService==null?0:mService.stop();
        }
    }


    private void printLog(String log){
        if(ENABLE_LOG&&log!=null) Log.d(TAG,log);
    }
}
