package ike.com.ikeplayer.model;

/**
* author ike
* create time 17:01 2017/6/1
* function: 播放器参数model
**/

public class PlayerParamModel {
    /**
     * 是否开启循环
     */
    public boolean isLoop;
    /**
     * 视频播放路径
     */
    public String path;
    /**
     * 播放速度
     */
    public int speed;

    public PlayerParamModel(boolean isLoop, String path, int speed) {
        this.isLoop = isLoop;
        this.path = path;
        this.speed = speed;
    }

    public PlayerParamModel() {
    }
}
