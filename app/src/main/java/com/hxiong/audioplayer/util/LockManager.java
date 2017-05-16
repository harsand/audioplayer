package com.hxiong.audioplayer.util;

/**
 * Created by hxiong on 2017/5/11 21:05.
 * Email 2509477698@qq.com
 */

public abstract class LockManager {

    /**
     *
     * @param lockId
     * @return
     */
      public abstract boolean holdLock(int lockId);

    /**
     *
     * @param lockId
     * @return
     */
      public abstract boolean releaseLock(int lockId);
}
