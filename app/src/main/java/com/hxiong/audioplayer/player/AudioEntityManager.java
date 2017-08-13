package com.hxiong.audioplayer.player;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.hxiong.audioplayer.aidl.IAudioPlayerListener;
import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.DefaultValue;
import com.hxiong.audioplayer.util.Error;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by hxiong on 2017/5/14 10:40.
 * Email 2509477698@qq.com
 */

public class AudioEntityManager {

    protected static final String CURR_PLAY_ID_FILE="/cur_id";

    protected static final int MSG_INIT_AUDIO_LIST = 1;  //获取歌曲列表，可能会比较耗时，所以放在新的线程处理

    protected  static final int PLAY_ORDER_SINGLE = 0;
    protected  static final int PLAY_ORDER_ORDER= 1;
    protected  static final int PLAY_ORDER_RANDOM= 2;

    private Context mContext;
    private Object mLock;   //ops need lock
    private String curAudioListName;
    private int curPlayId;
    private HashMap<String,ArrayList<AudioEntity>> mAudioListMap;
    private Random mRandom;
    private int mPlayOrder;

    //管理歌曲列表，需要一个新的线程
    private HandlerThread mHandlerThread;
    private AudioEntityHandle mAudioEntityHandle;
    private SQLiteManager mSQLiteManager;
    private AudioEntityListener mAudioEntityListener;

    public AudioEntityManager(Context context){
        this.mContext=context;
        mLock=new Object();
        curAudioListName=DefaultValue.DEFAULT_AUDIO_LIST_NAME;
        curPlayId=DefaultValue.DEFAULT_AUDIO_PLAY_ID;
        mAudioListMap=new HashMap<String,ArrayList<AudioEntity>>();
        mRandom=new Random();
        mPlayOrder=PLAY_ORDER_ORDER;
    }

    public void init(){
        mHandlerThread=new HandlerThread("AudioEntityManager");
        mHandlerThread.start();  //important
        mAudioEntityHandle=new AudioEntityHandle(mHandlerThread.getLooper());
        mSQLiteManager=new SQLiteManager(mContext);
        mAudioEntityHandle.sendEmptyMessage(MSG_INIT_AUDIO_LIST);
    }

    public void setAudioEntityListener(AudioEntityListener listener){
        mAudioEntityListener=listener;
    }

    private void notifyListener(int event,String arg0,int arg1){
        if(mAudioEntityListener!=null){
            mAudioEntityListener.onAudioEntityNotify(event, arg0, arg1);
        }
    }

    public String[] getAudioListName(){

        return null;
    }

    public String getCurAudioListName(){
        synchronized (mLock) {
            return curAudioListName;
        }
    }

    public int setCurAudioListName(String audioListName){
        synchronized (mLock) {
            if(mAudioListMap.containsKey(audioListName)){
                curAudioListName=audioListName;
                return Error.RETURN_OK;
            }
            return Error.RETURN_ERROR;
        }
    }

    public int getCurPlayId(){
        synchronized (mLock){
            return  curPlayId;
        }
    }

    public void setCurPlayId(int id){
        synchronized (mLock){
            curPlayId=id;
            setCurIdToFile(curPlayId);
        }
    }

    public int getNextPlayId(){
        return getNextPlayId(mPlayOrder);
    }

    public int getNextPlayId(int order){
        synchronized (mLock){
            ArrayList<AudioEntity> list = mAudioListMap.get(curAudioListName);
            if (list != null) {
                return calculateByOrder(list.size(),order);
            }
            return curPlayId;  //return current play item
        }
    }

    public int setPlayOrder(int order){
        synchronized (mLock) {
            if(order==mPlayOrder) {
                return Error.RETURN_ALREADY;
            }
            mPlayOrder=order;
            return Error.RETURN_OK;
        }
    }

    public int getPlayOrder(){
        synchronized (mLock){
            return mPlayOrder;
        }
    }

