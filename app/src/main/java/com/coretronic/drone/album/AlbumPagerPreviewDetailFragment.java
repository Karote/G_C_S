package com.coretronic.drone.album;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.utility.AppUtils;

/**
 * Created by james on 15/6/8.
 */
public class AlbumPagerPreviewDetailFragment extends UnBindDrawablesFragment {

    private String TAG = AlbumPagerPreviewDetailFragment.class.getName();
    private final static int INIT_MILLINSECS = 500;
    private Context context = null;
    private int mediaType = 0;
    private String previewShowMeidaPath = "";


    // ui elements
    private ImageView showImage = null;
    private LinearLayout showVideoLayout = null;
    private VideoView previewVideo = null;
    private ImageButton videoControlBtn = null;
    private ImageButton videoBigControlBtn = null;
    private SeekBar progressBar = null;
    private TextView videoTimerTV = null;


    public AlbumPagerPreviewDetailFragment() {

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_album_preview_pagercontent, container, false);
        context = fragmentView.getContext();

        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mediaType = bundle.getInt("mediaType");
            this.previewShowMeidaPath = bundle.getString("mediaPath");
//            Log.i(TAG, "mediaType:" + mediaType + "/meidaPath:" + previewShowMeidaPath);
        }

        Log.i(TAG, "====onCreateView/previewShowMeidaPath:" + previewShowMeidaPath);
        findViews(fragmentView);

        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {

            showImage.setVisibility(View.VISIBLE);
            showVideoLayout.setVisibility(View.GONE);

            showImage.setImageBitmap(AppUtils.rotateAndResizeBitmap(previewShowMeidaPath, 1024, 768));


        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            showImage.setVisibility(View.GONE);
            showVideoLayout.setVisibility(View.VISIBLE);

            try {
//                previewVideo.setVideoPath(previewShowMeidaPath);
                previewVideo.setVideoURI(Uri.parse(previewShowMeidaPath));
                previewVideo.seekTo(INIT_MILLINSECS);
                previewVideo.requestFocus();
                progressBar.setMax(previewVideo.getDuration());
//                progressAsync = new ProgressAsync();
//                progressAsync.execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "preview video error:" + e.getMessage());
            }


        }

        return fragmentView;
    }

    private void findViews(View fragmentView) {
        showImage = (ImageView) fragmentView.findViewById(R.id.preview_showimage);
        showVideoLayout = (LinearLayout) fragmentView.findViewById(R.id.preview_showvideo_layout);
        previewVideo = (VideoView) fragmentView.findViewById(R.id.preview_video);
        progressBar = (SeekBar) fragmentView.findViewById(R.id.progressBar);
        videoTimerTV = (TextView) fragmentView.findViewById(R.id.preview_play_time_tv);
        videoControlBtn = (ImageButton) fragmentView.findViewById(R.id.video_controller_ib);
        videoBigControlBtn = (ImageButton) fragmentView.findViewById(R.id.video_controller_big_ib);
        previewVideo.setOnTouchListener(videoViewClickListener);
        previewVideo.setOnCompletionListener(previewVideoCompletionListener);
//        previewVideo.setOnPreparedListener(previewVideoOnPreparedListener);
        videoControlBtn.setOnClickListener(videoControlBtnClickListener);
        videoBigControlBtn.setOnClickListener(videoControlBtnClickListener);
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

        Log.i(TAG, "previewVideo.isPlaying():" + previewVideo.isPlaying());
        if (previewVideo.isPlaying()) {
            videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
            videoBigControlBtn.setVisibility(View.VISIBLE);

            previewVideo.pause();
            previewVideo.seekTo(INIT_MILLINSECS);

        } else {
            videoControlBtn.setBackgroundResource(R.drawable.ic_album_pause_n);
            videoBigControlBtn.setVisibility(View.GONE);

            previewVideo.start();
            previewVideo.requestFocus();
            progressBar.setMax(previewVideo.getDuration());
            handler.post(runnable);
        }

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
        if (progressBar != null) {
            progressBar.setMax(previewVideo.getDuration());
        }

        if (isVisibleToUser) {
            if (previewVideo != null) {
                previewVideo.pause();
                previewVideo.seekTo(INIT_MILLINSECS);
                progressBar.setProgress(0);
                Log.i(TAG, "===is UserVisibleHint/previewShowMeidaPath:" + previewShowMeidaPath);
            }
        } else {
            if (previewVideo != null) {
                videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
                videoBigControlBtn.setVisibility(View.VISIBLE);
                previewVideo.seekTo(INIT_MILLINSECS);
                previewVideo.pause();
                progressBar.setProgress(0);

                Log.i(TAG, "===not is UserVisibleHint/previewShowMeidaPath:" + previewShowMeidaPath);
            }

            if (videoTimerTV != null) {
                videoTimerTV.setText("0:00");
            }
        }


    }

    private MediaPlayer.OnCompletionListener previewVideoCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
            videoBigControlBtn.setVisibility(View.VISIBLE);
            previewVideo.pause();
            handler.removeCallbacks(runnable);
        }
    };


    private VideoView.OnTouchListener videoViewClickListener = new VideoView.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
            videoBigControlBtn.setVisibility(View.VISIBLE);
            previewVideo.pause();
            return true;
        }
    };

    /*
        private MediaPlayer.OnPreparedListener previewVideoOnPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "===on prepared ===");
                videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
                videoBigControlBtn.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                progressBar.setMax(100);
                previewVideo.seekTo(100);
                previewVideo.pause();

            }
        };
    */
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (previewVideo.isPlaying()) {
                int current = previewVideo.getCurrentPosition();
                videoTimerTV.setText(milliSecondsToTimer(current));
                progressBar.setProgress(current);
                handler.postDelayed(runnable, 500);
            }
        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "===onDestroyView/previewShowMeidaPath:" + previewShowMeidaPath);
        videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
        videoBigControlBtn.setVisibility(View.VISIBLE);
        previewVideo.pause();
        previewVideo.seekTo(INIT_MILLINSECS);
        handler.removeCallbacks(runnable);
    }

}
