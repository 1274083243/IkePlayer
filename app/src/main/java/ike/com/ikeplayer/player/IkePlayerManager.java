package ike.com.ikeplayer.player;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

import ike.com.ikeplayer.model.PlayerParamModel;
import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * author ike
 * create time 22:39 2017/5/31
 * function: 视频播放类的管理类
 * 用于视屏播放器的初始化以及各种播放事件的回调监听
 **/

public class IkePlayerManager implements IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnVideoSizeChangedListener {
    private String TAG = "IkePlayerManager";
    private static volatile IkePlayerManager instance;

    /**
     * 外部播放器设置播放状态的监听
     *
     * @param listener
     */
    public void setListener(IkePlayerListener listener) {
        this.listener = listener;
    }

    public IkePlayerListener listener;
    /**
     * 视频播放器
     */
    public AbstractMediaPlayer mediaPlayer;
    /**
     * 视频信息的宽高
     */
    public int vedioHeight;
    public int vedioWidth;
    public static final int STATE_PREPARING = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_ERROR = 2;
    public static final int STATE_COMPLETE = 3;
    public static final int STATE_PAUSE = 4;
    public int current_state = STATE_PREPARING;

    private IkePlayerManager() {

    }

    /**
     * 构造IkePlayerManager的单例
     *
     * @return
     */
    public static IkePlayerManager getInstance() {
        if (instance == null) {
            synchronized (IkePlayerManager.class) {
                if (instance == null) {
                    instance = new IkePlayerManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化播放器信息
     */
    private void initIjkPlayer(PlayerParamModel model) {
        try {
            mediaPlayer = new IjkMediaPlayer();
            //设置音频流的类型
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //是否开启循环
            mediaPlayer.setLooping(model.isLoop);
            //设置播放速度
            ((IjkMediaPlayer) mediaPlayer).setSpeed(model.speed);
            mediaPlayer.setDataSource(model.path);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 准备视频
     *
     * @param speed
     * @param vedioPath
     * @param isLoop
     */
    public void prepare(int speed, String vedioPath, boolean isLoop) {
        vedioHeight = 0;
        vedioHeight = 0;
        PlayerParamModel model = new PlayerParamModel(isLoop, vedioPath, speed);
        initIjkPlayer(model);
    }

    /**
     * 视频缓冲的回调
     *
     * @param iMediaPlayer
     * @param i
     */
    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

        if (listener != null) {
            if (i >= 95) {
                i = 100;
            }
            listener.onBufferingUpdate(i);
        }
    }

    /**
     * 视频播放完成
     *
     * @param iMediaPlayer
     */
    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (listener != null) {
            current_state = STATE_COMPLETE;
            listener.onVideoCancleOrComplete(true);
        }

    }

    /**
     * 视频播放出错
     *
     * @param iMediaPlayer
     * @param i
     * @param i1
     * @return
     */
    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        Log.e(TAG, "onError: ");
        current_state = STATE_ERROR;
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        Log.e(TAG, "onInfo: ");
        return false;
    }

    /**
     * 视频准备完成
     *
     * @param iMediaPlayer
     */
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

        if (listener != null) {
            current_state = STATE_PLAYING;
            listener.onPrepared(iMediaPlayer);
        }
    }

    /**
     * 视频进度拖拽完成
     *
     * @param iMediaPlayer
     */
    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        Log.e(TAG, "onSeekComplete: ");
    }

    /**
     * 视频大小发生改变
     *
     * @param iMediaPlayer
     * @param i
     * @param i1
     * @param i2
     * @param i3
     */
    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        vedioHeight = iMediaPlayer.getVideoHeight();
        vedioWidth = iMediaPlayer.getVideoWidth();
        if (listener != null) {
            listener.onVideoSizeChanged(vedioWidth, vedioHeight);
        }

    }

    public void setDisPlay(Surface disPlay) {
        if (mediaPlayer != null && disPlay == null) {
            mediaPlayer.setSurface(null);
            if (listener != null) {
                listener.onVideoCancleOrComplete(false);
            }
            return;
        }
        if (mediaPlayer != null && disPlay.isValid()) {
            mediaPlayer.setSurface(disPlay);
            if (listener != null && current_state == STATE_PLAYING) {
                listener.resumeProgressUpdate();
            }
        }
    }
}
