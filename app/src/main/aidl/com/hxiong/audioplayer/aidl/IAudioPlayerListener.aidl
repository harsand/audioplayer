// IAudioPlayerListener.aidl
package com.hxiong.audioplayer.aidl;

// Declare any non-default types here with import statements

interface IAudioPlayerListener {
    void notifyListener(int event,String arg0,int arg1,int arg2);
}
