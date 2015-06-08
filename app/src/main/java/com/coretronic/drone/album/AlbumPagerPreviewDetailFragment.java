package com.coretronic.drone.album;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

    // ui elements
    private ImageView showImage = null;
    private LinearLayout showVideoLayout = null;
    private VideoView previewVideo = null;
    private ImageButton videoPlayBtn = null;
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


            showImage.setImageBitmap(AppUtils.rotateAndResizeBitmap(previewShowMeidaPath,1024,768));

//            int imageResource = getResources().getIdentifier(previewShowMeidaPath, null, context.getPackageName());
//            Uri path = Uri.parse(previewShowMeidaPath);
//            showImage.setImageURI(path);

        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            showImage.setVisibility(View.GONE);
            showVideoLayout.setVisibility(View.VISIBLE);
        }

        return fragmentView;
    }

    private void findViews(View fragmentView) {
        showImage = (ImageView) fragmentView.findViewById(R.id.preview_showimage);
        showVideoLayout = (LinearLayout) fragmentView.findViewById(R.id.preview_showvideo_layout);
        previewVideo = (VideoView) fragmentView.findViewById(R.id.preview_video);
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);
        videoTimerTV = (TextView) fragmentView.findViewById(R.id.preview_play_time_tv);
    }
}
