package com.hxiong.audioplayer.player;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

public class AudioPlayerService extends Service implements AudioPlayer.AudioPlayerListener,ScreenManager.ScreenListener{

    private static final String TAG="AudioPlayerService";
    private static final boolean ENABLE_LOG=true;


    private AudioPlayerBinder mBinder;
    private ArrayList<IAudioPlayerListener> mListener;
    private AudioEntityManager mAudioEntityManager;
    private AudioPlayer mAudioPlayer;
    private ScreenManager mScreenManager;
    private boolean mMutex=true;

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder=new AudioPlayerBinder(this);
        mListener=new ArrayList<IAudioPlayerListener>();
        mAudioEntityManager=new AudioEntityManager(getBaseContext());
        mAudioPlayer = new AudioPlayer();
        mScreenManager=new ScreenManager(getBaseContext());
        mAudioEntityManager.init();
        mAudioPlayer.setAudioPlayerListener(this);
        mAudioPlayer.init();
        mScreenManager.setScreenListener(this);
        mScreenManager.init();
        //通知栏显示
        mScreenManager.updateAudioInfo(mAudioEntityManager.getCurAudioEntity());
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        printLog("AudioPlayerService onStartCommand had call.");
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
        try {
            mBinder.release();
            mListener.clear();
            mAudioPlayer.release();
            mScreenManager.destroy();
        }catch (Exception e){
            e.printStackTrace();
        }
        printLog("AudioPlayerService had destroy.");
    }


    /**
     * message define here
     */
    protected static final int MSG_UPDATE_LYRICS = 1;

    protected static final int MSG_HIDDEN_DELAY = 2;

    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_UPDATE_LYRICS:
                    mScreenManager.updateDesktopLyric(msg.arg1,(String)msg.obj);
                    break;
                case MSG_HIDDEN_DELAY:
                    mScreenManager.updateAlertWindow(false);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };


    //
    boolean addListener(final IAudioPlayerListener listener){
       if (mListener.contains(listener)) {
           printLog("listener has already added.");
           mScreenManager.notifyAppState(true);
           mScreenManager.setLyricsVisible(false);//？？？
           return false;
       }
       try {
           listener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
               @Override
               public void binderDied() {
                   //// FIXME: 2017/5/13
                   mListener.remove(listener);
                   if(mListener.isEmpty()){
                       mScreenManager.notifyAppState(false);
                   }
               }
           },0);
       } catch (RemoteException e) {
           e.printStackTrace();
       }
       mListener.add(listener);
       mScreenManager.notifyAppState(true);
       mScreenManager.setLyricsVisible(false);
       return true;
    }

    boolean removeListener(IAudioPlayerListener listener){
        boolean ret=mListener.remove(listener);
        if(mListener.isEmpty()){
            mScreenManager.notifyAppState(false);
        }
        return ret;
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
        return mScreenManager.setLyricsVisible(isVisible);
    }

    int setLyricsColor(int color){
        return 0;
    }

    int setPlayOrder(int order){
        return mAudioEntityManager.setPlayOrder(order);
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

    public int exit(int flag){
        try{
            mAudioPlayer.stop();   //try to stop player
            stopSelf();            //stop service
            return Error.RETURN_OK;
        }catch (Exception e){
            e.printStackTrace();
        }
        return Error.RETURN_ERROR;
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
             case AudioPlayer.EVENT_TYPE_PREPARE:
                 mScreenManager.updateAudioInfo(mAudioEntityManager.getCurAudioEntity());
                 notifyListener(event, arg0, arg1, arg2);
                 break;
             case AudioPlayer.EVENT_TYPE_COMPLETION:
                 if(doComletion()){
                     notifyListener(event, arg0, arg1, arg2);
                 }else{
                     notifyListener(AudioPlayer.EVENT_TYPE_ERROR, "error", arg1, arg2);  //发生了错误
                 }
                 break;
             case AudioPlayer.EVENT_TYPE_SYNC:
                 if(arg2!=-1){   //if arg2 is not -1
                     Message msg=Message.obtain();
                     msg.what=MSG_UPDATE_LYRICS;
                     msg.obj=arg0;
                     msg.arg1=arg2;
                     mHandler.sendMessage(msg);
                 }
                 notifyListener(event, arg0, arg1, arg2);
                 break;
             case AudioPlayer.EVENT_TYPE_STATE:
                 mScreenManager.updatePlayState(arg1);
                 notifyListener(event, arg0, arg1, arg2);
                 break;
             default:
                 notifyListener(event, arg0, arg1, arg2);
                 break;
         }
    }

    private boolean doComletion(){
        int playId=mAudioEntityManager.getNextPlayId();//顺序（或者随机）播放下一首歌
        return playAudio(playId);
    }

    /**
     *
     * @return
     */
    private boolean playNextAudio(){
        int nextPlayId=mAudioEntityManager.getNextPlayId(AudioEntityManager.PLAY_ORDER_ORDER);
        return playAudio(nextPlayId);
    }

    private boolean playAudio(int playId){
        int state=mAudioPlayer.getPlayerState();
        if(state!=AudioPlayer.PLAYER_STATE_IDLE&&mAudioPlayer.stop()!=Error.RETURN_OK){   //先reset
            printLog("playAudio stop fail.");
        }

        if(setDataSource(playId)!=Error.RETURN_OK){   //设置播放资源
            printLog("playAudio setDataSource fail.");
        }
        if(mAudioPlayer.start()!=Error.RETURN_OK){   //开始播放
            printLog("playAudio start fail.");
        }
        return true;
    }

    @Override
    public void onScreenNotify(int event, String arg0, int arg1) {
         switch (event){
             case ScreenManager.INTENT_EXTRA_PLAY_ID:
                 doPlay();
                 break;
             case ScreenManager.INTENT_EXTRA_NEXT_ID:
                 playNextAudio();
                 break;
             case ScreenManager.INTENT_EXTRA_LYRIC_ID:
                 mScreenManager.setDesktopLyric();
                 break;
             case ScreenManager.INTENT_EXTRA_CLICKED_ID:
                 sendBroadcast();
                 break;
             case ScreenManager.INTENT_EXTRA_DELETED_ID:
                 printLog("AudioPlayerService stopSelf().");
                 stopSelf();  //停止service
                 break;
             case ScreenManager.INTENT_EXTRA_MSG_DELAY:
                 sendHiddenMessage(arg1);
                 break;
             case ScreenManager.INTENT_EXTRA_MSG_REMOVE:
                 removeHiddenMessage();
                 break;
             default: break;
         }
    }

    private void doPlay(){
        int playerState=mAudioPlayer.getPlayerState();
        if(playerState==AudioPlayer.PLAYER_STATE_IDLE){
            int playId=mAudioEntityManager.getCurPlayId();
            playAudio(playId);
        }else if(playerState==AudioPlayer.PLAYER_STATE_PAUSE){
            if(mAudioPlayer.start()!=Error.RETURN_OK){
                printLog("doPlay start fail.");
            }
        }else if(playerState==AudioPlayer.PLAYER_STATE_START){
            if(mAudioPlayer.pause()!=Error.RETURN_OK){
                printLog("doPlay pause fail.");
            }
        }
    }

    private void sendBroadcast(){
        if(mMutex){
            mMutex=false;
            Intent intent=new Intent();
            intent.setAction("com.hxiong.audioplayer.wakeup");
            getBaseContext().sendBroadcast(intent);
            mMutex=true;
        }
    }

    private void sendHiddenMessage(long delay){
        try {
            mHandler.sendEmptyMessageDelayed(MSG_HIDDEN_DELAY, delay);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void removeHiddenMessage(){
        try {
            mHandler.removeMessages(MSG_HIDDEN_DELAY);
        }catch (Exception e){
            e.printStackTrace();
        }
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
            return mService==null?false:mService.removeListener(listener);
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
            return mService==null?Error.RETURN_ERROR:mService.setCurAudioListName(audioListName);
        }

        @Override
        public int createAudioList(String audioListName) throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.createAudioList(audioListName);
        }

        @Override
        public int destroyAudioList(String audioListName) throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.destroyAudioList(audioListName);
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
            return mService==null?Error.RETURN_ERROR:mService.addAudioEntity(audioListName,audioEntity);
        }

        @Override
        public int removeAudioEntity(String audioListName, int index) throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.removeAudioEntity(audioListName,index);
        }

        @Override
        public int getDataSource() throws RemoteException {
            return mService==null?0:mService.getDataSource();
        }

        @Override
        public int setDataSource(int playIndex) throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.setDataSource(playIndex);
        }

        @Override
        public int getDuration() throws RemoteException {
           return mService==null?Error.RETURN_ERROR:mService.getDuration();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.getCurrentPosition();
        }

        @Override
        public int seekTo(int msec) throws RemoteException {
             return mService==null?Error.RETURN_ERROR:mService.seekTo(msec);
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
            return mService==null?Error.RETURN_ERROR:mService.setLyricsVisible(isVisible);
        }

        @Override
        public int setLyricsColor(int color) throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.setLyricsColor(color);
        }

        @Override
        public int setPlayOrder(int order) throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.setPlayOrder(order);
        }

        @Override
        public int start() throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.start();
        }

        @Override
        public int pause() throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.pause();
        }

        @Override
        public int stop() throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.stop();
        }

        @Override
        public int exit(int flag) throws RemoteException {
            return mService==null?Error.RETURN_ERROR:mService.exit(flag);
        }
    }


    private void printLog(String log){
        if(ENABLE_LOG&&log!=null) Log.d(TAG,log);
    }
}
