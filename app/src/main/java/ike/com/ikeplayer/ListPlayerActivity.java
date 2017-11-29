package ike.com.ikeplayer;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ike.com.ikeplayer.model.VideoModel;
import ike.com.ikeplayer.player.IkePlayer;
import ike.com.ikeplayer.player.IkePlayerManager;
import ike.com.ikeplayer.player.PlayerStateChangedListener;

/**
* author ike
* create time 17:48 2017/6/10
* function:列表方式的播放界面
**/

public class ListPlayerActivity extends AppCompatActivity{
    private String Tag="ListPlayerActivity";
    private RecyclerView rl;
    private List<VideoModel> datas;
    private int current_position=-1;
    private boolean hasVideoPlay;
    private IkePlayer player;
    private int position;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        rl= (RecyclerView) findViewById(R.id.rl);
        player=new IkePlayer(this);
        datas=new ArrayList<>();
        for (int i=0;i<20;i++){
            VideoModel videoModel=new VideoModel();
            videoModel.isLoop=false;
            videoModel.speed=1;
            videoModel.path="http://baobab.wdjcdn.com/14564977406580.mp4";
            datas.add(videoModel);
        }
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rl.setLayoutManager(linearLayoutManager);
        rl.setAdapter(new ListAdapter());
        addListenser();

    }

    private void addListenser() {
        rl.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstCompletelyVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                //不可见
                if ((current_position<firstCompletelyVisibleItemPosition||current_position>lastVisibleItemPosition)&& player.hasVideoPlay){
                    IkePlayerManager.getInstance().release();
                    current_position=-1;
                    player.hasVideoPlay=false;
                }
            }
        });
    }

    class ListAdapter extends RecyclerView.Adapter{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder1(View.inflate(parent.getContext(),R.layout.item,null));
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder1 mHolder= (ViewHolder1) holder;
            player.setVideoData(datas.get(position))
                    .setStateListener(new PlayerStateChangedListener() {
                        @Override
                        public void onCompletion() {

                        }
                    });
            if (position==player.position){
                mHolder.player_container.removeAllViews();
                mHolder.player_container.addView(player);
                if (position==0){
                    current_position=0;
                }
            }else {
                mHolder.player_container.removeAllViews();
                ImageView imageView=new ImageView(ListPlayerActivity.this);
                imageView.setImageResource(R.mipmap.ic_launcher);
                mHolder.player_container.addView(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FrameLayout parent = (FrameLayout) player.getParent();
                        if (parent!=null){
                            parent.removeAllViews();
                        }
                        mHolder.player_container.removeAllViews();
                        mHolder.player_container.addView(player);
                        player.prepareVideo();
                        player.hasVideoPlay=true;
                        current_position=position;
                        player.setPosition(position);
                        notifyDataSetChanged();
                    }
                });

            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
        class ViewHolder1 extends RecyclerView.ViewHolder{
        private FrameLayout player_container;
        public ViewHolder1(View itemView) {
            super(itemView);
            player_container= (FrameLayout) itemView.findViewById(R.id.player_container);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        if (player!=null&&hasVideoPlay){
            player.isSystemPause=true;
            player.pause();
        }
        super.onPause();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        player.destroy();
        super.onDestroy();
    }
}
