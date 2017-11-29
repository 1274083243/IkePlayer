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
import android.util.Log;
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
import ike.com.ikeplayer.utils.ScreenRotateUtil;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 结合ijkPlayer进行自定义播放器
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class IkePlayer extends FrameLayout implements
        PlayerControllerEventListener,
        IkePlayerListener {
    private String Tag = "IkePlayer";
    private Context context;
    private IkeTextureView mTextureView;//视频播放控件
    private Surface mSurface;
    public FrameLayout mainContainer;//播放控件以及播放器控制控件的容器
    public IkePlayerController mPlayController;//播放器控制者
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
    public boolean isSystemPause;//标记是否是由于activity的生命周期onpause方法引起的暂停，用以防止出现黑屏的现象
    private boolean isFirst = true;//是否是第一次播放视频
    public boolean isSmallScreen;//是否是小屏幕播放
    public boolean hasVideoPlay;//是否有视频在播放
    /**
     * 播放状态
     */
    public static final int STATE_PREPARING = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_ERROR = 2;
    public static final int STATE_COMPLETE = 3;
    public static final int STATE_PAUSE = 4;
    public static final int STATE_IDLE = 5;
    public int current_state = STATE_IDLE;
    public int position;

    public IkePlayer(@NonNull Context context) {
        this(context, null);
    }

    public IkePlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IkePlayer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        addVideoController();
        screenRotateUtil = new ScreenRotateUtil(getContext());
    }

    /**
     * 添加播放器控制控件
     */
    public void addVideoController() {
        FrameLayout.LayoutParams containerParams = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mainContainer = new FrameLayout(context);
        mainContainer.setLayoutParams(containerParams);
        if (mainContainer.getChildCount() != 0) {
            mainContainer.removeAllViews();
            mainContainer.setBackgroundColor(Color.TRANSPARENT);
        }
        //添加播放器控制控件
        if (mPlayController == null) {
            mPlayController = new IkePlayerController(context);
            mPlayController.setVisibility(VISIBLE);
            mPlayController.iv_cover.setImageResource(R.mipmap.ic_launcher);
            FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mainContainer.addView(mPlayController, params);
            mPlayController.bindPlayer(this, this);
        }
        mainContainer.setBackgroundColor(Color.BLACK);

        this.addView(mainContainer);
    }

    /**
     * 添加视频播放控件
     */
    private void addTexture() {
        //添加播放控件
        if (mTextureView == null) {
            mTextureView = new IkeTextureView(context);
            FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            mTextureView.setLayoutParams(params);
        }
        if (mainContainer.getChildAt(0) == mTextureView) {
            mainContainer.removeView(mTextureView);
        }
        mainContainer.addView(mTextureView, 0);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.e(Tag, "onSurfaceTextureAvailable");
                mSurface = new Surface(surface);
                IkePlayerManager.getInstance().setDisPlay(mSurface);

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                IkePlayerManager.getInstance().setDisPlay(null);
                mSurface.release();
                mSurface = null;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });


    }

    /**
     * 准备播放视频
     */
    public IkePlayer prepareVideo() {
        //释放播放器资源
        releaseRecource();
        addTexture();
        //绑定播放器
        mPlayController.iv_cover.setVisibility(GONE);
        IkePlayerManager.getInstance().setIkePlayer(IkePlayer.this);
        IkePlayerManager.getInstance().setListener(IkePlayer.this);
        IkePlayerManager.getInstance().prepare(videoModel.speed, videoModel.path, videoModel.isLoop);
        hasVideoPlay=true;
        return this;

    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        current_state = STATE_PLAYING;
        this.mIMediaPlayer = iMediaPlayer;
        Date date = new Date(mIMediaPlayer.getDuration());
        mPlayController.tv_final_time.setText(dateFormat.format(date));
        updateProgressTask = new UpdateProgressTask();
        updateProgressTask.start();
        mPlayController.UpDateUIApplyState(current_state);
        if (isFirst) {
            mPlayController.showSeekBarContainer();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlayController.hideSeekBarContainer();
                }
            }, 3000);
            isFirst = false;
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlayController.hideSeekBarContainer();
                }
            }, 500);
        }


        // pause();

    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (width != 0 && height != 0) {
            mTextureView.requestLayout();
            if (mSurface != null) {
                IkePlayerManager.getInstance().setDisPlay(mSurface);
            }

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
        if (isComplete) {
            current_state = STATE_COMPLETE;
            mPlayController.showSeekBarContainer();
            mPlayController.UpDateUIApplyState(current_state);
            if (stateListener != null) {
                stateListener.onCompletion();
            }
            Date date = new Date(mIMediaPlayer.getDuration());
            mPlayController.tv_current_time.setText(dateFormat.format(date));
        }
        if (updateProgressTask != null) {
            updateProgressTask.stop();
        }

    }

    @Override
    public void resumeProgressUpdate() {
        updateProgressTask.start();
    }

    @Override
    public void onError() {
        current_state = STATE_ERROR;

    }


    private ScreenRotateUtil screenRotateUtil;

    /**
     * 利用windows层进行全屏显示的效果
     */
    public void goToFullScreen() {
        if (isFullScreen) {
            return;
        }
        screenRotateUtil.setListener(new OritationChangedListener() {
            @Override
            public void onScreenOritationChanged(int oritation) {
                IkePlayerUtils.getAppCompActivity(context).setRequestedOrientation(oritation);

            }
        });
        getCoverBitmap();
        //隐藏状态栏与actionbar，并将屏幕置于横屏状态
        IkePlayerUtils.hideSupportActionBar(context, true, true);
        IkePlayerUtils.getAppCompActivity(context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.removeView(mainContainer);
        //查找windows层的父视图控件，并将mainContainer添加到视图中
        FrameLayout parentLayout = (FrameLayout) IkePlayerUtils.getAppCompActivity(context).findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        // parentLayout.setBackgroundColor(Color.BLACK);
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
        if (!isFullScreen && !isSmallScreen) {
            return;
        }
        screenRotateUtil.setListener(null);
        getCoverBitmap();
        IkePlayerUtils.showSupportActionBar(context, true, true);
        IkePlayerUtils.getAppCompActivity(context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //查找windows层的父视图控件，并将mainContainer移除
        FrameLayout parentLayout = (FrameLayout) IkePlayerUtils.getAppCompActivity(context).findViewById(android.R.id.content);
        // parentLayout.setBackgroundColor(Color.TRANSPARENT);
        parentLayout.removeView(mainContainer);
        FrameLayout.LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        this.addView(mainContainer, params);
        showCoverImage();
        mPlayController.btn_full.setImageResource(R.drawable.video_enlarge);
        mPlayController.iv_out_small.setVisibility(GONE);
        isFullScreen = false;
        isSmallScreen = false;
    }

    /**
     * 小屏播放
     */
    public void goToSmallScreen() {
        this.removeView(mainContainer);
        FrameLayout parentLayout = (FrameLayout) IkePlayerUtils.getAppCompActivity(context).findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new LayoutParams(getContext().getResources().getDisplayMetrics().widthPixels* 2/ 3, getContext().getResources().getDisplayMetrics().heightPixels / 3);
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        params.rightMargin = 20;
        params.bottomMargin = 10;
        mPlayController.iv_out_small.setVisibility(VISIBLE);
        parentLayout.addView(mainContainer, params);
        isSmallScreen = true;
    }

    Animation animation;

    private void showCoverImage() {
        if (mPlayController.cover_bitmap != null && current_state == STATE_PAUSE) {
            mPlayController.iv_cover.setImageBitmap(mPlayController.cover_bitmap);
            mPlayController.iv_cover.setVisibility(VISIBLE);
        }
    }

    @Override
    public void playOrPause() {
        switch (current_state) {
            case STATE_PAUSE:
                start();
                break;
            case STATE_PLAYING:
                pause();
                break;
            case STATE_COMPLETE:
            case STATE_PREPARING:
            case STATE_IDLE:
                prepareVideo();
                break;
        }
    }

    @Override
    public void seekToPosition(int progress) {
        long position = (long) (progress * 1.0f / 100 * mIMediaPlayer.getDuration());
        mIMediaPlayer.seekTo(position);
    }

    @Override
    public void outSmallScreen() {
        outFullScreen();
    }

    /**
     * 生成视屏缩略图，用以填补视屏旋转后黑屏的问题
     */
    public void getCoverBitmap() {
        if (current_state == STATE_PAUSE) {
            //重新生成bitmap
            if (mTextureView != null && shouldGetNewCover()) {
                mPlayController.cover_bitmap = mTextureView.getBitmap(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

    }

    /**
     * 判断视频旋转后是否继续播放过
     *
     * @return
     */
    public boolean shouldGetNewCover() {
        long current_position = mIMediaPlayer.getCurrentPosition();
        if (current_position != lastPlayPosition) {
            lastPlayPosition = current_position;
            return true;
        }
        lastPlayPosition = current_position;
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
        current_state = STATE_PLAYING;
        mPlayController.UpDateUIApplyState(current_state);
        mPlayController.hideSeekBarContainer();
        if (mPlayController.iv_cover.getVisibility() == VISIBLE) {
            if (animation == null) {
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
        current_state = STATE_PAUSE;
        mPlayController.UpDateUIApplyState(current_state);
        mPlayController.showSeekBarContainer();
        if (isSystemPause) {
            //防止暂停后出现黑屏的现象
            getCoverBitmap();
            showCoverImage();
            isSystemPause = false;
        }
    }

    /**
     * 释放播放器资源
     */
    public void destroy() {
        if (mIMediaPlayer != null) {
            releaseRecource();
            mIMediaPlayer = null;
            if (mPlayController.cover_bitmap != null && !mPlayController.cover_bitmap.isRecycled()) {
                mPlayController.cover_bitmap.recycle();
                mPlayController.cover_bitmap = null;
            }
            Log.e(Tag,"资源释放完毕");

        }
    }

    /**
     * 设置视屏播放数据
     *
     * @param videoData
     */
    public IkePlayer setVideoData(VideoModel videoData) {
        this.videoModel = videoData;
        if (videoData == null) {
            Log.e(Tag, "videoData==null");
        }
        return this;
    }

    /**
     * 设置外部播放器状态监听
     *
     * @param stateListener
     */
    public IkePlayer setStateListener(PlayerStateChangedListener stateListener) {
        this.stateListener = stateListener;
        return this;
    }

    /**
     * 释放播放器资源
     */
    public void releaseRecource() {
        IkePlayerManager.getInstance().release();
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(null);
            mainContainer.removeView(mTextureView);
            mTextureView = null;
        }
        if (mPlayController != null) {
            if (mPlayController.cover_bitmap != null && !mPlayController.cover_bitmap.isRecycled()) {
                mPlayController.cover_bitmap.recycle();
                mPlayController.cover_bitmap = null;
            }
            mPlayController.reset();
        }
        current_state = IkePlayer.STATE_IDLE;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
