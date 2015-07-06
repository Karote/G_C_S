package com.coretronic.drone.album.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
 * Created by karot.chuang on 2015/7/6.
 */
public class AlbumGridViewBaseAdapter extends BaseAdapter {
    private static String TAG = AlbumGridViewAdapter.class.getSimpleName();
    private static Context context;
    private static LayoutInflater inflater = null;
    private int resourceId;
    private static ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
    private static Boolean isShowDeleteOption = false;
    private static ArrayList selectedPathAryList = new ArrayList();

    public AlbumGridViewBaseAdapter(Context context, int resource, ArrayList<MediaItem> data) {
        this.context = context;
        this.resourceId = resource;
        this.mediaItems = data;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class Holder {
        FrameLayout itemLayout;
        TextView imageTitle;
        ImageView mediaImage;
        ImageView videoTagImg;
        ImageView selectTagImg;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        View grid;
        grid = inflater.inflate(R.layout.album_smartphone_griditem, null);

        holder.itemLayout = (FrameLayout) grid.findViewById(R.id.item_layout);
        holder.imageTitle = (TextView) grid.findViewById(R.id.imageview_title);
        holder.mediaImage = (ImageView) grid.findViewById(R.id.imageview);
        holder.videoTagImg = (ImageView) grid.findViewById(R.id.video_tag);
        holder.selectTagImg = (ImageView) grid.findViewById(R.id.select_tag);

        bindHolder(holder, position);

        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPos = position;

                Log.i(TAG, "itemPos:" + itemPos +
                        "/ click path: " + ((MediaItem) mediaItems.get(itemPos)).getMediaPath() +
                        "/ click id: " + ((MediaItem) mediaItems.get(itemPos)).getMediaId());


                // delete option menu status
                if (isShowDeleteOption) {

                    if (((MediaItem) mediaItems.get(itemPos)).getIsMediaSelect()) {

                        holder.selectTagImg.setVisibility(View.GONE);
                        ((MediaItem) mediaItems.get(itemPos)).setIsMediaSelect(false);
                        // if selected path is contain arraylist
                        if (selectedPathAryList.contains(((MediaItem) mediaItems.get(itemPos)).getMediaId())) {
                            selectedPathAryList.remove(((MediaItem) mediaItems.get(itemPos)).getMediaId());
                        }

                    } else {
                        ((MediaItem) mediaItems.get(itemPos)).setIsMediaSelect(true);
                        holder.selectTagImg.setVisibility(View.VISIBLE);

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
                }
            }
        });

        return grid;
    }

    private void bindHolder(Holder holder, int position){
        MediaItem item = (MediaItem) mediaItems.get(position);
        Bitmap bitmap = null;

        // set thumbnails images
        if (item.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            // image file
            bitmap = MediaStore.Images.Thumbnails.getThumbnail(context
                            .getApplicationContext().getContentResolver(), item.getMediaId(),
                    MediaStore.Images.Thumbnails.MINI_KIND, null);

            holder.videoTagImg.setVisibility(View.GONE);

        } else if (item.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            // video file
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(context
                            .getApplicationContext().getContentResolver(), item.getMediaId(),
                    MediaStore.Images.Thumbnails.MINI_KIND, null);
            holder.videoTagImg.setVisibility(View.VISIBLE);
        }
        holder.mediaImage.setImageBitmap(bitmap);

        ((MediaItem) mediaItems.get(position)).setIsMediaSelect(false);

        if(!isShowDeleteOption) {
            holder.selectTagImg.setVisibility(View.GONE);
        }
    }


    public void setIsShowDeleteOption(Boolean isShowDeleteOption) {
        this.isShowDeleteOption = isShowDeleteOption;
    }

    public void deleteSelectedPathAryList() {
        for (MediaItem item : mediaItems) {
            item.setIsMediaSelect(false);

        }

        selectedPathAryList.clear();

    }

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
                    int deletedNum = cr.delete(MediaStore.Files.getContentUri("external"),
                            MediaStore.Files.FileColumns._ID + " = ?", new String[]{"" + (long) mediaItems.get(i).getMediaId()});
                    tempDelAry.add(i);
                    Log.i(TAG, "deletedNum:" + deletedNum);
//                    notifyItemRemoved(i);
                }
            }
        }
        Log.i(TAG, "tempDelAry:" + tempDelAry);
        deleteSelectedPathAryList();
    }

    public static ArrayList getSelectedPathAryList() {
        return selectedPathAryList;
    }
}
