package com.hxiong.audioplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hxiong.audioplayer.adapter.AudioPagerAdapter;
import com.hxiong.audioplayer.app.AudioListManager;
import com.hxiong.audioplayer.app.AudioListManager.OnPlayItemListener;
import com.hxiong.audioplayer.app.AudioPlayerManager;
import com.hxiong.audioplayer.app.AudioPlayerManager.AudioPlayerListener;
import com.hxiong.audioplayer.app.AudioPlayerManager.ConnectionListener;
import com.hxiong.audioplayer.bean.AudioEntity;
import com.hxiong.audioplayer.util.CommonUtils;
import com.hxiong.audioplayer.util.Error;
import com.hxiong.audioplayer.util.SharedPreferencesUtils;
import com.hxiong.audioplayer.view.LyricsView;
import com.hxiong.audioplayer.widget.PagerTagLayout;

public class AudioPlayerActivity extends BaseActivity {

    protected  static final int MSG_BUILD_LIST = 1;
    protected  static final int MSG_PLAYER_START= 2;
    protected  static final int MSG_PLAYER_PAUSE= 3;
    protected  static final int MSG_PLAYER_PRE= 4;
    protected  static final int MSG_PLAYER_NEXT= 5;
    protected  static final int MSG_PLAYER_PLAY = 6;
    protected  static final int MSG_SYNC_PLAYER = 7;
    protected  static final int MSG_SYNC_LYRICS= 8;

    private AudioListManager mAudioListManager;
    private AudioPlayerManager mAudioPlayerManager;
    private TextView mNameView;
    private TextView mArtistView;
    private TextView mPlayingTime;
    private TextView mTotalTime;
    private SeekBar mSeekBar;

    //
    private ImageView mPreButton;
    private ImageView mNextButton;
    private ImageView mPlayButton;

