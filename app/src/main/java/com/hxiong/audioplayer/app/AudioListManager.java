package com.hxiong.audioplayer.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.hxiong.audioplayer.R;
import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.SpreadLock;
import com.hxiong.audioplayer.widget.SpreadLayout;
import com.hxiong.audioplayer.widget.SpreadLayout.OnSpreadListener;

import java.util.ArrayList;

/**
 * Created by hxiong on 2017/5/8 21:45.
 * Email 2509477698@qq.com
 */

public class AudioListManager implements OnSpreadListener {

    protected static final int ITEM_UN_SELECTED=-1;

     private Context mContext;

     private LinearLayout mLinearLayout;
     private ArrayList<SpreadLayout> mSpreadLayouts;
     private OnPlayItemListener mOnPlayItemListener;
     private SpreadLock mSpreadLock;

     //上一次选择的item
     private int mSelectItem;

     public AudioListManager(Context context, LinearLayout linearLayout){
           this.mContext=context;
           this.mLinearLayout=linearLayout;
           this.mSpreadLayouts=new ArrayList<SpreadLayout>();
           this.mSelectItem=ITEM_UN_SELECTED;
           this.mSpreadLock=new SpreadLock();
     }

    @Override
    public void onSpreadFinished(boolean isLongClicked, int position, AudioEntity audioEntity) {
        if(isLongClicked){  //长按，做特殊处理

        }else{   //点击，开始播放
            onItemSelected(position,audioEntity);
        }
    }

    public void setOnPlayItemListener(OnPlayItemListener listener){
         mOnPlayItemListener=listener;
    }

    public AudioEntity getAudioEntity(int index){
         if(index>=mSpreadLayouts.size()){
             return null;
         }
         SpreadLayout spreadLayout=mSpreadLayouts.get(index);
         return spreadLayout==null?null:spreadLayout.getAudioEntity();
    }

    public void setSelectItem(int playId){
        setSelectItemState(mSelectItem,SpreadLayout.ITEM_STATE_NORMAL);//取消上一次选择
        mSelectItem=playId;
        setSelectItemState(playId,SpreadLayout.ITEM_STATE_SELECT);//设置为选择状态
    }

    public void setItemState(boolean isPlay){
        SpreadLayout spreadLayout=mSpreadLayouts.get(mSelectItem);
        if(spreadLayout!=null){
            if(isPlay){
                spreadLayout.setSelectedState(SpreadLayout.ITEM_STATE_PLAY);//设置为播放状态
            }else{
                spreadLayout.setSelectedState(SpreadLayout.ITEM_STATE_PAUSE);//设置为暂停状态
            }
        }
    }

    private void setSelectItemState(int selectedId,int state){
        if(selectedId!=-1){
            SpreadLayout spreadLayout=mSpreadLayouts.get(selectedId);
            if(spreadLayout!=null) spreadLayout.setSelectedState(state);
        }
    }

    public void  playPreAudio(){
        if(mSpreadLayouts.size()<1){
            return ;
        }
        cancelSpreading();
        SpreadLayout spreadLayout=mSpreadLayouts.get(calculateOrder(false));
        onItemSelected(spreadLayout.getId(),spreadLayout.getAudioEntity());
    }

    public void playNextAudio(){
        if(mSpreadLayouts.size()<1){
            return ;
        }
        cancelSpreading();
        SpreadLayout spreadLayout=mSpreadLayouts.get(calculateOrder(true));
        onItemSelected(spreadLayout.getId(),spreadLayout.getAudioEntity());
    }

    private void cancelSpreading(){
        int lockId=mSpreadLock.getLockId();
        if(lockId!=SpreadLock.SPREAD_UNLOCK){ // spreading now, cancel
            mSpreadLayouts.get(lockId).cancelSpread();
        }
    }

    private int calculateOrder(boolean isAsc){
        if(mSelectItem==ITEM_UN_SELECTED){
            return 0;
        }
        int size=mSpreadLayouts.size()-1;   //max index of array
        int order=isAsc?(mSelectItem+1):(mSelectItem-1);
        order=order<0?size:order;
        order=order>size?0:order;
        return order;
    }

    private void onItemSelected(int selectedItem,AudioEntity audioEntity){
//            if(mSelectItem==position){
//                return ;
//            }
        setSelectItemState(mSelectItem,SpreadLayout.ITEM_STATE_NORMAL);
        mSelectItem=selectedItem;
        mSpreadLayouts.get(mSelectItem).setSelectedState(SpreadLayout.ITEM_STATE_PLAY);
        if(mOnPlayItemListener!=null){
            mOnPlayItemListener.onItemClicked(mSelectItem,audioEntity);
        }
    }

    public void buildAudioList(AudioEntity[] audioEntities){
        if(audioEntities!=null){
            int audioEntitySize=audioEntities.length;
            //create item view
            for(int i=mSpreadLayouts.size();i<audioEntitySize;i++){
                SpreadLayout spreadLayout=(SpreadLayout) LayoutInflater.from(mContext).inflate(R.layout.audio_item_layout,null);
                mSpreadLayouts.add(spreadLayout);
                mLinearLayout.addView(spreadLayout);
                //set listener
                spreadLayout.setOnSpreadListener(this);
                //set manager
                spreadLayout.setLockManager(mSpreadLock);
            }

            //show content on view
            for(int i=0;i<audioEntitySize;i++){
                mSpreadLayouts.get(i).show(i,audioEntities[i]);
            }
            int spreadLayoutSize=mSpreadLayouts.size();
            //hidden more view
            for(int j=audioEntitySize;j<spreadLayoutSize;j++){
                mSpreadLayouts.get(j).hide();
            }
        }
    }

    /**
     *
     */
    public interface OnPlayItemListener{
         void onItemClicked(int playId,AudioEntity audioEntity);
    }
}
