package com.coretronic.drone.album.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.utility.AppUtils;

import java.util.ArrayList;

/**
 * Created by james on 15/6/12.
 */
public class AlbumListViewAdapter extends RecyclerView.Adapter<AlbumListViewAdapter.ViewHolder> {

    private static String TAG = AlbumListViewAdapter.class.getSimpleName();
    private static Context context;
    private static ArrayList<MediaListItem> mediaListItems = new ArrayList<MediaListItem>();


    public interface OnItemClickListener {
        void onItemDeleteClick(View view, int position);

        void onDownloadClick(View view, int position);
    }

    public static OnItemClickListener mItemClickListener = null;

    public void SetOnItemClickListener(final OnItemClickListener listener) {
        mItemClickListener = listener;
    }


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
        holder.mediaFilesize.setText(AppUtils.readableFileSize(listItem.getMediaSize()));

        holder.mediaDeleteIBtn.setTag(position);
        holder.mediaDownloadIBtn.setTag(position);


    }


    @Override
    public int getItemCount() {
        return mediaListItems.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mediaFilename = null;
        TextView mediaDate = null;
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

            mediaDeleteIBtn.setOnClickListener(this);
            mediaDownloadIBtn.setOnClickListener(this);

        }


        public void onClick(View v) {
            if (v.equals(mediaDeleteIBtn)) {
                mItemClickListener.onItemDeleteClick(v, getAdapterPosition());
            } else if (v.equals(mediaDownloadIBtn)) {
                mItemClickListener.onDownloadClick(v, getAdapterPosition());
            }
        }
    }

}
