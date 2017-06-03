package ike.com.ikeplayer;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


import ike.com.ikeplayer.model.VideoModel;
import ike.com.ikeplayer.player.IkePlayer;
import ike.com.ikeplayer.player.PlayerStateChangedListener;

public class MainActivity extends AppCompatActivity {
    private IkePlayer mPlayer;
    private ImageView iv_pause_play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayer= (IkePlayer) findViewById(R.id.player);

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void start(View view){
        VideoModel vedioModel=new VideoModel();
        vedioModel.isLoop=false;
        vedioModel.speed=1;
        vedioModel.path="http://baobab.wdjcdn.com/14564977406580.mp4";
        mPlayer.prepareVideo(vedioModel);
        mPlayer.setStateListener(new PlayerStateChangedListener() {
            @Override
            public void onCompletion() {

            }
        });
    }
}
