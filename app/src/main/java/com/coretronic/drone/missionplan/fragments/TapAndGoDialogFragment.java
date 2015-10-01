package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;

/**
 * Created by karot.chuang on 2015/6/23.
 */
public class TapAndGoDialogFragment extends Fragment {
    private static final String ARGUMENT_ALTITUDE = "altitude";
    private static final String ARGUMENT_LATITUDE = "latitude";
    private static final String ARGUMENT_LONGITUDE = "longitude";

    private TextView mLocationTextView;
    private AbstractWheel mAltitudeWheel;

    private int mMissionAltitude;
    private float mMissionLatitude, mMissionLongitude;

    public static TapAndGoDialogFragment newInstance(int altitude, float latitude, float longitude) {
        TapAndGoDialogFragment fragment = new TapAndGoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_ALTITUDE, altitude);
        args.putFloat(ARGUMENT_LATITUDE, latitude);
        args.putFloat(ARGUMENT_LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_tap_and_go_dialog, container, false);
        findViews(fragmentView);
        return fragmentView;
    }

    private void findViews(View fragmentView) {

        mLocationTextView = (TextView) fragmentView.findViewById(R.id.tap_and_go_location_text);
        mAltitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.tap_and_go_altitude_wheel);
        mAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.tap_and_go_altitude_spinner_wheel_text_layout, 0, 20, "%01d"));
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mMissionAltitude = newValue;
            }
        });
        fragmentView.findViewById(R.id.tap_and_go_start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TapAndGoFragment) getParentFragment()).executeTapAndGoMission(mMissionAltitude, mMissionLatitude, mMissionLongitude);
                dismiss();
            }
        });

        fragmentView.findViewById(R.id.tap_and_go_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void dismiss() {
        getParentFragment().getChildFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setViews();
    }

    private void setViews() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            mMissionAltitude = arguments.getInt(ARGUMENT_ALTITUDE);
            mMissionLatitude = arguments.getFloat(ARGUMENT_LATITUDE);
            mMissionLongitude = arguments.getFloat(ARGUMENT_LONGITUDE);

            mAltitudeWheel.setCurrentItem(mMissionAltitude);
            mLocationTextView.setText(String.format("%.07f,", mMissionLatitude) + String.format("%.07f", mMissionLongitude));
        }
    }
}
