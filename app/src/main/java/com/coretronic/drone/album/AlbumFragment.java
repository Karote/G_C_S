package com.coretronic.drone.album;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.coretronic.drone.Drone;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.utility.AppUtils;
import com.coretronic.drone.utility.CustomerTwoBtnAlertDialog;

/**
 * Created by jiaLian on 15/4/1.
 */
public class AlbumFragment extends UnBindDrawablesFragment implements Drone.StatusChangedListener {

    private static String TAG = AlbumFragment.class.getSimpleName();
    // fragment declare
    private Context mContext = null;
    private FragmentManager fragmentChildManager = null;
    private FragmentTransaction albumFragmentTransaction = null;
    private Fragment smartPhoneAlbumFragment = null;
    private Fragment droneAlbumFragment = null;
    private FragmentActivity fragmentActivity = null;
    // ui declare
    private StatusView statusView = null;
    private Button albumDroneSwitchBtn = null;
    private Button albumSmartPhoneSwitchBtn = null;
    private ImageButton rubbishBinBtn = null;
    private LinearLayout deleteOptionLayout = null;
    private RelativeLayout albumMenuOption = null;
    private Button deleteBtn = null;
    private Button cancelDeleteBtn = null;
    private Button albumListBackBtn = null;
    private CustomerTwoBtnAlertDialog deleteDialog = null;
    // mode
    private Boolean isDroneOrSmartphoneMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentActivity = getActivity();
        fragmentChildManager = getChildFragmentManager();

        albumFragmentTransaction = fragmentChildManager.beginTransaction();
        smartPhoneAlbumFragment = new AlbumSmartPhoneTagFragment();

        fragmentActivity.getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener());
//        fragmentChildManager.addOnBackStackChangedListener( backStackChangedListener());

        droneAlbumFragment = new AlbumDroneTagFragment();
        if (droneAlbumFragment != null) {
            albumFragmentTransaction
                    .replace(R.id.album_fragment_container, droneAlbumFragment, "DroneAlbumFragment")
//                    .replace(R.id.album_fragment_container, smartPhoneAlbumFragment, "SmartPhoneFragment")
                    .commit();
        } else {
            Log.e(TAG, "Error in creating fragment");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_album, container, false);
        mContext = fragmentView.getContext();

        Log.i(TAG, TAG + "===onCreateView===");
        findViews(fragmentView);

        return fragmentView;
    }

    private void findViews(View fragmentView) {

        statusView = (StatusView) fragmentView.findViewById(R.id.status);
        albumListBackBtn = (Button) fragmentView.findViewById(R.id.album_backbtn);
        albumMenuOption = (RelativeLayout) fragmentView.findViewById(R.id.album_menu_option);
        albumDroneSwitchBtn = (Button) fragmentView.findViewById(R.id.drone_switchbtn);
        albumSmartPhoneSwitchBtn = (Button) fragmentView.findViewById(R.id.smartphone_switchbtn);
        albumSmartPhoneSwitchBtn.setOnClickListener(albumSwitchBtnListener);
        albumDroneSwitchBtn.setOnClickListener(albumSwitchBtnListener);
        albumDroneSwitchBtn.performClick();

        rubbishBinBtn = (ImageButton) fragmentView.findViewById(R.id.rubbish_bin_btn);
        rubbishBinBtn.setOnClickListener(rubbishBinBtnAction);


        deleteOptionLayout = (LinearLayout) fragmentView.findViewById(R.id.delete_option_layout);
        deleteBtn = (Button) fragmentView.findViewById(R.id.delete_btn);
        cancelDeleteBtn = (Button) fragmentView.findViewById(R.id.cancel_btn);
        deleteBtn.setOnClickListener(deleteGroupAction);
        cancelDeleteBtn.setOnClickListener(deleteGroupAction);
        albumListBackBtn.setOnClickListener(albumListBackBtnAction);

        deleteDialog = AppUtils.getAlertDialog(mContext, mContext.getResources().getString(R.string.delete_files), mContext.getResources().getString(R.string.btn_ok), mContext.getResources().getString(R.string.btn_cancel), deleteDialogOKListener);
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
//            albumSwitch.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener albumListBackBtnAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            getFragmentManager().popBackStack();
        }

    };


    private View.OnClickListener deleteGroupAction = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.delete_btn:

                    deleteDialog.show();
                    break;
                case R.id.cancel_btn:

                    ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).hideDeleteOption();
                    // set delete option and hide the rubbish bin button
                    deleteOptionLayout.setVisibility(View.GONE);
                    rubbishBinBtn.setVisibility(View.VISIBLE);
