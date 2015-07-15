package com.coretronic.drone.album;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.utility.AppUtils;

/**
 * Created by james on 15/6/8.
 */
public class AlbumPagerPreviewDetailFragment extends UnBindDrawablesFragment
        implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        VideoControllerView.MediaPlayerControl {

    private String TAG = AlbumPagerPreviewDetailFragment.class.getName();
    private final static int INIT_MILLINSECS = 500;
    private Context context = null;
    private int mediaType = 0;
    private String previewShowMeidaPath = "";


    // ui elements
    private ImageView showImage = null;
    private RelativeLayout showVideoLayout = null;
    private FrameLayout videoSurfaceContainer = null;
    private SurfaceView previewVideo = null;
    private MediaPlayer player = null;
    private VideoControllerView controller = null;

    public AlbumPagerPreviewDetailFragment() {

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_album_preview_pagercontent, container, false);
        context = fragmentView.getContext();

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int deviceWidth = dm.widthPixels;
        int deviceHeight = dm.heightPixels;

        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mediaType = bundle.getInt("mediaType");
            this.previewShowMeidaPath = bundle.getString("mediaPath");
        }

        findViews(fragmentView);

        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {

            showImage.setVisibility(View.VISIBLE);
            showVideoLayout.setVisibility(View.GONE);

            showImage.setImageBitmap(AppUtils.rotateAndResizeBitmap(previewShowMeidaPath, deviceWidth, deviceHeight));
            showImage.setScaleType(ImageView.ScaleType.FIT_XY);


        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            showImage.setVisibility(View.GONE);
            showVideoLayout.setVisibility(View.VISIBLE);

            player = new MediaPlayer();
            controller = new VideoControllerView(context);
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);

            try {
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(previewShowMeidaPath);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "preview video error:" + e.getMessage());
            }


        }

        return fragmentView;
    }

    private void findViews(View fragmentView) {
        showImage = (ImageView) fragmentView.findViewById(R.id.preview_showimage);
        showVideoLayout = (RelativeLayout) fragmentView.findViewById(R.id.preview_showvideo_layout);
        videoSurfaceContainer = (FrameLayout) fragmentView.findViewById(R.id.videoSurfaceContainer);
        previewVideo = (SurfaceView) fragmentView.findViewById(R.id.preview_video);
        SurfaceHolder videoHolder = previewVideo.getHolder();
//        previewVideo.setOnTouchListener(videoViewClickListener);
        videoHolder.addCallback(surfaceCreatedListener);
    }
    /*
    private VideoView.OnTouchListener videoViewClickListener = new VideoView.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            player.pause();
            return true;
        }
    };

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
    */

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO && player != null) {
                Log.i(TAG, previewShowMeidaPath + " UserVisibleHint TRUE");
            }
        } else {
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO && player != null) {
                Log.i(TAG, previewShowMeidaPath + " UserVisibleHint FALSE");
                player.stop();
                player.prepareAsync();
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        super.onDestroyView();
        Log.i(TAG, previewShowMeidaPath + " onDestroyView");
    }

    @Override
    public void start() {
        if (player != null) {
            player.start();
        }
    }

    @Override
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public int getDuration() {
        if (player != null)
            return player.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (player != null)
            return player.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (player != null) {
            player.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        if (player != null)
            return player.isPlaying();
        else
            return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    private SurfaceHolder.Callback surfaceCreatedListener = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, previewShowMeidaPath + " previewVideo onSurfaceCreated");
            player.setDisplay(holder);
            player.prepareAsync();
            Log.i(TAG, previewShowMeidaPath + " player prepareAsync");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            Log.i(TAG, previewShowMeidaPath + " previewVideo onSurfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

            Log.i(TAG, previewShowMeidaPath + " previewVideo onSurfaceDestroyed");
        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, previewShowMeidaPath + " player onPrepared");
        controller.setMediaPlayer(this);
        controller.setAnchorView(videoSurfaceContainer);
        player.seekTo(0);
        player.setLooping(false);
        controller.show();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player == null)
            return;
        controller.setReplay();
    }
}
