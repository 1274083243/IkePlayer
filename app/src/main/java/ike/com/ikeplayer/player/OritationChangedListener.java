package ike.com.ikeplayer.player;

/**
* author ike
* create time 16:17 2017/6/4
* function: 屏幕旋转监听
**/
public interface OritationChangedListener {
    /**
     * 屏幕方向变化的监听
     * @param oritation 当前屏幕的方向类型
     */
   void onScreenOritationChanged(int oritation);
}
