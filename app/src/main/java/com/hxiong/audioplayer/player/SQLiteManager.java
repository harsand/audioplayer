package com.hxiong.audioplayer.player;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hxiong on 2017/8/13 16:20.
 * Email 2509477698@qq.com
 */

public class SQLiteManager {

      private Context mContext;
      private MusicSQLiteHelper mMusicSQLiteHelper;

      public SQLiteManager(Context context){
           this.mContext=context;
           init();
      }

      public void init(){

      }

    /**
     * 保存默认歌曲列表之外的歌曲列表，也就是自定义的歌曲列表
     */
    private class MusicSQLiteHelper extends SQLiteOpenHelper{

         public static final String DB_NAME = "music_list";
         public static final int DB_VERSION = 1;

         public MusicSQLiteHelper(Context context) {
             super(context, DB_NAME, null, DB_VERSION);
         }

         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL("create table if not exists audio_list(id integer primary key," +
                     "audio_id integer,title varchar)");
         }

         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

         }
     }


}
