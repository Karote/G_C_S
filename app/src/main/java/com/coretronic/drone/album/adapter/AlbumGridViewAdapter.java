package com.coretronic.drone.album.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.album.AlbumPreviewFragment;
import com.coretronic.drone.album.model.MediaItem;
import com.coretronic.drone.album.model.MediaObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by james on 15/6/1.
 */
public class AlbumGridViewAdapter extends RecyclerView.Adapter<AlbumGridViewAdapter.ViewHolder> {

    private static String TAG = AlbumGridViewAdapter.class.getSimpleName();
    private static Context context;
    private int resourceId;
    private static ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
    private static Boolean isShowDeleteOption = false;
    private static ArrayList selectedPathAryList = new ArrayList();

    public AlbumGridViewAdapter(Context context, int resource, ArrayList<MediaItem> data) {

        Log.i(TAG, "AlbumGridViewAdapter");
        this.context = context;
        this.resourceId = resource;

        this.mediaItems = data;
//        Collections.sort(this.data, new CustomComparator());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemlayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.album_smartphone_griditem, null);

        ViewHolder viewHolder = new ViewHolder(itemlayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        MediaItem item = (MediaItem) mediaItems.get(i);
        Bitmap bitmap = null;

        // set thumbnails images
        if (item.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            // image file
            bitmap = MediaStore.Images.Thumbnails.getThumbnail(context
                            .getApplicationContext().getContentResolver(), item.getMediaId(),
                    MediaStore.Images.Thumbnails.MINI_KIND, null);

            viewHolder.videoTagImg.setVisibility(View.GONE);

        } else if (item.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            // video file
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(context
                            .getApplicationContext().getContentResolver(), item.getMediaId(),
                    MediaStore.Images.Thumbnails.MINI_KIND, null);
            viewHolder.videoTagImg.setVisibility(View.VISIBLE);
        }

//        bitmap = Bitmap.createBitmap(
//                bitmap,
//                50,
//                50,
//                bitmap.getWidth() - 50 * 2,
//                bitmap.getHeight() - 50 * 2
//        );
        viewHolder.mediaImage.setImageBitmap(bitmap);

        viewHolder.selectTagImg.setImageResource(R.drawable.ic_album_uncheck_n);
        ((MediaItem) mediaItems.get(i)).setIsMediaSelect(false);

        // set delete option
        if (isShowDeleteOption) {
            viewHolder.selectTagImg.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectTagImg.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RelativeLayout itemLayout = null;
        TextView imageTitle = null;
        ImageView mediaImage = null;
        ImageView videoTagImg = null;
        ImageView selectTagImg = null;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.item_layout);
            imageTitle = (TextView) itemView.findViewById(R.id.imageview_title);
            mediaImage = (ImageView) itemView.findViewById(R.id.imageview);
            videoTagImg = (ImageView) itemView.findViewById(R.id.video_tag);
            selectTagImg = (ImageView) itemView.findViewById(R.id.select_tag);
        }

        @Override
        public void onClick(View v) {


            int itemPos = getAdapterPosition();

            Log.i(TAG, "itemPos:" + itemPos +
                    "/ click path: " + ((MediaItem) mediaItems.get(itemPos)).getMediaPath() +
                    "/ click id: " + ((MediaItem) mediaItems.get(itemPos)).getMediaId());


            // delete option menu status
            if (isShowDeleteOption) {

                if (((MediaItem) mediaItems.get(itemPos)).getIsMediaSelect()) {

                    selectTagImg.setImageResource(R.drawable.ic_album_uncheck_n);
                    ((MediaItem) mediaItems.get(itemPos)).setIsMediaSelect(false);
                    // if selected path is contain arraylist
                    if (selectedPathAryList.contains(((MediaItem) mediaItems.get(itemPos)).getMediaId())) {
                        selectedPathAryList.remove(((MediaItem) mediaItems.get(itemPos)).getMediaId());
                    }

                } else {
                    ((MediaItem) mediaItems.get(itemPos)).setIsMediaSelect(true);
                    selectTagImg.setImageResource(R.drawable.ic_album_check_n);

                    selectedPathAryList.add(((MediaItem) mediaItems.get(itemPos)).getMediaId());
                }

                Log.i(TAG, "selectedPathAryList:" + selectedPathAryList);


            } else {
                // preview status
                MediaObject mediaObject = new MediaObject();
                mediaObject.setImageItems(mediaItems);

                Bundle bundle = new Bundle();
                bundle.putSerializable("mediaObject", mediaObject);
                bundle.putLong("selectMediaId", ((MediaItem) mediaItems.get(itemPos)).getMediaId());
                Fragment cuttentFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("fragment");

                AlbumPreviewFragment albumPreviewFragment = new AlbumPreviewFragment();
                albumPreviewFragment.setArguments(bundle);
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .hide(cuttentFragment)
                        .add(R.id.frame_view, albumPreviewFragment, "AlbumPreviewFragment")
                        .addToBackStack("AlbumPreviewFragment")
                        .commit();
//                ((Fragment)context)
//                .beginTransaction().replace(R.id.album_fragment_container, droneAlbumFragment, "SmartPhoneFragment")
//                        .commit();
            }

        }
    }


    public void setIsShowDeleteOption(Boolean isShowDeleteOption) {
        this.isShowDeleteOption = isShowDeleteOption;
    }


    //    public class CustomComparator implements Comparator<ImageItem> {
//
//        @Override
//        public int compare(ImageItem lhs, ImageItem rhs) {
//            return lhs.getMediaDate().compareTo(rhs.getMediaDate());
//        }
//    }

    public class CustomComparator implements Comparator<Long> {


        @Override
        public int compare(Long lhs, Long rhs) {
            return lhs.compareTo(rhs);
        }
    }

    public void deleteSelectMediaFile() {
        ContentResolver cr = context.getContentResolver(); // in an Activity
        Collections.sort(selectedPathAryList, new CustomComparator());
        List<Integer> tempDelAry = new ArrayList<Integer>();
        for (int i = 0; i < mediaItems.size(); i++) {
            for (int j = 0; j < selectedPathAryList.size(); j++) {
                if ((long) (selectedPathAryList.get(j)) == mediaItems.get(i).getMediaId()) {

                    Log.i(TAG, "(long) imageItems.get(i).getMediaId():" + (long) mediaItems.get(i).getMediaId());
//                        selectedPathAryList.remove((long) imageItems.get(i).getMediaId());

                    int deletedNum = cr.delete(MediaStore.Files.getContentUri("external"),
                            MediaStore.Files.FileColumns._ID + " = ?", new String[]{"" + (long) mediaItems.get(i).getMediaId()});
                    tempDelAry.add(i);
//                    mediaItems.remove(mediaItems.get(i));
                    Log.i(TAG, "deletedNum:" + deletedNum);
                    notifyItemRemoved(i);
                }
            }
        }
        Log.i(TAG, "tempDelAry:" + tempDelAry);
        deleteSelectedPathAryList();
    }

    public void deleteSelectedPathAryList() {
        for (MediaItem item : mediaItems) {
            item.setIsMediaSelect(false);

        }

        selectedPathAryList.clear();

    }

    public static ArrayList getSelectedPathAryList() {
        return selectedPathAryList;
    }

    public void clearData() {
        mediaItems.clear();
    }
}
