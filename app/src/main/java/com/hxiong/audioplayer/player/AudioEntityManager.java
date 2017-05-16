package com.hxiong.audioplayer.player;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.DefaultValue;
import com.hxiong.audioplayer.util.Error;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hxiong on 2017/5/14 10:40.
 * Email 2509477698@qq.com
 */

public class AudioEntityManager {

    private Context mContext;
    private Object mLock;   //ops need lock
    private String curAudioListName;
    private int curPlayId;
    private HashMap<String,ArrayList<AudioEntity>> mAudioListMap;

    public AudioEntityManager(Context context){
        this.mContext=context;
        mLock=new Object();
        curAudioListName=DefaultValue.DEFAULT_AUDIO_LIST_NAME;
        curPlayId=DefaultValue.DEFAULT_AUDIO_PLAY_ID;
        mAudioListMap=new HashMap<String,ArrayList<AudioEntity>>();
    }

    public void init(){
        ArrayList<AudioEntity> list= getDefaultAudioList();
        mAudioListMap.put(DefaultValue.DEFAULT_AUDIO_LIST_NAME,list);

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
        }
    }

    public String getAudioEntityPath(int index){
        synchronized (mLock) {
            ArrayList<AudioEntity> list = mAudioListMap.get(curAudioListName);
            if (list != null) {
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

    private ArrayList<AudioEntity> getDefaultAudioList(){
        ArrayList<AudioEntity> audioList=new ArrayList<AudioEntity>();
//        AudioEntity entity=new AudioEntity();
//        entity.name="test.mp3";
//        entity.title="test";
//        entity.artist="hxiong";
//        entity.duration=123*1000;
//        audioList.add(entity);
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
//        audioList.add(new AudioEntity());
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
}
