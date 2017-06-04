package ike.com.ikeplayer.utils;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.OrientationEventListener;

import ike.com.ikeplayer.player.OritationChangedListener;

public class ScreenRotateUtil {
    private  int lastOritation=0;
    public void setListener(OritationChangedListener listener) {
        if (listener==null){
            screenOritationListener.disable();
        }else {
            screenOritationListener.enable();
        }
        this.listener = listener;
    }
    private OritationChangedListener listener;

    public ScreenRotateUtil(Context context) {
        screenOritationListener=new ScreenOritationListener(context);
        screenOritationListener.enable();
    }

    /**
     * 停止监听屏幕旋转信息
     */
    public void stopGetScreenRotateInfo(){
        screenOritationListener.disable();
    }

    private ScreenOritationListener screenOritationListener;

    class ScreenOritationListener extends OrientationEventListener {

        public ScreenOritationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
//            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
//                return; // 手机平放时，检测不到有效的角度
//            }
            // 只检测是否有四个角度的改变
            if (orientation > 350 || orientation < 10) {
                // 0度：手机默认竖屏状态（home键在正下方）
                sendOritationChenged(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (orientation > 80 && orientation < 100) {
                // 90度：手机顺时针旋转90度横屏（home建在左侧）
                sendOritationChenged(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else if (orientation > 170 && orientation < 190) {
                sendOritationChenged(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                // 手机顺时针旋转180度竖屏（home键在上方）
                orientation = 180;
            } else if (orientation > 260 && orientation < 280) {
                // 手机顺时针旋转270度横屏，（home键在右侧）
                sendOritationChenged(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

        }
    }

    private void sendOritationChenged(int screenOrientationPortrait) {
        if (lastOritation!=screenOrientationPortrait){
            if (listener != null) {
                listener.onScreenOritationChanged(screenOrientationPortrait);
            }
            lastOritation=screenOrientationPortrait;
        }

    }
}