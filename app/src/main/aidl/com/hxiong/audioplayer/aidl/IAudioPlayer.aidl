// IAudioPlayer.aidl
package com.hxiong.audioplayer.aidl;
import com.hxiong.audioplayer.aidl.IAudioPlayerListener;
import com.hxiong.audioplayer.bean.AudioEntity;

// Declare any non-default types here with import statements

interface IAudioPlayer {


        boolean addListener(IAudioPlayerListener listener);

        boolean removeListener(IAudioPlayerListener listener);

        int getPlayerState();

        String[] getAudioListName();

        String getCurAudioListName();

        int setCurAudioListName(String audioListName);

        int createAudioList(String audioListName);

        int destroyAudioList(String audioListName);

        AudioEntity[] getAudioEntityList(String audioListName);

        AudioEntity getAudioEntity(int index);

        int addAudioEntity(String audioListName,in AudioEntity audioEntity);

        int removeAudioEntity(String audioListName,int index);

        int getDataSource();

        int setDataSource(int playIndex);

        int getDuration();

        int getCurrentPosition();

        int seekTo(int msec);

        String getLyrics();

        boolean isLyricsVisible();

        int setLyricsVisible(boolean isVisible);

        int setLyricsColor(int color);

        int setPlayOrder(int order);

        //ops
        int start();
        int pause();
        int stop();

        //destroy function
        int exit(int flag);
}
