package com.hxiong.audioplayer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by hxiong on 2017/5/9 23:13.
 * Email 2509477698@qq.com
 */

public class CommonUtils {

    public static final int LYRIC_SEPARATOR = ']';
    public static final int DURATION_INVALID = -1;

    public static final String LYRIC_SUFFIX =".lrc";
    //编码格式，可能会存在乱码哦
    public static final String LYRIC_CHARSET ="GBK";

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
            InputStreamReader inReader=null;
            lyricsList=new LyricsList();
            try {
                inReader = new InputStreamReader(new FileInputStream(file), LYRIC_CHARSET);
                BufferedReader bufferedReader=new BufferedReader(inReader);
                String str;
                while ((str=bufferedReader.readLine())!=null){
                    readOneLyric(lyricsList,str);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeFileReader(inReader);
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

    public static void closeFileReader(InputStreamReader inReader){
        try {
            if(inReader!=null) inReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
