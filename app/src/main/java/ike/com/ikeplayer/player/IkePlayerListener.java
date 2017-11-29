package ike.com.ikeplayer.player;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
* author ike
* create time 17:58 2017/6/1
* function: 自定义播放器接口监听类
**/

public interface IkePlayerListener {
    /**
     * 视频播放准备完成
     * @param iMediaPlayer
     */
     void onPrepared(IMediaPlayer iMediaPlayer);

    /**
     * 视频的宽高发生变化
     * @param width
     * @param height
     */
     void onVideoSizeChanged(int width,int height);

    /**
     * 缓冲进度监听
     * @param percent：缓冲进度
     */
    void onBufferingUpdate(int percent);

    /**
     * 当视频播放取消或是播放完毕的时候
     */
    void onVideoCancleOrComplete(boolean isComplete);

    /**
     * 重新开始跟新进度条
     */
    void resumeProgressUpdate();

    /**
     * 视频加载出错了
     */
    void onError();

    /**
     * 暂停
     */
   // void videoPause();
}
