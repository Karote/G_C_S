package com.coretronic.drone.album;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.coretronic.drone.R;
import com.coretronic.drone.utility.AppUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by james on 15/6/8.
 */
public class AlbumPagerPreviewDetailFragment extends Fragment implements MediaPlayer.OnCompletionListener,
        SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnPreparedListener,
        SurfaceHolder.Callback {

    private String TAG = AlbumPagerPreviewDetailFragment.class.getName();
    private Context context = null;
    private int mediaType = 0;
    private String previewShowMeidaPath = "";

    private Bundle bundle = null;
    private MediaController mediaControls;
    private MediaPlayer mediaPlayer = null;
    private SurfaceHolder surfaceHolder = null;
    private int width=0,height=0;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean progressFlag = false;

    // ui elements
    private ImageView showImage = null;
    private LinearLayout showVideoLayout = null;
    private SurfaceView surfaceView = null;
    private ImageButton videoControlBtn = null;
    private ImageButton videoBigControlBtn = null;
    private SeekBar seekBar = null;
    private TextView videoTimerTV = null;


    public AlbumPagerPreviewDetailFragment() {

    }

    public static AlbumPagerPreviewDetailFragment newInstance(int mediaType, String previewShowMeidaPath) {
        AlbumPagerPreviewDetailFragment albumPagerPreviewDetailFragment = new AlbumPagerPreviewDetailFragment();

        Bundle args = new Bundle();
        args.putInt("mediaType", mediaType);
        args.putString("previewShowMeidaPath", previewShowMeidaPath);
        albumPagerPreviewDetailFragment.setArguments(args);

        return albumPagerPreviewDetailFragment;
    }

//    public AlbumPagerPreviewDetailFragment(int mediaType, String previewShowMeidaPath) {
//        Log.i(TAG, "mediaType:" + mediaType + "/meidaPath:" + previewShowMeidaPath);
//        this.mediaType = mediaType;
//        this.previewShowMeidaPath = previewShowMeidaPath;
//    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_album_preview_pagercontent, container, false);
        context = fragmentView.getContext();

        Log.i(TAG, "===onCreateView===");
        findViews(fragmentView);

        bundle = getArguments();
        if( bundle != null )
        {
            this.mediaType = bundle.getInt("mediaType",0);
            this.previewShowMeidaPath = bundle.getString("mediaPath","");
        }
        Log.i(TAG,"bundle:"+bundle+"/this.mediaType:"+this.mediaType+"/this.previewShowMeidaPath:"+this.previewShowMeidaPath);
//        this.mediaType = getArguments().getInt("mediaType",0);
//        this.previewShowMeidaPath = getArguments().getString("previewShowMeidaPath","");

        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            if( mTimerTask != null)
            {
                mTimerTask.cancel();
                mTimerTask = null;
            }
            if( mTimer != null )
            {
                mTimer.cancel();
                mTimer = null;
            }

            showImage.setVisibility(View.VISIBLE);
            showVideoLayout.setVisibility(View.GONE);


            showImage.setImageBitmap(AppUtils.rotateAndResizeBitmap(previewShowMeidaPath, 1024, 768));


        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            showImage.setVisibility(View.GONE);
            showVideoLayout.setVisibility(View.VISIBLE);

            mediaPlayer = new MediaPlayer();

            surfaceHolder = surfaceView.getHolder();

            surfaceHolder.addCallback(this);
            seekBar.setOnSeekBarChangeListener(this);

//            playViedo();

        }

        return fragmentView;
    }

    private void findViews(View fragmentView) {
        showImage = (ImageView) fragmentView.findViewById(R.id.preview_showimage);
        showVideoLayout = (LinearLayout) fragmentView.findViewById(R.id.preview_showvideo_layout);
        surfaceView = (SurfaceView) fragmentView.findViewById(R.id.preview_video_sv);
        seekBar = (SeekBar) fragmentView.findViewById(R.id.seekBar);
        videoTimerTV = (TextView) fragmentView.findViewById(R.id.preview_play_time_tv);
        videoControlBtn = (ImageButton) fragmentView.findViewById(R.id.video_controller_ib);
        videoBigControlBtn = (ImageButton) fragmentView.findViewById(R.id.video_controller_big_ib);

//        videoControlBtn.setOnClickListener(videoControlBtnClickListener);
//        videoBigControlBtn.setOnClickListener(videoControlBtnClickListener);
    }

    private View.OnClickListener videoControlBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_controller_ib:
                    handleVideoControllerAction();
                    break;
                case R.id.video_controller_big_ib:
                    handleVideoControllerAction();
                    break;
            }

        }
    };


    private void handleVideoControllerAction() {
        Log.i(TAG,"===handleVideoControllerAction===");
        if( mediaPlayer != null && mediaPlayer.isPlaying())
        {

            videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
            videoBigControlBtn.setVisibility(View.VISIBLE);
            mediaPlayer.pause();

        }
        else
        {
            videoControlBtn.setBackgroundResource(R.drawable.ic_album_pause_n);
            videoBigControlBtn.setVisibility(View.GONE);
//            playViedo();
            mediaPlayer.start();
//            mediaPlayer.start();
        }

//        if (surfaceView.isPlaying()) {
//            videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
//            videoBigControlBtn.setVisibility(View.VISIBLE);
//            surfaceView.pause();
//        } else {
//            videoControlBtn.setBackgroundResource(R.drawable.ic_album_pause_n);
//            videoBigControlBtn.setVisibility(View.GONE);
//
//
//        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            Log.i(TAG, "===isResumed ===");
            if (seekBar != null) {
                Log.i(TAG, "progress async == null");
//                playViedo();
            }
        } else if (isVisibleToUser) {
            Log.i(TAG, "===only at fragment on created ===");
            if (seekBar != null) {
                Log.i(TAG, "progress async == null");
//                playViedo();
            }
        } else {
            Log.i(TAG, "===isPaused ===");
            if (videoControlBtn != null && videoBigControlBtn != null) {

                videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
                videoBigControlBtn.setVisibility(View.VISIBLE);

                if( mediaPlayer != null )
                {
                    mediaPlayer.seekTo(100);

                }

//                mediaPlayer.pause();
            }

            if( mediaPlayer != null )
            {
//                mediaPlayer.release();
            }
        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "-----surfaceCreated-----");
        playViedo();
    }

    private void playViedo() {
        try {
            Log.i(TAG, "-----playViedo-----");
            Log.i(TAG, "-----previewShowMeidaPath-----:" + previewShowMeidaPath);
            mediaPlayer.setDataSource(previewShowMeidaPath);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.prepare();
//            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "===surfaceDestroyed===");
        if( mediaPlayer != null )
        {
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.seekTo(100);
        videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
        videoBigControlBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "-----onPrepared-----");

        width = mediaPlayer.getVideoWidth();
        height = mediaPlayer.getVideoHeight();

        if (width != 0 && height != 0) {
            Log.i(TAG, "-----width != 0 && height != 0-----");
            surfaceHolder.setFixedSize(width,height);
            seekBar.setMax(mediaPlayer.getDuration());
            mp.seekTo(100);
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if( progressFlag == true )
                    {
                        return;
                    }
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            };

//            mTimer.schedule(mTimerTask,0,10);
//            mediaPlayer.start();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        progressFlag = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaPlayer.seekTo(seekBar.getProgress());
        progressFlag = false;
    }
}
