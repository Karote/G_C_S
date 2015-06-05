package com.coretronic.drone.album.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.coretronic.drone.album.model.ImageItem;

import java.util.ArrayList;

/**
 * Created by james on 15/6/5.
 */
public class MediaPreviewAdapter extends BaseAdapter {

    private ArrayList<ImageItem> imageItems = null;

    public void MediaPreviewAdapter()
    {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
