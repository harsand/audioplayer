package com.hxiong.audioplayer.util;

/**
 * Created by hxiong on 2017/5/11 21:07.
 * Email 2509477698@qq.com
 */

public class SpreadLock extends LockManager {

    public  static final int SPREAD_UNLOCK=-1;

    private int mSpreadLock=SPREAD_UNLOCK;
    private Object mLock=new Object();

    @Override
    public boolean holdLock(int lockId) {
        synchronized (mLock) {
            if (mSpreadLock == SPREAD_UNLOCK) {
                mSpreadLock = lockId;
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean releaseLock(int lockId) {
        synchronized (mLock) {
            if (mSpreadLock == lockId) {
                mSpreadLock = SPREAD_UNLOCK;
                return true;
            }
            return false;
        }
    }

    public int getLockId(){
        synchronized (mLock) {
            return mSpreadLock;
        }
    }
}