    /**
     * 获取下一首个的方式，目前是顺序
     * @param size  列表的大小
     * @param order 获取的方式
     * @return
     */
    private int calculateByOrder(int size,int order){
        int playId=0;
        switch (order){
            case PLAY_ORDER_SINGLE:
                playId=curPlayId;
                break;
            case PLAY_ORDER_ORDER:
                playId=curPlayId+1;
                playId=playId>=size?0:playId;
                break;
            case PLAY_ORDER_RANDOM:
                playId=mRandom.nextInt(1000)%size;
                break;
        }
        return playId;
    }

    public String getAudioEntityPath(int index){
        synchronized (mLock) {
            ArrayList<AudioEntity> list = mAudioListMap.get(curAudioListName);
            if (list != null&&index<list.size()) {
                AudioEntity audioEntity = list.get(index);
                return audioEntity==null?null:audioEntity.path;
            }
            return null;
        }
    }

    public AudioEntity[] getAudioEntityList(String audioListName){
        synchronized (mLock) {
            ArrayList<AudioEntity> list = mAudioListMap.get(audioListName);
            if (list != null) {
                AudioEntity[] audioEntities = new AudioEntity[list.size()];
                return list.toArray(audioEntities);
            }
            return new AudioEntity[0];
        }
    }

    /**
     * 当前正在播放的歌曲信息
     * @return
     */
    public AudioEntity getCurAudioEntity(){
        synchronized (mLock) {
            ArrayList<AudioEntity> list = mAudioListMap.get(curAudioListName);
            return (list == null|| curPlayId>=list.size()) ? null : list.get(curPlayId);
        }
    }

    public void destroy(){
         try{
             mHandlerThread.quit();
         }catch (Exception e){
             e.printStackTrace();
         }
    }

    /**
     *
     */
    private class AudioEntityHandle extends Handler{

        public AudioEntityHandle(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT_AUDIO_LIST:
                    handleInitAudioList();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private void handleInitAudioList() {
        synchronized (mLock){
            ArrayList<AudioEntity> list = getDefaultAudioList();
            mAudioListMap.put(DefaultValue.DEFAULT_AUDIO_LIST_NAME, list);
            curPlayId=getCurIdFromFile();
        }

        //初始化列表完成之后，通知到app
        notifyListener(IAudioPlayerListener.EVENT_TYPE_BUILD_LIST,"",0);
    }

    private ArrayList<AudioEntity> getDefaultAudioList(){
        ArrayList<AudioEntity> audioList=new ArrayList<AudioEntity>();
        Cursor cursor = null;
        try{
            cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE},
                    null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            if(cursor!=null){
                Log.d("AudioListManager","cursor is null..");
                getAudioList(cursor,audioList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
        return audioList;
    }

    private void getAudioList(Cursor cursor, ArrayList<AudioEntity> audioList){
        while(cursor.moveToNext()){
            int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            Log.d("AudioListManager","duration="+duration+" size="+size);
            if(!isLegalAudio(duration,size)){
                continue;
            }
            AudioEntity audioEntity=new AudioEntity();
            audioEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            audioEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            audioEntity.name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            audioEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            audioEntity.album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            audioEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            audioEntity.duration=duration;
            audioEntity.size=size;
            audioList.add(audioEntity);
        }
    }

    private boolean isLegalAudio(int duration,long size){
        if(size < 1024 * 1024 || duration < 60 *1000) {
            return  false;
        }
        return true;
    }

    private void setCurIdToFile(int playId){
        FileOutputStream fous=null;
        try{
            File externalDir= Environment.getExternalStorageDirectory();
            String curIdPath=externalDir.getPath()+CURR_PLAY_ID_FILE;
            fous=new FileOutputStream(curIdPath);
            DataOutputStream dous=new DataOutputStream(fous);
            dous.writeInt(playId);
            dous.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fous!=null) fous.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getCurIdFromFile(){
        int curId=DefaultValue.DEFAULT_AUDIO_PLAY_ID;
        FileInputStream fin=null;
        try{
            File externalDir= Environment.getExternalStorageDirectory();
            String curIdPath=externalDir.getPath()+CURR_PLAY_ID_FILE;
            fin=new FileInputStream(curIdPath);
            DataInputStream din=new DataInputStream(fin);
            curId=din.readInt();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fin!=null) fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return curId;
    }

    public interface AudioEntityListener{
        void onAudioEntityNotify(int event,String arg0,int arg1);
    }
}
