package com.hxiong.audioplayer.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class PagerTagLayout extends RelativeLayout implements OnPageChangeListener{

	private int mChildCount;  //可见的子控件个数
	private int mTagWidth;   //每个tag控件可用于计算的最大的宽度
	private int mTagHeight;  //每个tag控件可用于计算的最大的高度
	
	//需要关联的ViewPager控件，默认添加了OnPageChangeListener监听器
	private ViewPager mViewPager; 
	//当前选中的tag控件
	private int mSelectedTag; 
	//当前按下的tag控件
	private int mTouchTag;
	//选中的tag颜色
	private int mSelectedColor;
	//按下的颜色
	private int mTouchColor;
	//滚动条的颜色
	private int mScrollBarColor;
	//分割条的颜色
	private int mSparateBarColor;
	//滚动条的高度
	private int mScrollBarHeight;
	//分割条的宽度
	private int mSparateBarWidth;
	//分割条的上下边距
	private int mSparateBarPardding;
	//是否按下tag控件
	private boolean isTouch;
	//滚动条的X轴起始位置
	private float mScrollBarX;
	//绘制的画笔
	private Paint mPaint;
	//
	private OnPageSelectedListener mOnPageSelectedListener;
	
	
	
	public PagerTagLayout(Context context) {
		this(context, null);
	}
	
	public PagerTagLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PagerTagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	/**
	 * 初始化，设置默认值
	 * @date 2016年7月24日 下午10:54:17
	 */
	private void init(){
		mSelectedTag=0;
		mTouchTag=0;
		mSelectedColor=Color.parseColor("#b3f5c1");
		mTouchColor=Color.LTGRAY;
		mScrollBarColor=Color.GREEN;
		mSparateBarColor=Color.WHITE;
		mScrollBarHeight=5;
		mSparateBarWidth=3;
		mSparateBarPardding=1;
		mScrollBarX=0;
		isTouch=false;
		mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
		
		setWillNotDraw(false);  //这个很重要，不然ondraw不会绘制
	}
    
	/**
	 * @param viewPager
	 * @date 2016年7月24日 下午11:02:16
	 */
	public void setViewPager(ViewPager viewPager){
         this.mViewPager=viewPager;
         if(this.mViewPager!=null&&this.mViewPager.getChildCount()==mChildCount){
        	 this.mViewPager.addOnPageChangeListener(this); 
         }
	}

    /**
     *
     * @return 返回保存的ViewPager对象
     */
	public ViewPager getViewPager(){
        return mViewPager;
    }

	/**
	 * @param selectedColor
	 * @date 2016年7月24日 下午11:02:52
	 */
	public void setSelectedColor(int selectedColor) {
		this.mSelectedColor = selectedColor;
		invalidate(); //刷新控件
	}
    /**
     * @param touchColor
     * @date 2016年7月24日 下午11:03:20
     */
	public void setTouchColor(int touchColor) {
		this.mTouchColor = touchColor;
		invalidate(); //刷新控件
	}
    /**
     * @param scrollBarColor
     * @date 2016年7月24日 下午11:03:42
     */
	public void setScrollBarColor(int scrollBarColor) {
		this.mScrollBarColor = scrollBarColor;
		invalidate(); //刷新控件
	}
    /**
     * @param sparateBarColor
     * @date 2016年7月24日 下午11:04:01
     */
	public void setmSparateBarColor(int sparateBarColor) {
		this.mSparateBarColor = sparateBarColor;
		invalidate(); //刷新控件
	}
    /**
     * @param scrollBarHeight
     * @date 2016年7月24日 下午11:04:29
     */
	public void setScrollBarHeight(int scrollBarHeight) {
		this.mScrollBarHeight = scrollBarHeight;
		invalidate(); //刷新控件
	}
    /**
     * @param sparateBarWidth
     * @date 2016年7月24日 下午11:04:47
     */
	public void setSparateBarWidth(int sparateBarWidth) {
		this.mSparateBarWidth = sparateBarWidth;
		invalidate(); //刷新控件
	}
    
	/**
	 * @param listener
	 * @date 2016年7月24日 下午11:58:12
	 */
	public void setOnPageSelectedListener(OnPageSelectedListener listener){
		this.mOnPageSelectedListener=listener;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		 case MotionEvent.ACTION_DOWN:
			 int touchX=(int)(event.getX()-getPaddingLeft());
			 if(touchX>0){ //考虑边距情况
				 mTouchTag=(touchX)/mTagWidth;
				 isTouch=true;
				 invalidate(); //刷新
				 return true;
			 }
			 return false;
		 case MotionEvent.ACTION_UP:
			 isTouch=false;  //无论如何都要清掉背景
			 invalidate(); //刷新
			 int touchUpX=(int)(event.getX()-getPaddingLeft());
			 if(touchUpX>0&&event.getY()<mTagHeight){ //考虑边距情况,考虑释放点是不是在控件上
				 handleTouchTag((touchUpX)/mTagWidth);  //考虑释放点的位置
				 return true;
			 }
		}
		return super.onTouchEvent(event);
	}
	
	private void handleTouchTag(int tagPosition){
		 if(tagPosition==mSelectedTag){  //已经是选中的tag的情况
			 return ;
		 }
		 if(this.mViewPager!=null){
			 this.mViewPager.setCurrentItem(tagPosition);
		 }
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		 //最终要设置的宽度和高度
		 int width=0;
		 int height=0;
		 
		 //需要保存的全局变量赋上初始值
		 mChildCount=0;
		 mTagWidth=0;
		 mTagHeight=0;
		 
		 //
		 final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
	     final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	     final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
	     final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
	     
	     //临时变量
	     int tempWidth;
	     int tempHeight;
	     
	     final int childCount=getChildCount();
	     for(int i=0;i<childCount;i++){
	    	 final View childView=getChildAt(i);
	    	 if(childView.getVisibility()!=View.GONE){ //忽略掉不可见的子控件
	    		 
	    		 //measureChild 很重要，很关键
	    		 measureChild(childView, widthMeasureSpec, heightMeasureSpec);
	    		 LayoutParams params = (LayoutParams) childView.getLayoutParams();
	    		 
	    		 //计算控件的大小
	    		 tempWidth=childView.getMeasuredWidth()+params.leftMargin+params.rightMargin;
	    		 tempHeight=childView.getMeasuredHeight()+params.topMargin+params.bottomMargin;
	    		 
	    		 //记录最大的宽度和高度
	    		 if(tempWidth>mTagWidth){
	    			 mTagWidth=tempWidth;
	    		 }
	    		 if(tempHeight>mTagHeight){
	    			 mTagHeight=tempHeight;
	    		 }
	    		 //记录计算的子控件个数
	    		 mChildCount++;
	    	 }
	     }
	     
	     //先按当前计算得到的控件最大宽度和高度计算，记得加上内边距
	     width=mTagWidth*mChildCount+getPaddingLeft()+getPaddingRight();
	     height=mTagHeight+getPaddingTop()+getPaddingBottom();
	     
	     //如果已经限制了大小，并且给的值大于我们需要的值，则需要重新计算
	     if (widthMode == MeasureSpec.EXACTLY&&widthSize>width) {
	            width = widthSize;
	     }
	     if (heightMode == MeasureSpec.EXACTLY&&heightSize>height) {
	            height = heightSize;
	     }
	    
	    //最后设置控件的大小
	     setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
		//计算后得到的子控件宽度和高度
		final int width=r-l;
		final int height=b-t;
		
		//临时变量
		final int tempWidth=mTagWidth*mChildCount+getPaddingLeft()+getPaddingRight();
		final int tempHeight=mTagHeight+getPaddingTop()+getPaddingBottom();
	     
		//如果不是之前的宽度和高度，需要重新计算一下
		if(width>tempWidth){
			mTagWidth=(width-getPaddingLeft()-getPaddingRight())/mChildCount;
		}
        if(height>tempHeight){
        	mTagHeight=height-getPaddingTop()-getPaddingBottom();
		}
        
        //子控件的最终四个坐标
        int left,right,top,bottom;
        
        int tempLeft=l+getPaddingLeft();//起始位置
        int tempTop=getPaddingTop();//起始位置

        final int childCount=getChildCount();
        for(int i=0;i<childCount;i++){
	    	 final View childView=getChildAt(i);
	    	 if(childView.getVisibility()!=View.GONE){ //忽略掉不可见的子控件
	    		 LayoutParams params = (LayoutParams) childView.getLayoutParams();
	    		 
	    		  left=tempLeft+(mTagWidth-childView.getMeasuredWidth()-params.rightMargin+params.leftMargin)/2;
	    		  right=left+childView.getMeasuredWidth();
	    		  top=tempTop+(mTagHeight-childView.getMeasuredHeight()-params.bottomMargin+params.topMargin)/2;
	    		  bottom=top+childView.getMeasuredHeight();
	    		  tempLeft+=mTagWidth;
	    		  
	    		  //设置子控件位置
	    		  childView.layout(left, top, right, bottom);
	    	 }
        }
	}
    
	@Override
	protected void onDraw(Canvas canvas) {
		//在实际计算位置时，还需要考虑控件的边距
		int height=mTagHeight+getPaddingTop()+getPaddingBottom();
		int paddingLeft=getPaddingLeft();
		float beginX=paddingLeft;
		
		//绘制选中状态
		mPaint.setColor(mSelectedColor);
		beginX=mSelectedTag*mTagWidth+paddingLeft;
		canvas.drawRect(beginX, 0, beginX+mTagWidth, height, mPaint);
		//绘制按下时的状态
		if(isTouch){
			mPaint.setColor(mTouchColor);
			beginX=mTouchTag*mTagWidth+paddingLeft;
			canvas.drawRect(beginX, 0, beginX+mTagWidth, height, mPaint);
		}
		//绘制分割条
		mPaint.setColor(mSparateBarColor);
		for(int i=1;i<mChildCount;i++){
			beginX=i*mTagWidth+paddingLeft;
			canvas.drawRect(beginX, mSparateBarPardding, beginX+mSparateBarWidth, height-2*mSparateBarPardding-mScrollBarHeight, mPaint);
		}
		//绘制滚动条
		mPaint.setColor(mScrollBarColor);
		beginX=mScrollBarX+paddingLeft;
		canvas.drawRect(beginX, height-mScrollBarHeight, beginX+mTagWidth, height, mPaint);
		
		super.onDraw(canvas);
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mScrollBarX=position*mTagWidth+positionOffset*mTagWidth;
        invalidate(); //刷新		
	}

	@Override
	public void onPageSelected(int position) {
		mSelectedTag=position;
		invalidate(); //刷新
		if(mOnPageSelectedListener!=null){  //通知
			this.mOnPageSelectedListener.onPageSelected(position);
		}
	}
	
	/**
	 * 
	 * @author hxiong
	 * @date 2016年7月25日 上午12:00:05
	 */
	public interface OnPageSelectedListener{
		void onPageSelected(int position);
	}
}
