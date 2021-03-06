package com.udacity.baking.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.udacity.baking.MainActivity;
import com.udacity.baking.R;
import com.udacity.baking.database.RecipeStepDetails;
import com.udacity.baking.database.RecipeStepsEntity;
import com.udacity.baking.network.AssertConnectivity;
import com.udacity.baking.viewmodel.DetailViewModel;

import java.util.List;


public class VideoFragment extends Fragment {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;

    private int currentWindow = 0;
    private long playbackPosition = 0;

    private View rootView;
    private ImageView imageView;
    private DetailViewModel mDetailViewModel;
    private static final String TAG = "VIDEO FRAGMENT";
    private String mVideoURL = "video.com";


    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_video, container, false);

        return rootView;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViewModel();

        playerView = rootView.findViewById(R.id.video_view);
        imageView = rootView.findViewById(R.id.image_view);



    }

    private void initViewModel() {
        mDetailViewModel = ViewModelProviders.of(this)
                .get(DetailViewModel.class);


        // Observer of new Video URL
        DetailViewModel.getsVideoURL().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String urlString) {
                Log.d(TAG, "onChanged: videoURL: " + urlString);
                mVideoURL = urlString;

                // Don't play video if URL is empty
                if(urlString.isEmpty()){
                    Log.d(TAG, "onChanged: URL EMPTY");
                    playerView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    releasePlayer();
                }else{
                    Log.d(TAG, "onChanged: urlString is NOT empty");

                    // Check if there's connectivity before playing video
                    new AssertConnectivity(getContext());

                    if(AssertConnectivity.isOnline()){
                        initializePlayer();
                    }else {
                        AssertConnectivity.errorConnectMessage();
                    }



                    playerView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                }


            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

/*    @Override
    public void onResume() {
        super.onResume();
        //hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }*/

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

/*    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }*/

    private void initializePlayer() {
        if (player == null){
            player = ExoPlayerFactory.newSimpleInstance(getContext());
            playerView.setPlayer(player);

        }

        Uri uri = Uri.parse(mVideoURL);

        MediaSource mediaSource = buildMediaSource(uri);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(getContext(), "Baking Recipes");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }






}
