package com.coretronic.drone.album;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.album.adapter.MediaPreviewAdapter;
import com.coretronic.drone.album.model.ImageItem;
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
    // fragment declare
    private Context mContext = null;
    private FragmentManager fragmentManager = null;
    private FragmentTransaction previewFragmentTransaction = null;
    private FragmentActivity fragmentActivity = null;
    // ui elements declare
    private ImageButton previewBackBtn = null;
    private ImageButton previewShareBtn = null;
    private ImageButton previewDeleteBtn = null;
    private TextView previewCountTitle = null;
    private ViewPager previewPager = null;
    private CustomerTwoBtnAlertDialog removeDialog = null;

    private long selectedMediaId = 0;
    private int currentMediaNum = 0;
    private ArrayList<ImageItem> imageItems = null;

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
        imageItems = mediaObject.getImageItems();
        selectedMediaId = bundle.getLong("selectMediaId");

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_preview_page, container, false);
        context = view.getContext();


        findViews(view);


        // get number count
        currentMediaNum = getSerialNumber(selectedMediaId);
        if (currentMediaNum != -1) {
            previewCountTitle.setText((currentMediaNum + 1) + "/" + imageItems.size());
        }

        MediaPreviewAdapter mediaPreviewAdapter = new MediaPreviewAdapter(getChildFragmentManager(), imageItems, currentMediaNum);
        previewPager.setAdapter(mediaPreviewAdapter);
        // move to touch item
        previewPager.setCurrentItem(currentMediaNum, false);
        previewPager.setOnPageChangeListener(this);
        removeDialog = AppUtils.getAlertDialog(context, context.getResources().getString(R.string.delete_files), context.getResources().getString(R.string.btn_ok), context.getResources().getString(R.string.btn_cancel), removeDialogOKListener);

        return view;
    }

    private void findViews(View view) {
        previewBackBtn = (ImageButton) view.findViewById(R.id.preview_back_btn);
        previewShareBtn = (ImageButton) view.findViewById(R.id.preview_share_btn);
        previewDeleteBtn = (ImageButton) view.findViewById(R.id.preview_rubbish_bin_btn);
        previewCountTitle = (TextView) view.findViewById(R.id.preview_media_num_tv);
        previewPager = (ViewPager) view.findViewById(R.id.preview_pager);


        previewBackBtn.setOnClickListener(previewBackBtnListener);
        previewShareBtn.setOnClickListener(previewShareBtnListener);
        previewDeleteBtn.setOnClickListener(previewDeleteBtnListener);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private View.OnClickListener previewBackBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            fragmentManager.popBackStack();

        }
    };

    private View.OnClickListener previewShareBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


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
        for (int i = 0; i < imageItems.size(); i++) {
            if ((long) imageItems.get(i).getMediaId() == mediaId) {
                return i;
            }
        }
        return -1;
    }


    //  --- viewpager interface methods  ---
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.i(TAG, "onPageScrolled position:" + position);
        currentMediaNum = position + 1;
        previewCountTitle.setText( (position + 1) + "/" + imageItems.size());
    }

    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "onPageSelected position:" + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.i(TAG, "onPageScrollStateChanged state:" + state);

    }

    // delete dialog ok listener
    private View.OnClickListener removeDialogOKListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            removeDialog.dismiss();
        }
    };

}
