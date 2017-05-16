package com.hxiong.audioplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hxiong.audioplayer.R;
import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.CommonUtils;
import com.hxiong.audioplayer.util.LockManager;

/**
 * Created by hxiong on 2017/5/8 20:46.
 * Email 2509477698@qq.com
 */

public class SpreadLayout extends RelativeLayout {

    public  static final int ITEM_STATE_PLAY= 0;
    public  static final int ITEM_STATE_PAUSE = 1;
    public  static final int ITEM_STATE_NORMAL = 2;

    protected  static final int REFRESH_DELAY = 8;
    protected  static final int REFRESH_COUNT = 50;
    protected  static final int TOUCH_EDGE = 3;

    //选中时，背景颜色
    private int mSelectedColor;
    //扩散效果的颜色
    private int mSpreadColor;
    //按下时的x轴位置
    private float mTouchX;
    //按下时的Y轴位置
    private float mTouchY;
    //向左边递减
    private float deX;
    //向右边递加
    private float inX;
    //是否是选中状态
    private boolean isSelected;
    //是否按下过
    private boolean hasTouch;
    //是否符合点击事件
    private boolean isClicked;

    //是否处于绘制扩散效果
    private boolean isSpreading;
    //画笔
    private Paint mPaint;
    //刷新次数统计
    private int mRefreshCount;

    private int mId;
    private AudioEntity mAudioEntity;
    private OnSpreadListener mOnSpreadListener;
    private LockManager mLockManager;

    //
    private boolean isInitialized;
    private ImageView mAudioState;
    private TextView mAudioId;
    private TextView mAudioArtist;
    private TextView mAudioInfo;
    private TextView mAudioTime;


    public SpreadLayout(Context context) {
        this(context, null);
    }

