package com.coretronic.drone.album.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.R;
import com.coretronic.drone.album.AlbumDroneTagFragment;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.utility.AppUtils;

import java.util.ArrayList;

/**
 * Created by james on 15/6/12.
 */
public class AlbumListViewAdapter extends RecyclerView.Adapter<AlbumListViewAdapter.ViewHolder> {

    private static String TAG = AlbumListViewAdapter.class.getSimpleName();
    private static Context context;
    private static Boolean isShowDeleteOption = false;
    private static ArrayList<MediaListItem> mediaListItems = new ArrayList<MediaListItem>();
    private static ArrayList<String> selectedPathAryList = new ArrayList<String>();


    public interface OnItemClickListener {
        //        void onItemDeleteClick(View view, int position);
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

//        holder.mediaSelectTag.setTag(position);
        holder.mediaDownloadIBtn.setTag(position);

        if (!isShowDeleteOption) {
            holder.mediaDownloadIBtn.setVisibility(View.VISIBLE);
            holder.mediaSelectTag.setSelected(false);
            holder.mediaSelectTag.setVisibility(View.GONE);
        } else {
            holder.mediaDownloadIBtn.setVisibility(View.GONE);
            if (((MediaListItem) mediaListItems.get(position)).getIsMediaSelect()) {
                holder.mediaSelectTag.setSelected(true);
            } else {
                holder.mediaSelectTag.setSelected(false);
            }
            holder.mediaSelectTag.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public int getItemCount() {
        return mediaListItems.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mediaFilename = null;
        TextView mediaDate = null;
        TextView mediaFilesize = null;
        ImageView mediaSelectTag = null;
        ImageButton mediaDownloadIBtn = null;

        public ViewHolder(View itemView) {
            super(itemView);
            mediaDate = (TextView) itemView.findViewById(R.id.media_date);
            mediaFilesize = (TextView) itemView.findViewById(R.id.media_filesize);
            mediaFilename = (TextView) itemView.findViewById(R.id.media_filename);
            mediaSelectTag = (ImageView) itemView.findViewById(R.id.media_list_select_tag);
            mediaDownloadIBtn = (ImageButton) itemView.findViewById(R.id.media_download_btn);

            mediaSelectTag.setOnClickListener(selectTagAction);
            mediaDownloadIBtn.setOnClickListener(this);

        }

        public void onClick(View v) {
//            if(v.equals(mediaDeleteIBtn)){
//                mItemClickListener.onItemDeleteClick(v, getAdapterPosition());
//            }else if (v.equals(mediaDownloadIBtn)) {
            mItemClickListener.onDownloadClick(v, getAdapterPosition());
//            }
        }

        private View.OnClickListener selectTagAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPos = getAdapterPosition();
                if (((MediaListItem) mediaListItems.get(itemPos)).getIsMediaSelect()) {

                    mediaSelectTag.setSelected(false);
                    ((MediaListItem) mediaListItems.get(itemPos)).setIsMediaSelect(false);
                    // if selected path is contain arraylist
                    if (selectedPathAryList.contains(((MediaListItem) mediaListItems.get(itemPos)).getMediaFileName())) {
                        selectedPathAryList.remove(((MediaListItem) mediaListItems.get(itemPos)).getMediaFileName());
                    }

                } else {
                    ((MediaListItem) mediaListItems.get(itemPos)).setIsMediaSelect(true);
                    mediaSelectTag.setSelected(true);

                    selectedPathAryList.add(((MediaListItem) mediaListItems.get(itemPos)).getMediaFileName());
                }
            }
        };
    }

    public void setIsShowDeleteOption(Boolean isShowDeleteOption) {
        this.isShowDeleteOption = isShowDeleteOption;
    }

    public void deleteSelectedPathAryList() {
        for (MediaListItem item : mediaListItems) {
            item.setIsMediaSelect(false);
        }
        selectedPathAryList.clear();
    }

    public void deleteSelectMediaFile(final DroneController droneController, final AlbumDroneTagFragment albumDroneTagFragment) {
        for (int i = 0; i < mediaListItems.size(); i++) {
            for (int j = 0; j < selectedPathAryList.size(); j++) {
                final String s1 = (String) selectedPathAryList.get(j);
                String s2 = mediaListItems.get(i).getMediaFileName();
                if (s1.equals(s2)) {
                    // delete drone file
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            droneController.removeContent(s1, albumDroneTagFragment);
//                        }
//                    }).start();
//                    notifyItemRemoved(i);
                }
            }
        }
        deleteSelectedPathAryList();
    }

    public static ArrayList getSelectedPathAryList() {
        return selectedPathAryList;
    }
}
