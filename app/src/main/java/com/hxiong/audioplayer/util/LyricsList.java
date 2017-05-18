package com.hxiong.audioplayer.util;

/**
 * Created by hxiong on 2017/5/15 23:58.
 * Email 2509477698@qq.com
 */

public class LyricsList {

    //base
    private int[] mKeys;
    private String[] mValues;
    private int mSize;

    //当前或上一次 选中的下标，也就是当前需要显示的一行歌词
    private int mHitIndex;

    public LyricsList(){
        this(10);
    }

    public LyricsList(int length){
        length=length<0?0:length;
        mKeys=new int[length];
        mValues=new String[length];
        mSize=0;
        mHitIndex=-1;
    }

    public void put(int key,String value){
        if(mSize>=mKeys.length)
            growArray();
        mKeys[mSize]=key;
        mValues[mSize]=value;
        mSize++;
    }

    public int keyAt(int index){
        return mKeys[index];
    }

    public String valueAt(int index){
        return mValues[index];
    }

    public int size(){
        return mSize;
    }

    /**
     * 查找符合这个时间点的歌曲
     * @param msec 参考时间
     * @return  如果符合，返回歌词下标
     */
    public boolean findHitLyric(int msec){
        //快速查找
        if((mHitIndex+2)<mSize&&mKeys[mHitIndex+1]<=msec&&mKeys[mHitIndex+2]>msec){
            mHitIndex++;
            return true;
        }
        int hitIndex=findHitLyric(0,msec);  //可能歌曲被seek，从头往后找
        if(mHitIndex!=hitIndex){
            mHitIndex=hitIndex;
            return true;
        }
        return false;
    }

    /**
     *
     * @param referIndex
     * @param msec
     * @return
     */
    private int findHitLyric(int referIndex,int msec){
        for(int i=referIndex;i<mSize;i++){
            if(mKeys[i]<=msec){
                if(i==(mSize-1)){  //达到尾部
                    return i;
                }
                if((i+1)<mSize&&mKeys[i+1]>msec){ //小于下一行歌词的时间
                    return i;
                }
            }
        }
        return referIndex;
    }

    public void setHitIndex(int hitIndex){
        mHitIndex=hitIndex;
    }

    public int getHitIndex(){
        return mHitIndex;
    }

    public String getHitLyric(){
        if(mHitIndex<0||mHitIndex>mSize-1)
            return "欢迎使用Audio Player";
        return mValues[mHitIndex];
    }

    private void growArray(){
         int[] keys=new int[mSize+10];
         String[] values=new String[mSize+10];
         System.arraycopy(mKeys,0,keys,0,mSize);
         System.arraycopy(mValues,0,values,0,mSize);
         mKeys=null; //for gc ??
         mValues=null; //for gc ??
         mKeys=keys;   //new array
         mValues=values;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(mSize * 32);
        buffer.append(intToString(mSize));
        buffer.append(intToString(mHitIndex));
        buffer.append(intToString(mKeys.length));
        for(int i=0;i<mSize;i++){
            buffer.append(intToString(mKeys[i]));
            if(mValues[i]==null){   // no allow to be null
                buffer.append(intToString(-1));
            }else{
                buffer.append(intToString(mValues[i].length()));  //str length
                buffer.append(mValues[i]);
            }
        }
        return buffer.toString();
    }

    private String intToString(int i){
         String str=Integer.toString(i);
         String s=""+str.length();  //i length
         s+=str;
         return s;
    }

    public static LyricsList parseLyrics(String lyrics){
        if(lyrics!=null) {
            CharReader charReader = new CharReader(lyrics.toCharArray());
            int size=charReader.readInt();
            int hitIndex=charReader.readInt();
            int length=charReader.readInt();
            LyricsList lyricsList=new LyricsList(length);
            lyricsList.setHitIndex(hitIndex);
            for (int i=0;i<size;i++){
                lyricsList.put(charReader.readInt(),charReader.readString());
            }
            return lyricsList;
        }
        return null;
    }

    static class CharReader{

        private char[] content;
        private int pos;
        public CharReader(char[] content){
            this.content=content;
            pos=0;
        }

        public int readInt(){
            try {
                int length = content[pos] - '0';
                pos += 1;
                String str = String.copyValueOf(content, pos, length);
                pos+=length;
                return Integer.parseInt(str);
            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }

        public String readString(){
            int length=readInt();
            String str = String.copyValueOf(content, pos, length);
            pos+=length;
            return str;
        }
    }
}
