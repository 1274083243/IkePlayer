package ike.com.ikeplayer;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


import ike.com.ikeplayer.model.VideoModel;
import ike.com.ikeplayer.player.IkePlayer;
import ike.com.ikeplayer.player.PlayerStateChangedListener;
import ike.com.ikeplayer.utils.ScreenRotateUtil;

public class MainActivity extends AppCompatActivity {
    private IkePlayer mPlayer;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayer= (IkePlayer) findViewById(R.id.player);
        start();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void start(){
        VideoModel vedioModel=new VideoModel();
        vedioModel.isLoop=false;
        vedioModel.speed=1;
        vedioModel.path="http://baobab.wdjcdn.com/14564977406580.mp4";
        mPlayer.setVideoData(vedioModel)
                .setStateListener(new PlayerStateChangedListener() {
            @Override
            public void onCompletion() {

            }
        });
    }

    /**
     * 小屏幕播放
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void goSmallScreen(View view){
        if (!mPlayer.isSmallScreen){
            mPlayer.goToSmallScreen();
        }else {
            mPlayer.outFullScreen();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        if (mPlayer!=null){
            mPlayer.isSystemPause=true;
            mPlayer.pause();
        }

        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        mPlayer.destroy();
        super.onDestroy();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void player_by_list(View view){
        Intent intent=new Intent(this,ListPlayerActivity.class);
        startActivity(intent);
        mPlayer.destroy();
        mPlayer=null;
    }
}