//                    albumSwitch.setVisibility(View.VISIBLE);
                    ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).deleteSelectedPathAryList();
                    break;
            }


        }
    };


    // album switch listener
    private View.OnClickListener albumSwitchBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            albumFragmentTransaction = fragmentChildManager.beginTransaction();

            if (v.getId() == R.id.drone_switchbtn) {

                albumDroneSwitchBtn.setSelected(true);
                albumSmartPhoneSwitchBtn.setSelected(false);
                albumMenuOption.setVisibility(View.INVISIBLE);

                if (isDroneOrSmartphoneMode == false)
                    return;
                Log.d(TAG, "---click on the drone switch button---");
                if (droneAlbumFragment != null) {
                    albumFragmentTransaction
                            .replace(R.id.album_fragment_container, droneAlbumFragment, "DroneAlbumFragment")
                            .commit();

                } else {
                    Log.e(TAG, "Error in creating fragment");
                }
                isDroneOrSmartphoneMode = false;


            } else {

                albumDroneSwitchBtn.setSelected(false);
                albumSmartPhoneSwitchBtn.setSelected(true);
                albumMenuOption.setVisibility(View.VISIBLE);

                if (isDroneOrSmartphoneMode == true)
                    return;
                Log.d(TAG, "---click on the smartphone switch button---");
                if (smartPhoneAlbumFragment != null) {
                    albumFragmentTransaction
                            .replace(R.id.album_fragment_container, smartPhoneAlbumFragment, "SmartPhoneFragment")
                            .commit();

                } else {
                    Log.e(TAG, "Error in creating fragment");
                }
                isDroneOrSmartphoneMode = true;
            }
        }
    };


    @Override
    public void onPause() {
        super.onPause();

        // set delete option and hide the rubbish bin button
        deleteOptionLayout.setVisibility(View.GONE);
        rubbishBinBtn.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (smartPhoneAlbumFragment != null && isDroneOrSmartphoneMode) {
            Log.i(TAG, TAG + "smartPhoneAlbumFragment onResume");
            ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).refreshData();
        }

//        if (droneAlbumFragment != null && (!isDroneOrSmartphoneMode)) {
//            Log.i(TAG, TAG +"droneAlbumFragment onResume");
//            ((AlbumDroneTagFragment) droneAlbumFragment).refreshListData();
//        }
    }

    // delete dialog ok listener
    private View.OnClickListener deleteDialogOKListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).deleteSelectMediaFile();
            ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).hideDeleteOption();
            deleteDialog.dismiss();
//            albumSwitch.setVisibility(View.VISIBLE);
            rubbishBinBtn.setVisibility(View.VISIBLE);
            deleteOptionLayout.setVisibility(View.GONE);
        }
    };

    private FragmentManager.OnBackStackChangedListener backStackChangedListener() {
        FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                Log.i(TAG, TAG + "===backStackChangedListener===");
                if (isDroneOrSmartphoneMode) {
                    if (smartPhoneAlbumFragment != null) {
                        ((AlbumSmartPhoneTagFragment) smartPhoneAlbumFragment).refreshData();
                    }
                }
            }
        };
        return result;
    }

    @Override
    public void onBatteryUpdate(final int battery) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setBatteryStatus(battery);
            }
        });

    }

    @Override
    public void onAltitudeUpdate(float altitude) {

    }


    @Override
    public void onRadioSignalUpdate(int rssi) {

    }

    @Override
    public void onSpeedUpdate(float groundSpeed) {

    }

    @Override
    public void onLocationUpdate(long lat, long lon, int eph) {

    }

    @Override
    public void onHeadingUpdate(int heading) {

    }

}