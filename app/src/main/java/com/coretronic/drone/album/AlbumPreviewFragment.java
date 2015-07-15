package com.coretronic.drone.album;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.album.adapter.MediaPreviewAdapter;
import com.coretronic.drone.album.model.MediaItem;
import com.coretronic.drone.album.model.MediaObject;
import com.coretronic.drone.utility.AppUtils;
import com.coretronic.drone.utility.CustomerTwoBtnAlertDialog;

import java.util.ArrayList;

/**
 * Created by james on 15/6/5.
 */
public class AlbumPreviewFragment extends UnBindDrawablesFragment implements ViewPager.OnPageChangeListener {

    private static String TAG = AlbumPreviewFragment.class.getSimpleName();
    private Context context = null;

    private MediaPreviewAdapter mediaPreviewAdapter = null;
    // fragment declare
    private FragmentManager fragmentManager = null;
    private FragmentTransaction previewFragmentTransaction = null;
    private FragmentActivity fragmentActivity = null;
    // ui elements declare
    private Button previewBackBtn = null;
    private Button previewShareBtn = null;
    private Button previewDeleteBtn = null;
    private TextView previewCountTitle = null;
    private ViewPager previewPager = null;
    private CustomerTwoBtnAlertDialog removeDialog = null;

    private long selectedMediaId = 0;
    private int currentMediaIdx = 0;
    private ArrayList<MediaItem> mediaItems = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = getActivity();
        fragmentManager = getFragmentManager();

        previewFragmentTransaction = fragmentManager.beginTransaction();

        // get media list data
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        MediaObject mediaObject = (MediaObject) bundle.getSerializable("mediaObject");
        mediaItems = mediaObject.getImageItems();
        selectedMediaId = bundle.getLong("selectMediaId");


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_preview_page, container, false);
        context = view.getContext();


        findViews(view);


        // get number count
        currentMediaIdx = getSerialNumber(selectedMediaId);
        if (currentMediaIdx != -1) {
            previewCountTitle.setText((currentMediaIdx + 1) + "/" + mediaItems.size());
        }

        mediaPreviewAdapter = new MediaPreviewAdapter(getChildFragmentManager(), mediaItems, currentMediaIdx);
        previewPager.setAdapter(mediaPreviewAdapter);
        // move to touch item
        previewPager.setCurrentItem(currentMediaIdx, false);
        previewPager.setOnPageChangeListener(this);
        previewPager.setOffscreenPageLimit(0);
        removeDialog = AppUtils.getAlertDialog(context, context.getResources().getString(R.string.delete_files), context.getResources().getString(R.string.btn_ok), context.getResources().getString(R.string.btn_cancel), removeDialogOKListener);


        Log.i(TAG, "imageItems.get(currentMediaIdx).getMediaId():" + mediaItems.get(currentMediaIdx).getMediaId() + "/currentMediaIdx:" + currentMediaIdx);
        return view;
    }

    private void findViews(View view) {
        previewBackBtn = (Button) view.findViewById(R.id.preview_back_btn);
        previewShareBtn = (Button) view.findViewById(R.id.preview_share_btn);
        previewDeleteBtn = (Button) view.findViewById(R.id.preview_rubbish_bin_btn);
        previewCountTitle = (TextView) view.findViewById(R.id.preview_media_num_tv);
        previewPager = (ViewPager) view.findViewById(R.id.preview_pager);


        previewBackBtn.setOnClickListener(previewBackBtnListener);
        previewShareBtn.setOnClickListener(previewShareBtnListener);
        previewDeleteBtn.setOnClickListener(previewDeleteBtnListener);
    }

    @Override
    public void onPause() {
        super.onPause();
//        fragmentManager.popBackStack();
        Log.i(TAG, "==== on pause ====");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "==== onResume ====");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "==== onDestroyView ====");
    }

    private View.OnClickListener previewBackBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.i(TAG, "fragmentManager:" + fragmentManager);
            fragmentManager.popBackStack();

        }
    };

    private View.OnClickListener previewShareBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

            if (mediaItems.get(currentMediaIdx).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                shareIntent.setType("image/*");
            } else if (mediaItems.get(currentMediaIdx).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                shareIntent.setType("video/*");
            }
            Uri uri = Uri.parse(mediaItems.get(currentMediaIdx).getMediaPath());
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(shareIntent);
        }
    };

    private View.OnClickListener previewDeleteBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            removeDialog.show();

        }
    };

    // get current media index in arraylist
    private int getSerialNumber(long mediaId) {
        for (int i = 0; i < mediaItems.size(); i++) {
            if ((long) mediaItems.get(i).getMediaId() == mediaId) {
                return i;
            }
        }
        return -1;
    }


    //  --- viewpager interface methods  ---
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        Log.i(TAG, "onPageScrolled position:" + position);


    }

    @Override
    public void onPageSelected(int position) {

//        Log.i(TAG, "onPageSelected position:" + position);
        currentMediaIdx = position;
        previewCountTitle.setText((position + 1) + "/" + mediaItems.size());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
//        Log.i(TAG, "onPageScrollStateChanged state:" + state);
        if (state == ViewPager.SCROLL_STATE_DRAGGING) {

        }

    }

    // delete dialog ok listener
    private View.OnClickListener removeDialogOKListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

//            Log.i(TAG, "currentMediaIdx:" + currentMediaIdx + "/ imageItems.get(currentMediaIdx).getMediaId():" + imageItems.get(currentMediaIdx).getMediaId());
            ContentResolver cr = context.getContentResolver(); // in an Activity
            cr.delete(MediaStore.Files.getContentUri("external"),
                    MediaStore.Files.FileColumns._ID + " = ?", new String[]{"" + (long) mediaItems.get(currentMediaIdx).getMediaId()});

            Log.i(TAG, "mediaItems/currentMediaIdx:" + mediaItems + "/currentMediaIdx:" + currentMediaIdx);

            mediaItems.remove(currentMediaIdx);
            previewCountTitle.setText((currentMediaIdx + 1) + "/" + mediaItems.size());

            Log.i(TAG, "mediaItems.size():" + mediaItems.size());
            if (mediaItems.size() == 0) {
                fragmentManager.popBackStack();
            } else {
                mediaPreviewAdapter.notifyDataSetChanged();
            }

            Log.i(TAG, "currentMediaIdx:" + currentMediaIdx);

            previewPager.invalidate();
            removeDialog.dismiss();


        }
    };


}
