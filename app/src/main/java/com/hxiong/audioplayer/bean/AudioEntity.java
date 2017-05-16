package com.hxiong.audioplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hxiong on 2017/5/8 22:34.
 * Email 2509477698@qq.com
 */

public class AudioEntity implements Parcelable {

    public int id; //id标识
    public String title; // 显示名称
    public String name; // 文件名称
    public String path; // 音乐文件的路径
    public int duration; // 媒体播放总时间
    public String album; // 专辑
    public String artist; // 艺术家
    public long size;    //文件大小

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeInt(duration);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeLong(size);
    }

    public static final Parcelable.Creator<AudioEntity> CREATOR = new Parcelable.Creator<AudioEntity>() {
        /**
         * Return a new rectangle from the data in the specified parcel.
         */
        public AudioEntity createFromParcel(Parcel in) {
            AudioEntity audioEntity = new AudioEntity();
            audioEntity.readFromParcel(in);
            return audioEntity;
        }

        /**
         * Return an array of rectangles of the specified size.
         */
        public AudioEntity[] newArray(int size) {
            return new AudioEntity[size];
        }
    };

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        title = in.readString();
        name = in.readString();
        path= in.readString();
        duration=in.readInt();
        album = in.readString();
        artist = in.readString();
        size = in.readLong();
    }

}