    public SpreadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpreadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mSelectedColor = Color.LTGRAY;
        mSpreadColor = Color.parseColor("#FFB7BDB7");
        isSelected=false;
        isSpreading=false;
        hasTouch=false;
        isClicked=false;
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        isInitialized=false;
        setWillNotDraw(false);  //这个很重要，不然onDraw不会调用
    }


    public void show(int id,AudioEntity audioEntity){
        this.mId=id;
        this.mAudioEntity=audioEntity;
        initAudioItem();
        this.setVisibility(View.VISIBLE);  //visible
    }

    public void hide(){
         this.setVisibility(View.GONE);
    }

    public void setOnSpreadListener(OnSpreadListener listener){
        mOnSpreadListener=listener;
    }

    public void setLockManager(LockManager manager){
        mLockManager=manager;
    }

    public int getId(){
        return mId;
    }

    public AudioEntity getAudioEntity(){
        return mAudioEntity;
    }

    public void setSelectedState(int state){
        switch (state){
            case ITEM_STATE_PLAY:
                mAudioState.setImageResource(R.mipmap.audio_state_play);
                seStateVisible(true);
                break;
            case ITEM_STATE_PAUSE:
                mAudioState.setImageResource(R.mipmap.audio_state_stop);
                seStateVisible(true);
                break;
            default:     //default is normal
                seStateVisible(false);
                break;
        }
    }

    private void seStateVisible(boolean isVisible){
        mAudioState.setVisibility(isVisible?VISIBLE:INVISIBLE);
        mAudioId.setVisibility(isVisible?INVISIBLE:VISIBLE);
        isSelected=isVisible;
        invalidate();  //刷新一下哦
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(holdSpreadLock()&&!isSpreading){
                    mTouchX=event.getX();
                    mTouchY=event.getY();
                    //先计算好，每次需要递加的幅度
                    deX=mTouchX/REFRESH_COUNT;
                    inX=((float)getWidth()-mTouchX)/REFRESH_COUNT;
                    mRefreshCount=0;
                    hasTouch=true; //标识按下过
                    isClicked=false;
                    isSpreading=true;
                    invalidate(); //刷新
                }
                break;
            case MotionEvent.ACTION_UP:
                if(hasTouch){   //必须是按下过释放才有效,并且在对应控件上释放
                     if(!isSpreading){  //如果扩散效果绘制完毕，属于长按
                         notifyListener(true);
                     }
                    isClicked=true;
                }
                if(!isSpreading){
                    releaseSpreadLock(); //记得释放
                    invalidate();  //记得刷新一下
                }
                hasTouch=false; // had release
                break;
            case MotionEvent.ACTION_MOVE:
                if(hasTouch&&isOutside(event.getX(),event.getY())){
                    cancelSpread();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelSpread();
                break;
            default:   break;
        }
        return true;
    }

    private boolean isOutside(float x,float y){
        boolean isOutside=x<0||x>getWidth()||y<0||y>getHeight();
        boolean isOutEdge=Math.abs(x-mTouchX)>TOUCH_EDGE||Math.abs(y-mTouchY)>TOUCH_EDGE;
        return isOutside||isOutEdge;
    }


    private void notifyListener(boolean isLongClicked){
        if(mOnSpreadListener!=null){
            mOnSpreadListener.onSpreadFinished(isLongClicked,mId,mAudioEntity);
        }
    }

    /**
     * very importance function
     */
    public void cancelSpread(){
        isSpreading=false;
        hasTouch=false;
        isClicked=false;
        releaseSpreadLock(); //记得释放
        invalidate();  //记得刷新一下
    }

    private boolean holdSpreadLock(){
        return mLockManager==null?true:mLockManager.holdLock(mId);
    }

    private void releaseSpreadLock(){
        if(mLockManager!=null)
            mLockManager.releaseLock(mId);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isSelected){
            mPaint.setColor(mSelectedColor);
            canvas.drawRect(0,0,getWidth(),getHeight(),mPaint);
        }
        if(isSpreading){
            mPaint.setColor(mSpreadColor);
            canvas.drawRect(0,0,getWidth(),getHeight(),mPaint);  //绘制背景
            mRefreshCount++;
            mPaint.setColor(mSelectedColor);
            float left=mTouchX-deX*mRefreshCount;
            float right=mTouchX+inX*mRefreshCount;
            canvas.drawRect(left,0,right,getHeight(),mPaint);
            if(mRefreshCount<REFRESH_COUNT){
                postInvalidateDelayed(REFRESH_DELAY); //继续刷新
            }else{    //达到对应的次数了，扩散效果绘制完毕
                isSpreading=false;   //绘制完毕
                if(isClicked){    //属于点击事件，不算长按
                    notifyListener(false);
                }
                if(!hasTouch){
                    releaseSpreadLock(); //记得释放
                    invalidate();  //记得刷新一下
                }
            }
        }
    }

    private void initAudioItem(){
        if(!isInitialized){
            mAudioState = (ImageView)findViewById(R.id.audio_state_image);
            mAudioId = (TextView)findViewById(R.id.audio_id_text);
            mAudioArtist = (TextView)findViewById(R.id.audio_item_name_text);
            mAudioInfo = (TextView)findViewById(R.id.audio_item_info_text);
            mAudioTime = (TextView)findViewById(R.id.audio_item_time_text);
            isInitialized=true;
        }
        mAudioId.setText(idToString());
        mAudioArtist.setText(mAudioEntity.name);
        mAudioInfo.setText(mAudioEntity.artist+" | "+mAudioEntity.album);
        mAudioTime.setText(CommonUtils.getAudioTime(mAudioEntity.duration));
    }


    private String idToString(){
        if(mId<10){
            return "00"+mId;
        }else if(mId<100){
            return "0"+mId;
        }else{
            return ""+mId;
        }
    }

    /**
     *
     */
    public interface OnSpreadListener{
        void onSpreadFinished(boolean isLongClicked,int position,AudioEntity audioEntity);
    }
}
