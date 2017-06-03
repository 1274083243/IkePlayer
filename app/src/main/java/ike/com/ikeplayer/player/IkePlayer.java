package ike.com.ikeplayer.player;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.Date;

import ike.com.ikeplayer.R;
import ike.com.ikeplayer.model.VideoModel;
import ike.com.ikeplayer.utils.IkePlayerUtils;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 结合ijkPlayer进行自定义播放器
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class IkePlayer extends FrameLayout implements
        PlayerControllerEventListener,
        IkePlayerListener,
        TextureView.SurfaceTextureListener {
    private String Tag = "IkePlayer";
    private Context context;
    private IkeTextureView mTextureView;//视频播放控件
    private Surface mSurface;
    private FrameLayout mainContainer;//播放控件以及播放器控制控件的容器
    private IkePlayerController mPlayController;//播放器控制者
    private AudioManager audioManager;
    private VideoModel videoModel;
    public boolean isFullScreen;//标记是否是全屏播放中
    private SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private Handler handler = new Handler();
    private IMediaPlayer mIMediaPlayer;
    private UpdateProgressTask updateProgressTask;
    private boolean shouldGetNewCover;//是否该重新生成视频背景缩略图
    private long lastPlayPosition;
    private PlayerStateChangedListener stateListener;
    public void setStateListener(PlayerStateChangedListener stateListener) {
        this.stateListener = stateListener;
    }


    public IkePlayer(@NonNull Context context) {
        this(context, null);
    }

    public IkePlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IkePlayer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        addTexture();

    }


    /**
     * 添加视频播放控件
     */
    private void addTexture() {
        if (getChildCount() != 0) {
            removeAllViews();
        }
        FrameLayout.LayoutParams containerParams = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mainContainer = new FrameLayout(context);
        mainContainer.setLayoutParams(containerParams);
        if (mainContainer.getChildCount() != 0) {
            mainContainer.removeAllViews();
        }
        //添加播放控件
        if (mTextureView == null) {
            mTextureView = new IkeTextureView(context);
            mTextureView.setSurfaceTextureListener(this);
            FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            mTextureView.setLayoutParams(params);
            mainContainer.addView(mTextureView);
        }
        //添加播放器控制控件
        if (mPlayController == null) {
            mPlayController = new IkePlayerController(context);
            FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mainContainer.addView(mPlayController, params);
            mPlayController.bindPlayer(this, this);
        }
        this.addView(mainContainer);
    }

    /**
     * 准备播放视频
     */
    public void prepareVideo(VideoModel videoModel) {
      //  addTexture();
        this.videoModel = videoModel;
        IkePlayerManager.getInstance().setListener(this);
        IkePlayerManager.getInstance().prepare(videoModel.speed, videoModel.path, videoModel.isLoop);

    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        this.mIMediaPlayer = iMediaPlayer;
        Date date = new Date(mIMediaPlayer.getDuration());
        mPlayController.tv_final_time.setText(dateFormat.format(date));
        updateProgressTask = new UpdateProgressTask();
        updateProgressTask.start();
        mPlayController.UpDateUIApplyState(IkePlayerManager.getInstance().current_state);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mPlayController.hideSeekBarContainer();
            }
        },500);

       // pause();

    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (width != 0 && height != 0) {
            mTextureView.requestLayout();
            IkePlayerManager.getInstance().setDisPlay(mSurface);
        }
    }

    @Override
    public void onBufferingUpdate(final int percent) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mPlayController.seek_bar.setSecondaryProgress(percent);
            }
        });

    }
    @Override
    public void onVideoCancleOrComplete(boolean isComplete) {
        if (isComplete){
            mPlayController.showSeekBarContainer();
            mPlayController.UpDateUIApplyState(IkePlayerManager.getInstance().current_state);
            if (stateListener!=null){
                stateListener.onCompletion();
            }
        }
        Date date = new Date(mIMediaPlayer.getDuration());
        mPlayController.tv_current_time.setText(dateFormat.format(date));
        updateProgressTask.stop();
    }

    @Override
    public void resumeProgressUpdate() {
        updateProgressTask.start();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        IkePlayerManager.getInstance().setDisPlay(mSurface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        IkePlayerManager.getInstance().setDisPlay(null);
        surface.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 利用windows层进行全屏显示的效果
     */
    public void goToFullScreen() {
        if (isFullScreen) {
            return;
        }
        getCoverBitmap();
        //隐藏状态栏与actionbar，并将屏幕置于横屏状态
        IkePlayerUtils.hideSupportActionBar(context, true, true);
        IkePlayerUtils.getAppCompActivity(context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.removeView(mainContainer);
        //查找windows层的父视图控件，并将mainContainer添加到视图中
        FrameLayout parentLayout = (FrameLayout) IkePlayerUtils.getAppCompActivity(context).findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        parentLayout.setBackgroundColor(Color.BLACK);
        params.gravity = Gravity.CENTER;
        parentLayout.addView(mainContainer, params);
        showCoverImage();
        mPlayController.btn_full.setImageResource(R.drawable.video_shrink);
        isFullScreen = true;

    }

    /**
     * 退出全屏
     */
    public void outFullScreen() {
        if (!isFullScreen) {
            return;
        }
        getCoverBitmap();
        IkePlayerUtils.showSupportActionBar(context, true, true);
        IkePlayerUtils.getAppCompActivity(context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //查找windows层的父视图控件，并将mainContainer移除
        FrameLayout parentLayout = (FrameLayout) IkePlayerUtils.getAppCompActivity(context).findViewById(android.R.id.content);
        parentLayout.setBackgroundColor(Color.TRANSPARENT);
        parentLayout.removeView(mainContainer);
        FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        this.addView(mainContainer, params);
        showCoverImage();
        mPlayController.btn_full.setImageResource(R.drawable.video_enlarge);
        isFullScreen = false;
    }
    Animation animation;
    private void showCoverImage() {
        if (mPlayController.cover_bitmap!=null&&IkePlayerManager.getInstance().current_state==IkePlayerManager.STATE_PAUSE){
            mPlayController.iv_cover.setImageBitmap(mPlayController.cover_bitmap);
            mPlayController.iv_cover.setVisibility(VISIBLE);
        }
    }

    @Override
    public void playOrPause() {
        switch (IkePlayerManager.getInstance().current_state) {
            case IkePlayerManager.STATE_PAUSE:
                start();
                break;
            case IkePlayerManager.STATE_PLAYING:
                pause();
                break;
            case IkePlayerManager.STATE_COMPLETE:
                prepareVideo(videoModel);
                break;
        }
    }

    @Override
    public void seekToPosition(int progress) {
        long position= (long) (progress*1.0f/100*mIMediaPlayer.getDuration());
        mIMediaPlayer.seekTo(position);
    }

    /**
     * 生成视屏缩略图，用以填补视屏旋转后黑屏的问题
     */
    public void getCoverBitmap() {
        if (IkePlayerManager.getInstance().current_state==IkePlayerManager.STATE_PAUSE){
            //重新生成bitmap
            if (mTextureView!=null&&shouldGetNewCover()){
                mPlayController.cover_bitmap=mTextureView.getBitmap(mTextureView.getWidth(),mTextureView.getHeight());
            }
        }

    }

    /**
     * 判断视频旋转后是否继续播放过
     * @return
     */
    public boolean  shouldGetNewCover(){
        long current_position=mIMediaPlayer.getCurrentPosition();
        if (current_position!=lastPlayPosition){
            lastPlayPosition=current_position;
            return true;
        }
        lastPlayPosition=current_position;
        return false;

    }

    class UpdateProgressTask implements Runnable {
        public void start() {
            stop();
            handler.post(this);
        }
        public void stop() {
            handler.removeCallbacks(this);
        }

        @Override
        public void run() {
            Date date = new Date(mIMediaPlayer.getCurrentPosition());
            mPlayController.tv_current_time.setText(dateFormat.format(date));
            int percent = (int) (mIMediaPlayer.getCurrentPosition() * 1.0f / mIMediaPlayer.getDuration() * 100);
            mPlayController.seek_bar.setProgress(percent);
            handler.postDelayed(this, 1000);
        }
    }

    public void start() {
        mIMediaPlayer.start();
        IkePlayerManager.getInstance().current_state = IkePlayerManager.STATE_PLAYING;
        mPlayController.UpDateUIApplyState(IkePlayerManager.getInstance().current_state);
        mPlayController.hideSeekBarContainer();
        if (mPlayController.iv_cover.getVisibility()==VISIBLE){
            if (animation==null){
                animation = AnimationUtils.loadAnimation(context, R.anim.alpha_animation);
            }
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mPlayController.iv_cover.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mPlayController.iv_cover.startAnimation(animation);
        }


    }

    public void pause() {
        mIMediaPlayer.pause();
        IkePlayerManager.getInstance().current_state = IkePlayerManager.STATE_PAUSE;
        mPlayController.UpDateUIApplyState(IkePlayerManager.getInstance().current_state);
        mPlayController.showSeekBarContainer();
        if(mPlayController.iv_cover.getVisibility()==VISIBLE){
        if (animation==null){
            animation = AnimationUtils.loadAnimation(context, R.anim.alpha_animation);
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayController.iv_cover.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPlayController.iv_cover.startAnimation(animation);
        }
    }

}