    //
    private LyricsView mLyricsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        SharedPreferencesUtils.get().init(this);
        init();
    }


    private void init(){
        PagerTagLayout mPagerTagLayout=(PagerTagLayout)findViewById(R.id.audio_tag_layout);
        ViewPager viewPager=(ViewPager)findViewById(R.id.audio_content_view);
        AudioPagerAdapter audioPagerAdapter=new AudioPagerAdapter();
        View audioList=getLayoutInflater().inflate(R.layout.pager_audio_list,null);
        View audioLyrics=getLayoutInflater().inflate(R.layout.pager_audio_lyrics,null);
        View audioSetting=getLayoutInflater().inflate(R.layout.pager_audio_settings,null);
        //main layout init
        audioPagerAdapter.addView(audioList);
        audioPagerAdapter.addView(audioLyrics);
        audioPagerAdapter.addView(audioSetting);
        viewPager.setAdapter(audioPagerAdapter);
        mPagerTagLayout.setViewPager(viewPager);

        //init control view
        mNameView=(TextView)findViewById(R.id.audio_view_name);
        mArtistView=(TextView)findViewById(R.id.audio_view_singer);
        mPlayingTime=(TextView)findViewById(R.id.audio_view_start_time);
        mTotalTime=(TextView)findViewById(R.id.audio_view_total_time);
        mSeekBar=(SeekBar)findViewById(R.id.audio_player_seek_bar);

        //
        mPreButton=(ImageView)findViewById(R.id.audio_play_pre_button);
        mNextButton=(ImageView)findViewById(R.id.audio_play_next_button);
        mPlayButton=(ImageView)findViewById(R.id.audio_play_button);

        //set listener
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mPreButton.setOnClickListener(mOnClickListener);
        mNextButton.setOnClickListener(mOnClickListener);
        mPlayButton.setOnClickListener(mOnClickListener);
        ;
        //init view on viewpager
        LinearLayout linearLayout=(LinearLayout)audioList.findViewById(R.id.audio_list_layout);
        mAudioListManager=new AudioListManager(this,linearLayout);
        mAudioListManager.setOnPlayItemListener(mOnPlayItemListener);

        //
        mLyricsView=(LyricsView)audioLyrics.findViewById(R.id.audio_lyrics_view);

        //last we connect audioplayer service
        AudioPlayerManager.get().connect(this,mConnectionListener);
    }

    /**
     *
     */
    private OnPlayItemListener mOnPlayItemListener=new OnPlayItemListener(){

        @Override
        public void onItemClicked(int playId, AudioEntity audioEntity) {
            setPlayerControlInfo(audioEntity);
            Message message=Message.obtain();
            message.what=MSG_PLAYER_START;
            message.arg1=playId;
            mHandler.sendMessage(message);
        }
    };

    private void setPlayerControlInfo(AudioEntity audioEntity){
        mNameView.setText(audioEntity.title);
        mArtistView.setText(audioEntity.artist);
        mTotalTime.setText(CommonUtils.getAudioTime(audioEntity.duration));
    }

    /**
     *
     */
    private OnSeekBarChangeListener mOnSeekBarChangeListener=new OnSeekBarChangeListener(){

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mPlayingTime.setText(CommonUtils.getAudioTime(progress*1000));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
           // printLog("onStartTrackingTouch ");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            setSeekPosition(seekBar.getProgress()*1000);
        }
    };

    /**
     *
     */
    private OnClickListener mOnClickListener=new OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.audio_play_pre_button:
                    mHandler.sendEmptyMessage(MSG_PLAYER_PRE);
                    break;
                case R.id.audio_play_next_button:
                    mHandler.sendEmptyMessage(MSG_PLAYER_NEXT);
                    break;
                case R.id.audio_play_button:
                    mHandler.sendEmptyMessage(MSG_PLAYER_PLAY);
                    break;
                default:   break;
            }
        }
    };

    private ConnectionListener mConnectionListener=new ConnectionListener(){

        @Override
        public void onConnected(AudioPlayerManager manager) {
            mAudioPlayerManager=manager;  //very importance
            mAudioPlayerManager.setAudioPlayerListener(mAudioPlayerListener);
            mHandler.sendEmptyMessage(MSG_BUILD_LIST);
        }

        @Override
        public void onDisconnected() {

        }
    };

    private AudioPlayerListener mAudioPlayerListener=new AudioPlayerListener(){

        @Override
        public void onNotify(int event, String arg0, int arg1, int arg2) {
             switch (event){
                 case AudioPlayerManager.EVENT_TYPE_PREPARE:

                     break;
                 case AudioPlayerManager.EVENT_TYPE_COMPLETION:
                     mHandler.sendEmptyMessage(MSG_SYNC_PLAYER); //sync with player
                     break;
                 case AudioPlayerManager.EVENT_TYPE_SEEK_COMPLETE:

                     break;
                 case AudioPlayerManager.EVENT_TYPE_INFO:

                     break;
                 case AudioPlayerManager.EVENT_TYPE_ERROR:

                     break;
                 case AudioPlayerManager.EVENT_TYPE_SYNC:
                     Message message=Message.obtain();
                     message.what=MSG_SYNC_LYRICS;
                     message.arg1=arg1;
                     message.arg2=arg2;
                     mHandler.sendMessage(message);  //必须在主线程中刷新
                     break;
                 default:  break;
             }
        }
    };

    /**
     *
     */
    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case  MSG_BUILD_LIST:
                    handleBuildAudioList();
                    break;
                case  MSG_PLAYER_START:
                    handlePlayerStart(msg.arg1);
                    break;
                case MSG_PLAYER_PAUSE:
                    handlePlayerPause();
                    break;
                case MSG_PLAYER_PRE:
                    mAudioListManager.playPreAudio();
                    break;
                case MSG_PLAYER_NEXT:
                    mAudioListManager.playNextAudio();
                    break;
                case MSG_PLAYER_PLAY:
                    handlePlayerPlay();
                    break;
                case MSG_SYNC_PLAYER:
                    handleSyncPlayer();
                    break;
                case MSG_SYNC_LYRICS:
                    handleSyncTimeAndLyrics(msg.arg1,msg.arg2);
                    break;
                default:   break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleBuildAudioList(){
        if(mAudioPlayerManager==null){
            printLog("handleBuildAudioList mAudioPlayerManager is not init.");
            return;
        }
        String audioListName=mAudioPlayerManager.getCurAudioListName();
        printLog("getCurAudioListName return "+audioListName);
        if(audioListName!=null){
            AudioEntity[] audioEntities=mAudioPlayerManager.getAudioEntityList(audioListName);
            mAudioListManager.buildAudioList(audioEntities);
            mHandler.sendEmptyMessage(MSG_SYNC_PLAYER); //sync player
        }
    }

    private void handlePlayerStart(int playId){
        if(mAudioPlayerManager==null){
            printLog("handlePlayerStart mAudioPlayerManager is not init.");
            return;
        }
        if (mAudioPlayerManager.getPlayerState()!=AudioPlayerManager.PLAYER_STATE_IDLE){
            if(mAudioPlayerManager.stop()!=Error.RETURN_OK){
                printLog("handlePlayerStart stop fail.");
            }
        }
        //
        if (mAudioPlayerManager.getPlayerState()==AudioPlayerManager.PLAYER_STATE_IDLE){
            if(mAudioPlayerManager.setDataSource(playId)!=Error.RETURN_OK){
                printLog("handlePlayerStart setDataSource fail.");
            }
            if(mAudioPlayerManager.start()!=Error.RETURN_OK){
                printLog("handlePlayerStart start fail.");
            }else{
                setPlayerImageState(true);
                setDurationInfo();
                //播放了下一首，显示歌词
                String lyrics=mAudioPlayerManager.getLyrics();
                mLyricsView.setLyricsText(lyrics);
            }
        }
    }

    private void handlePlayerPlay(){
        if(mAudioPlayerManager==null){
            printLog("handlePlayerPlay mAudioPlayerManager is not init.");
            return;
        }
        int playerState=mAudioPlayerManager.getPlayerState();
        if(playerState==AudioPlayerManager.PLAYER_STATE_IDLE){
            Message message=Message.obtain();
            message.what=MSG_PLAYER_START;
            message.arg1=mAudioPlayerManager.getCurPlayId();
            mHandler.sendMessage(message);
        }else if(playerState==AudioPlayerManager.PLAYER_STATE_PAUSE){
            if(mAudioPlayerManager.start()!=Error.RETURN_OK){
                printLog("handlePlayerPlay start fail.");
            }else{
                setPlayerImageState(true);
            }
        }else if(playerState==AudioPlayerManager.PLAYER_STATE_START){
            mHandler.sendEmptyMessage(MSG_PLAYER_PAUSE);
        }
    }

    private void handlePlayerPause(){
        if(mAudioPlayerManager==null){
            printLog("handlePlayerPause mAudioPlayerManager is not init.");
            return;
        }
        if(mAudioPlayerManager.pause()!=Error.RETURN_OK){
            printLog("handlePlayerStart start fail.");
        }else{
            setPlayerImageState(false);
        }
    }

    private void setSeekPosition(int position){
        if(mAudioPlayerManager==null){
            printLog("setSeekPosition mAudioPlayerManager is not init.");
            return;
        }

        if(mAudioPlayerManager.seekTo(position)!=Error.RETURN_OK){
             printLog("setSeekPosition seekTo fail.");
        }

    }

    private void handleSyncPlayer(){
        if(mAudioPlayerManager==null){
            printLog("handleSyncPlayer mAudioPlayerManager is not init.");
            return;
        }
         int playId=mAudioPlayerManager.getCurPlayId();
         if(playId!= Error.RETURN_ERROR){
              AudioEntity audioEntity=mAudioListManager.getAudioEntity(playId);
             if(audioEntity!=null){
                 setPlayerControlInfo(audioEntity);
                 mAudioListManager.setSelectItem(playId);
                 // 同步歌词
                 String lyrics=mAudioPlayerManager.getLyrics();
                 mLyricsView.setLyricsText(lyrics);
                 int playerState=mAudioPlayerManager.getPlayerState();
                 if(playerState==AudioPlayerManager.PLAYER_STATE_PAUSE){
                     setPlayerImageState(false);
                     setDurationInfo();
                 }else if(playerState==AudioPlayerManager.PLAYER_STATE_START){
                     setPlayerImageState(true);
                     setDurationInfo();
                 }
             }
         }
    }

    private void handleSyncTimeAndLyrics(int position,int lyricIndex){
        mPlayingTime.setText(CommonUtils.getAudioTime(position));
        position/=1000;
        mSeekBar.setProgress(position);
        //歌词
        mLyricsView.setSelectedIndex(lyricIndex);
    }

    private void setDurationInfo(){
        int duration=mAudioPlayerManager.getDuration();
        int position=mAudioPlayerManager.getCurrentPosition();
        mTotalTime.setText(CommonUtils.getAudioTime(duration));
        mPlayingTime.setText(CommonUtils.getAudioTime(position));
        duration/=1000;
        position/=1000;
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(position);
    }

    private void setPlayerImageState(boolean isPlay){
        if(isPlay){
            mPlayButton.setImageResource(R.mipmap.audio_play);
        }else {
            mPlayButton.setImageResource(R.mipmap.audio_stop);
        }
        mAudioListManager.setItemState(isPlay);
    }


    /**
     * 按下返回键时调用，弹出提示框，询问用户是否真的退出
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("提示信息")
                .setIcon(R.mipmap.dialog_icon)
                .setMessage("您要退出当前应用？")
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();   //退出
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAudioPlayerManager!=null)
            mAudioPlayerManager.disconnect();
    }
}
