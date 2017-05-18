package com.hxiong.audioplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hxiong.audioplayer.util.LyricsList;

/**
 * Created by hxiong on 2017/5/17 22:12.
 * Email 2509477698@qq.com
 */

public class LyricsView extends View {

    protected  static final String NO_LYRICS_TEXT="未找到本地歌词";

    protected  static final int ROW_SPACING=15;//row spacing
    protected  static final int MARGIN_SPACING=48;//margin spacing

    //默认的颜色
    private int mNormalColor;
    //高亮时的颜色
    private int mSelectedColor;
    //
    private float mNormalTextSize;
    //
    private float mSelectedTextSize;
    //
    private Paint mPaint;
    //保存的歌词
    private LyricsList mLyricsList;
    //用于计算字符串的范围
    private Rect mTextRect;

    public LyricsView(Context context) {
        this(context, null);
    }

    public LyricsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mNormalColor= Color.GRAY;
        mSelectedColor = Color.DKGRAY;
        mNormalTextSize = 40;
        mSelectedTextSize = 52;
        mTextRect = new Rect();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setTextSize(mNormalTextSize);
        mPaint.getTextBounds("a", 0, 1, mTextRect);
        Log.d("LyricsView", "height:" + mTextRect.height() + "width:" + mTextRect.width());
    }

    public void setLyricsText(String lyrics){
        mLyricsList=null; //for gc ??
        mLyricsList=LyricsList.parseLyrics(lyrics);
        invalidate();  //刷新一下
    }

    public void setSelectedIndex(int index){
        if(index<0||mLyricsList==null||index>mLyricsList.size())  //index == size时，相当于播放到最后面了。
            return ;
        mLyricsList.setHitIndex(index);
        invalidate(); //刷新
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width=getWidth();
        int height=getHeight();
        if(width<1&&height<1) return ; //可能为了防止width 为0
        if(mLyricsList!=null){
             int selectedIndex=mLyricsList.getHitIndex();  //
             drawPriorLyrics(canvas,selectedIndex,width,height/2); //二分之一位置上面绘制

             int laterTop=drawSelectedLyric(canvas,selectedIndex,width,height/2); //

             drawLaterLyrics(canvas,selectedIndex,width,height,laterTop);
        }else{
            drawNoLyrics(canvas,width,height);
        }
    }

    private void drawPriorLyrics(Canvas canvas,int sparateIndex,int width,int bottom){
        mPaint.setColor(mNormalColor);
        mPaint.setTextSize(mNormalTextSize);
        int left ;
        int top = bottom-ROW_SPACING;
        for(int i=sparateIndex-1;i>-1;i--){  //从下往上绘制
            String lyric=mLyricsList.valueAt(i);
            mPaint.getTextBounds(lyric, 0, lyric.length(), mTextRect);
            top-=ROW_SPACING; //行距
            if(top<MARGIN_SPACING) break;
            if(mTextRect.width()>width){   //最多绘制两行
                int wordCount=lyric.length();
                int count=width*wordCount/mTextRect.width();  //一行的字符个数
                int wordWidth=mTextRect.width()/wordCount;    //一个字符的宽度
                count=count<wordCount?count:wordCount;  //防止数组越界
                left = (width -(wordCount-count+2)*wordWidth) / 2;
                top -= mTextRect.height();
                canvas.drawText(lyric.substring(count+2), left, top, mPaint);  //count +2 为了预留 一些空间
                top-=ROW_SPACING; //行距
                if(top<MARGIN_SPACING) break;  //
                top -= mTextRect.height();
                canvas.drawText(lyric.substring(0,count-3), wordWidth, top, mPaint);  //count -3 为了预留 一些空间
            }else {
                left = (width - mTextRect.width()) / 2;
                top -= mTextRect.height();
                canvas.drawText(lyric, left, top, mPaint);
            }
        }
    }

    private int drawSelectedLyric(Canvas canvas,int sparateIndex,int width,int top){
        if(sparateIndex>-1&&sparateIndex<mLyricsList.size()){
            mPaint.setColor(mSelectedColor);
            mPaint.setTextSize(mSelectedTextSize);
            String lyric=mLyricsList.valueAt(sparateIndex);
            mPaint.getTextBounds(lyric, 0, lyric.length(), mTextRect);
            top=drawLyric(canvas,lyric,mTextRect.width(),mTextRect.height(),width,top);
            return top;
        }
        return 0;
    }

    private void drawLaterLyrics(Canvas canvas,int sparateIndex,int width,int height,int top){
        mPaint.setColor(mNormalColor);
        mPaint.setTextSize(mNormalTextSize);
        int size=mLyricsList.size();
        for(int i=sparateIndex+1;i<size;i++){   //从上往下绘制
            String lyric=mLyricsList.valueAt(i);
            mPaint.getTextBounds(lyric, 0, lyric.length(), mTextRect);
            top=drawLyric(canvas,lyric,mTextRect.width(),mTextRect.height(),width,top);
            if(height-top<MARGIN_SPACING) break;
        }
    }

    private int drawLyric(Canvas canvas,String lyric,int lyricWidth,int lyricHeight,int width,int top){
        int left;
        if(lyricWidth>width){   //最多绘制两行
            int wordCount=lyric.length();
            int count=width*wordCount/lyricWidth;  //一行的字符个数
            int wordWidth=lyricWidth/wordCount;    //一个字符的宽度
            count=count<wordCount?count:wordCount;  //防止数组越界
            canvas.drawText(lyric.substring(0,count-3), wordWidth, top, mPaint);  //count -3 为了预留 一些空间
            top += lyricHeight;  //行高
            top+=ROW_SPACING; //行距
            left = (width -(wordCount-count+2)*wordWidth) / 2;
            canvas.drawText(lyric.substring(count+2), left, top, mPaint);  //count +2 为了预留 一些空间
        }else {
            left = (width - lyricWidth) / 2;
            canvas.drawText(lyric, left, top, mPaint);
        }
        top += lyricHeight;  //行高
        top +=ROW_SPACING; //行距
        return top;
    }

    private void drawNoLyrics(Canvas canvas,int width,int height){
        mPaint.setTextSize(mSelectedTextSize);
        mPaint.setColor(mSelectedColor);
        mPaint.getTextBounds(NO_LYRICS_TEXT, 0, NO_LYRICS_TEXT.length(), mTextRect);
        //不考虑显示的宽度会超过控件宽度的情况
        int left=(width-mTextRect.width())/2;
        int top=height/2;   //高度二分之一位置
        canvas.drawText(NO_LYRICS_TEXT,left,top,mPaint);
    }
}
