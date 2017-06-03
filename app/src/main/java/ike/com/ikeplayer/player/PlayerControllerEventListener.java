package ike.com.ikeplayer.player;

/**
* author ike
* create time 15:37 2017/6/2
* function: 控制器的各个事件，比如全屏点击事件等
**/

public interface PlayerControllerEventListener {
    /**
     * 全屏
     */
    void goToFullScreen();

    /**
     * 退出全屏
     */
    void outFullScreen();
    /**
     * 播放或是暂停
     */
    void playOrPause();

    /**
     * 拖拽进度条
     */
    void seekToPosition(int progress);

//    /**
//     * 是否触碰进度条
//     * @param isTouch
//     */
//    void startOrStopTouchSeekBar(boolean isTouch);
}
