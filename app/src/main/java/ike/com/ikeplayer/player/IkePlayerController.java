package ike.com.ikeplayer.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ike.com.ikeplayer.R;
import ike.com.ikeplayer.utils.ScreenRotateUtil;

/**
 * author ike
 * create time 15:14 2017/6/2
 * function:播放器的控制器
 **/

public class IkePlayerController extends FrameLayout implements View.OnClickListener {
    public ImageView btn_full;//全屏按钮
    private View view;
    public TextView tv_current_time;//当前播放时间
    public TextView tv_final_time;//视频的总时间
    public SeekBar seek_bar;
    public ImageView iv_pause_play;
    public ImageView iv_cover;//防止在大小屏切换的时候黑屏的顶替图片
    public Bitmap cover_bitmap;
    private LinearLayout ll_controller_container;//底部控制器容器
    private Context context;
    private PlayerControllerEventListener listener;
    private IkePlayer mPlayer;
    private String Tag = "IkePlayerController";
    private HideSeekBarTask hideSeekBarTask;
    private Animation enter_animation;
    private Animation out_animation;
    public ImageView iv_out_small;//退出小屏播放
    private boolean isSeekBarOntouch;//是否seekbar再被拖动,此时ll_controller_container不应该进行下落动画

    public IkePlayerController(@NonNull Context context) {
        this(context, null);
    }

    public IkePlayerController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IkePlayerController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        view = View.inflate(context, R.layout.ike_player_view, this);
        initView();

    }

    private void initView() {
        btn_full = (ImageView) findViewById(R.id.btn_full);
        seek_bar = (SeekBar) findViewById(R.id.seek_bar);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        tv_final_time = (TextView) findViewById(R.id.tv_final_time);
        iv_pause_play = (ImageView) findViewById(R.id.iv_pause_play);
        iv_cover = (ImageView) findViewById(R.id.iv_cover);
        ll_controller_container = (LinearLayout) findViewById(R.id.ll_controller_container);
        iv_out_small= (ImageView) findViewById(R.id.iv_out_small);

        iv_pause_play.setOnClickListener(this);
        btn_full.setOnClickListener(this);
        iv_out_small.setOnClickListener(this);
        seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (listener != null) {
                        listener.seekToPosition(progress);
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarOntouch=true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarOntouch=false;
                removeCallbacks(hideSeekBarTask);
                postDelayed(hideSeekBarTask, 3000);
            }
        });
        hideSeekBarTask = new HideSeekBarTask();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //全屏半屏切换按钮
            case R.id.btn_full:
                if (listener != null) {
                    if (!mPlayer.isFullScreen&&!mPlayer.isSmallScreen) {
                        listener.goToFullScreen();
                    } else if (mPlayer.isFullScreen||mPlayer.isSmallScreen){
                        listener.outFullScreen();
                    }
                }
                break;
            case R.id.iv_pause_play:
                if (listener != null) {
                    if (out_animation!=null){
                        out_animation.cancel();
                    }
                    listener.playOrPause();
                }
                break;
            case R.id.iv_out_small:
                if (listener!=null){
                    listener.outSmallScreen();

                }
                break;
        }
    }


    public void bindPlayer(PlayerControllerEventListener listener, IkePlayer ikePlayer1) {
        this.listener = listener;
        this.mPlayer = ikePlayer1;
    }

    /**
     * 根据播放状态跟新UI
     *
     * @param state
     */
    public void UpDateUIApplyState(int state) {
        switch (state) {
            case IkePlayer.STATE_PAUSE:
            case IkePlayer.STATE_COMPLETE:
            case IkePlayer.STATE_IDLE:
                iv_pause_play.setImageResource(R.drawable.video_play_normal);
                break;
            case IkePlayer.STATE_PLAYING:
                iv_pause_play.setImageResource(R.drawable.video_pause_normal);
                break;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(hideSeekBarTask);
                if (ll_controller_container.getVisibility() == INVISIBLE) {
                    if (out_animation != null) {
                        out_animation.cancel();
                    }
                    if (mPlayer.current_state==IkePlayer.STATE_PLAYING){
                        if (!mPlayer.isSmallScreen){
                            showSeekBarContainer();
                        }else {
                            iv_pause_play.setVisibility(VISIBLE);
                        }
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                    if (mPlayer.current_state == IkePlayer.STATE_PLAYING) {
                        postDelayed(hideSeekBarTask, 3000);
                    }

                break;
        }
        return true;
    }

    /**
     * 显示seekbar的容器
     */
    public void showSeekBarContainer() {
        if (ll_controller_container.getVisibility()==VISIBLE){
            return;
        }
        enter_animation = AnimationUtils.loadAnimation(context, R.anim.transition_enter);
        ll_controller_container.startAnimation(enter_animation);
        enter_animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv_pause_play.setVisibility(VISIBLE);
                ll_controller_container.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void hideSeekBarContainer() {
        out_animation = AnimationUtils.loadAnimation(context, R.anim.transition_out);
        ll_controller_container.startAnimation(out_animation);
        out_animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv_pause_play.setVisibility(GONE);
                ll_controller_container.setVisibility(INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public class HideSeekBarTask implements Runnable {
        @Override
        public void run() {
            if (isSeekBarOntouch){
                return;
            }
            if (!mPlayer.isSmallScreen){
                if (mPlayer.current_state == IkePlayer.STATE_PLAYING) {
                    hideSeekBarContainer();
                }
            }else {
                if (mPlayer.current_state == IkePlayer.STATE_PLAYING) {
                    iv_pause_play.setVisibility(GONE);
                }

            }

        }
    }

    /**
     * 重置状态
     */
    public void reset(){
        iv_cover.setVisibility(VISIBLE);
        iv_pause_play.setVisibility(VISIBLE);
        seek_bar.setProgress(0);
        seek_bar.setSecondaryProgress(0);
        tv_current_time.setText("00:00");
        //隐藏进度条控件
        ll_controller_container.setVisibility(INVISIBLE);
        //重置播放器的状态为空闲状态
        mPlayer.current_state=IkePlayer.STATE_IDLE;
        UpDateUIApplyState(mPlayer.current_state);
    }
}
