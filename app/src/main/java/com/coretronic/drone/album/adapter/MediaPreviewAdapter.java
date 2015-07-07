package com.coretronic.drone.album.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.coretronic.drone.album.AlbumPagerPreviewDetailFragment;
import com.coretronic.drone.album.model.MediaItem;

import java.util.ArrayList;

/**
 * Created by james on 15/6/5.
 */
public class MediaPreviewAdapter extends FragmentStatePagerAdapter {

    private String TAG = MediaPreviewAdapter.class.getName();
    private ArrayList<MediaItem> mediaItems = null;
    private int currentMediaNum = 0;

    public MediaPreviewAdapter(FragmentManager fm, ArrayList<MediaItem> mediaItems, int currentMediaNum) {
        super(fm);
        Log.i(TAG, "MediaPreviewAdapter initial");
        this.mediaItems = mediaItems;
        this.currentMediaNum = currentMediaNum;

    }


    @Override
    public Fragment getItem(int position) {

        Log.i(TAG, "position:" + position);
        Log.i(TAG, "imageItems.get(currentMediaNum).getMediaPath():" + mediaItems.get(position).getMediaPath());
        Bundle bundle = new Bundle();
        bundle.putInt("mediaType", mediaItems.get(position).getMediaType());
        bundle.putString("mediaPath", mediaItems.get(position).getMediaPath());
        Fragment fragment = null;
        fragment = new AlbumPagerPreviewDetailFragment();
        fragment.setArguments(bundle);

//        AlbumPagerPreviewDetailFragment.newInstance(mediaItems.get(position).getMediaType(),
//                mediaItems.get(position).getMediaPath());

        return fragment;
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
