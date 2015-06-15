package com.coretronic.drone.album.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.coretronic.drone.R;
import com.coretronic.drone.album.AlbumPreviewFragment;
import com.coretronic.drone.album.model.MediaItem;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.album.model.MediaObject;

import java.util.ArrayList;

/**
 * Created by james on 15/6/12.
 */
public class AlbumListViewAdapter extends RecyclerView.Adapter<AlbumListViewAdapter.ViewHolder>{

    private static String TAG = AlbumListViewAdapter.class.getSimpleName();
    private static Context context;
    private static ArrayList<MediaListItem> mediaListItems = new ArrayList<MediaListItem>();

    public AlbumListViewAdapter(Context context, ArrayList<MediaListItem> data) {
        this.context = context;

        this.mediaListItems = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemlayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_drone_listitem, null);

        ViewHolder viewHolder = new ViewHolder(itemlayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        MediaListItem listItem = (MediaListItem) mediaListItems.get(position);
        holder.mediaFilename.setText(listItem.getMediaFileName());
        holder.mediaDate.setText(listItem.getMediaDate().toString());
        holder.mediaFilesize.setText(listItem.getMediaSize());

        holder.mediaDeleteIBtn.setTag(position);
        holder.mediaDownloadIBtn.setTag(position);
        holder.mediaDeleteIBtn.setOnClickListener(deleteBtnListener);
        holder.mediaDownloadIBtn.setOnClickListener(downloadBtnListener);
    }

    @Override
    public int getItemCount() {
        return mediaListItems.size();
    }

    public View.OnClickListener downloadBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.i(TAG, "download btn click pos:" + v.getTag() );
            Toast.makeText(context, "download " + v.getTag() ,Toast.LENGTH_SHORT).show();
        }
    };

    public View.OnClickListener deleteBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "delete btn click pos:" + v.getTag());
            Toast.makeText(context, "delete " + v.getTag() ,Toast.LENGTH_SHORT).show();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder  {

        TextView mediaFilename = null;
        TextView mediaDate  = null;
        TextView mediaFilesize = null;
        ImageButton mediaDeleteIBtn = null;
        ImageButton mediaDownloadIBtn = null;

        public ViewHolder(View itemView) {
            super(itemView);
            mediaDate = (TextView) itemView.findViewById(R.id.media_date);
            mediaFilesize = (TextView) itemView.findViewById(R.id.media_filesize);
            mediaFilename = (TextView) itemView.findViewById(R.id.media_filename);
            mediaDeleteIBtn = (ImageButton) itemView.findViewById(R.id.media_listdelete_btn);
            mediaDownloadIBtn = (ImageButton) itemView.findViewById(R.id.media_download_btn);

        }



    }

  


}