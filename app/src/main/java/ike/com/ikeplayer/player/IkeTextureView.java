package ike.com.ikeplayer.player;

import android.content.Context;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

/**
 * author ike
 * create time 22:32 2017/5/31
 * function: 自定义TextureView以满足屏幕适配的需求(完成屏幕大小与视频大小的适配问题)
 **/

public class IkeTextureView extends TextureView {
    private String Tag = "IkeTextureView";
    private int videoHight;//视频的高
    private int videoWidth;//视频的宽
    private boolean isFull = true;
    private int widthSpecSize;
    private int heightSpecSize;

    public IkeTextureView(Context context) {
        super(context);
    }

    public IkeTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IkeTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int lastWidth = 0;//最终的宽
        int lastHeight = 0;//最终的高
        videoHight = IkePlayerManager.getInstance().vedioHeight;
        videoWidth = IkePlayerManager.getInstance().vedioWidth;
        if (videoHight != 0 && videoWidth != 0) {
            widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            //进行横竖屏幕的适配
            float widthPersent = videoWidth * 1.0f / widthSpecSize;
            float heightPersent = videoHight * 1.0f / heightSpecSize;

            lastWidth = widthSpecSize;
            lastHeight = heightSpecSize;

            //视屏的宽与控件宽的比 大于 视屏的高与控件高的比,进行高度比例压缩
            if (widthPersent > heightPersent) {

                lastHeight = (int) (lastWidth * 1.0f * (videoHight * 1.0f / videoWidth));
            } else {

                lastWidth = (int) (lastHeight * 1.0f * (videoWidth * 1.0f / videoHight));
            }
        }
        setMeasuredDimension(lastWidth, lastHeight);
    }
}
