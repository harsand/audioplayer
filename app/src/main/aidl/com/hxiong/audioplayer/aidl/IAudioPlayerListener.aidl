// IAudioPlayerListener.aidl
package com.hxiong.audioplayer.aidl;

// Declare any non-default types here with import statements

interface IAudioPlayerListener {
     //notify event type
      const int EVENT_TYPE_PREPARE = 0;
      const int EVENT_TYPE_COMPLETION = 1;
      const int EVENT_TYPE_SEEK_COMPLETE = 2;
      const int EVENT_TYPE_INFO = 3;
      const int EVENT_TYPE_ERROR = 4;
      const int EVENT_TYPE_SYNC = 5;
      const int EVENT_TYPE_STATE = 6;
      const int EVENT_TYPE_BUILD_LIST = 7;

    void notifyListener(int event,String arg0,int arg1,int arg2);
}
