package com.coretronic.drone.album.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import com.coretronic.drone.album.AlbumPagerPreviewDetailFragment;
import com.coretronic.drone.album.model.ImageItem;

import java.util.ArrayList;

/**
 * Created by james on 15/6/5.
 */
public class MediaPreviewAdapter extends FragmentStatePagerAdapter {

    private String TAG = MediaPreviewAdapter.class.getName();
    private ArrayList<ImageItem> imageItems = null;
    private int currentMediaNum = 0;

    public MediaPreviewAdapter(FragmentManager fm, ArrayList<ImageItem> imageItems, int currentMediaNum) {
        super(fm);
        Log.i(TAG, "MediaPreviewAdapter initial");
        this.imageItems = imageItems;
        this.currentMediaNum = currentMediaNum;

    }


    @Override
    public Fragment getItem(int position) {

        Log.i(TAG, "imageItems.get(currentMediaNum).getMediaPath():" + imageItems.get(currentMediaNum).getMediaPath());
        Fragment fragment = null;
        fragment = new AlbumPagerPreviewDetailFragment(imageItems.get(position).getMediaType(),
                                                 imageItems.get(position).getMediaPath());
        return fragment;
    }

    @Override
    public int getCount() {
        return imageItems.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
