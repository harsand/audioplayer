package com.hxiong.audioplayer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by hxiong on 2017/5/9 23:13.
 * Email 2509477698@qq.com
 */

public class CommonUtils {

    public static final int LYRIC_SEPARATOR = ']';
    public static final int DURATION_INVALID = -1;

    public static final String LYRIC_SUFFIX =".lrc";

    private CommonUtils(){   }

    public static String getAudioTime(int duration){
        if(duration<0){
            return "";
        }
        duration/=1000;
        int mins=duration/60;
        int secs=duration%60;
        String time="";
        if(mins<10){
            time="0"+mins;
        }else{
            time+=mins;
        }
        time+=":";
        if(secs<10){
            time=time+"0"+secs;
        }else{
            time+=secs;
        }
        return time;
    }

    public static LyricsList readLyrics(String path){
        File file=new File(path);
        LyricsList lyricsList=null;
        if(file.exists()&&file.isFile()){
            FileReader fileReader=null;
            lyricsList=new LyricsList();
            try {
                fileReader= new FileReader(file);
                BufferedReader bufferedReader=new BufferedReader(fileReader);
                String str;
                while ((str=bufferedReader.readLine())!=null){
                    readOneLyric(lyricsList,str);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeFileReader(fileReader);
            }
        }
        return lyricsList;
    }

    public static void readOneLyric(LyricsList lyricsList,String str){
        try {
            int separatorIndex=str.indexOf(LYRIC_SEPARATOR);
            if(separatorIndex<0){  //not find
                return ;
            }
            String time=str.substring(1,separatorIndex);
            int duration=timeToDuration(time);
            if(duration>DURATION_INVALID){
                String lyric=str.substring(separatorIndex+1);  //index +1
                lyricsList.put(duration,lyric);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static int timeToDuration(String time){
        try{
            time = time.replace(".", ":");
            String timeData[] = time.split(":");
            int minute = Integer.parseInt(timeData[0]);
            int second = Integer.parseInt(timeData[1]);
            int millisecond = Integer.parseInt(timeData[2]);

            int duration = (minute * 60 + second) * 1000 + millisecond * 10;
            return duration;
        }catch (Exception e){
            e.printStackTrace();
        }
        return DURATION_INVALID;
    }

    public static void closeFileReader(FileReader fileReader){
        try {
            if(fileReader!=null) fileReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
