package com.coretronic.drone.album;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;

/**
 * Created by jiaLian on 15/4/1.
 */
public class AlbumFragment extends UnBindDrawablesFragment {

    private static String TAG = AlbumFragment.class.getSimpleName();
    private FragmentManager fragmentChildManager             = null;
    private FragmentTransaction albumFragmentTransaction     = null;
    private Fragment smartPhoneAlbumFragment                 = null;
    private Fragment droneAlbumFragment                      = null;
    private FragmentActivity fragmentActivity                = null;

    private Switch albumSwitch = null;
    private ImageButton rubbishBinBtn = null;
    private LinearLayout deleteOptionLayout = null;
    private Button deleteBtn = null;
    private Button cancelDeleteBtn = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_album);

        fragmentActivity = getActivity();
        fragmentChildManager = getChildFragmentManager();
//
//        findViews();

//        albumFragmentManager = getFragmentManager();
        albumFragmentTransaction = fragmentChildManager.beginTransaction();
//        fragment = getReplaceTargetFragment(targetFragment);
//        fragment.setArguments(bundle);
        smartPhoneAlbumFragment = new AlbumSmartPhoneTagFragment();


        droneAlbumFragment = new AlbumDroneTagFragment();
        if (droneAlbumFragment != null) {
            albumFragmentTransaction
                    .replace(R.id.album_fragment_container, droneAlbumFragment, "SmartPhoneFragment")
                    .commit();
        } else {
            Log.e(TAG, "Error in creating fragment");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_album, container, false);
        findViews(fragmentView);

        return fragmentView;
    }

    private void findViews(View fragmentView) {
        albumSwitch = (Switch) fragmentView.findViewById(R.id.album_toggle);
        albumSwitch.setOnCheckedChangeListener(albumSwitchListener);

        rubbishBinBtn = (ImageButton) fragmentView.findViewById(R.id.rubbish_bin_btn);
        rubbishBinBtn.setOnClickListener(rubbishBinBtnAction);

        deleteOptionLayout = (LinearLayout) fragmentView.findViewById(R.id.delete_option_layout);
        deleteBtn = (Button) fragmentView.findViewById(R.id.delete_btn);
        cancelDeleteBtn = (Button) fragmentView.findViewById(R.id.cancel_btn);
        deleteBtn.setOnClickListener(deleteGroupAction);
        cancelDeleteBtn.setOnClickListener(deleteGroupAction);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }



    private View.OnClickListener rubbishBinBtnAction = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).showDeleteOption();

            // set delete option and hide the rubbish bin button
            deleteOptionLayout.setVisibility(View.VISIBLE);
            rubbishBinBtn.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener deleteGroupAction = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.delete_btn:
                    ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).deleteSelectMediaFile();
                    break;
                case R.id.cancel_btn:

                    ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).hideDeleteOption();
                    // set delete option and hide the rubbish bin button
                    deleteOptionLayout.setVisibility(View.GONE);
                    rubbishBinBtn.setVisibility(View.VISIBLE);
                    ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).deleteSelectedPathAryList();
                    break;
            }


        }
    };


    // album switch listener
    private CompoundButton.OnCheckedChangeListener albumSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.i(TAG, "isChecked:" + isChecked);

            albumFragmentTransaction = fragmentChildManager.beginTransaction();
            // switch to smart phone album fragment
            if (isChecked) {
                if (smartPhoneAlbumFragment != null) {
                    albumFragmentTransaction
                            .replace(R.id.album_fragment_container, smartPhoneAlbumFragment, "SmartPhoneFragment")
                            .commit();
                } else {
                    Log.e(TAG, "Error in creating fragment");
                }

            }
            // switch to drone album fragment
            else {
                if (droneAlbumFragment != null) {
                    albumFragmentTransaction
                            .replace(R.id.album_fragment_container, droneAlbumFragment, "DroneAlbumFragment")
                            .commit();
                } else {
                    Log.e(TAG, "Error in creating fragment");
                }

            }
        }
    };


}