package com.coretronic.drone.album;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.coretronic.drone.R;
import com.coretronic.drone.utility.AppUtils;

/**
 * Created by james on 15/6/8.
 */
public class AlbumPagerPreviewDetailFragment extends Fragment {

    private String TAG = AlbumPagerPreviewDetailFragment.class.getName();
    private Context context = null;
    private int mediaType = 0;
    private String previewShowMeidaPath = "";

    private MediaController mediaControls;

    // ui elements
    private ImageView showImage = null;
    private LinearLayout showVideoLayout = null;
    private VideoView previewVideo = null;
    private ImageButton videoControlBtn = null;
    private ImageButton videoBigControlBtn = null;
    private ProgressBar progressBar = null;
    private TextView videoTimerTV = null;


    public AlbumPagerPreviewDetailFragment() {

    }

    public AlbumPagerPreviewDetailFragment(int mediaType, String previewShowMeidaPath) {
        Log.i(TAG, "mediaType:" + mediaType + "/meidaPath:" + previewShowMeidaPath);
        this.mediaType = mediaType;
        this.previewShowMeidaPath = previewShowMeidaPath;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_album_preview_pagercontent, container, false);
        context = fragmentView.getContext();

        findViews(fragmentView);

        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {

            showImage.setVisibility(View.VISIBLE);
            showVideoLayout.setVisibility(View.GONE);


            showImage.setImageBitmap(AppUtils.rotateAndResizeBitmap(previewShowMeidaPath, 1024, 768));


        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            showImage.setVisibility(View.GONE);
            showVideoLayout.setVisibility(View.VISIBLE);

            if (mediaControls == null) {
                mediaControls = new MediaController(context);
            }

            try {
//                previewVideo.setMediaController(mediaControls);
                previewVideo.setVideoPath(previewShowMeidaPath);
//                previewVideo.requestFocus();
//                previewVideo.setFocusableInTouchMode(false);
                previewVideo.seekTo(100);
                previewVideo.pause();

                // progress bar
                progressBar.setProgress(0);
                progressBar.setMax(100);

            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "preview video error:" + e.getMessage());
            }


        }

        return fragmentView;
    }

    private void findViews(View fragmentView) {
        showImage = (ImageView) fragmentView.findViewById(R.id.preview_showimage);
        showVideoLayout = (LinearLayout) fragmentView.findViewById(R.id.preview_showvideo_layout);
        previewVideo = (VideoView) fragmentView.findViewById(R.id.preview_video);
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);
        videoTimerTV = (TextView) fragmentView.findViewById(R.id.preview_play_time_tv);
        videoControlBtn = (ImageButton) fragmentView.findViewById(R.id.video_controller_ib);
        videoBigControlBtn = (ImageButton) fragmentView.findViewById(R.id.video_controller_big_ib);

        videoControlBtn.setOnClickListener(videoControlBtnClickListener);
        videoBigControlBtn.setOnClickListener(videoControlBtnClickListener);
    }

    private View.OnClickListener videoControlBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.video_controller_ib:
                    handleVideoControllerAction();
                    break;
                case R.id.video_controller_big_ib:
                    handleVideoControllerAction();
                    break;
            }

        }
    };


    private void handleVideoControllerAction()
    {
        if( previewVideo.isPlaying() )
        {
            videoControlBtn.setBackgroundResource(R.drawable.ic_album_preview_play_n);
            videoBigControlBtn.setVisibility(View.VISIBLE);
            previewVideo.pause();
        }
        else
        {
            videoControlBtn.setBackgroundResource(R.drawable.ic_album_pause_n);
            videoBigControlBtn.setVisibility(View.GONE);
            previewVideo.start();

            new ProgressAsync().execute();
        }
    }

    private class ProgressAsync extends AsyncTask<Void, Integer, Void>
    {

        int duration = 0;
        int current = 0;

        @Override
        protected Void doInBackground(Void... params) {

            duration = previewVideo.getDuration();
            do {
                current = previewVideo.getCurrentPosition();
                System.out.println("duration - " + duration + " current- "
                        + current);
                try {
                    publishProgress((int) (current * 100 / duration));
                    if(progressBar.getProgress() >= 100){
                        break;
                    }
                } catch (Exception e) {
                }
            } while (progressBar.getProgress() <= 100);


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int hours = values[0]/1000 / 3600;
            int minutes = (values[0]/1000 / 60) - (hours * 60);
            int seconds = values[0]/1000 - (hours * 3600) - (minutes * 60) ;
            progressBar.setProgress(values[0]/1000);
            videoTimerTV.setText(minutes +":"+seconds);

            Log.i(TAG,"previewVideo.getCurrentPosition():"+previewVideo.getCurrentPosition());
        }
    }


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
}
