package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private TextView location_text;
    private AbstractWheel altitudeWheel;

    private int tapGo_altitude;
    private float tapGo_lat, tapGo_lng;

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
        location_text = (TextView) fragmentView.findViewById(R.id.tap_and_go_location_text);

        altitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.tap_and_go_altitude_wheel);
        altitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.tap_and_go_altitude_spinner_wheel_text_layout, 0, 20, "%01d"));
        altitudeWheel.setCyclic(false);
        altitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                tapGo_altitude = newValue;
            }
        });

        final Button btn_go = (Button) fragmentView.findViewById(R.id.tap_and_go_start_button);
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TapAndGoFragment) getParentFragment()).hideTapAndGoDialogFragment(true, tapGo_altitude, tapGo_lat, tapGo_lng);
            }
        });

        final TextView btn_cancel = (TextView) fragmentView.findViewById(R.id.tap_and_go_cancel_button);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TapAndGoFragment) getParentFragment()).hideTapAndGoDialogFragment(false, 0, 0, 0);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setViews();
    }

    private void setViews() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            tapGo_altitude = arguments.getInt(ARGUMENT_ALTITUDE);
            tapGo_lat = arguments.getFloat(ARGUMENT_LATITUDE);
            tapGo_lng = arguments.getFloat(ARGUMENT_LONGITUDE);

            altitudeWheel.setCurrentItem(tapGo_altitude);
            location_text.setText(String.format("%.07f,", tapGo_lat) + String.format("%.07f", tapGo_lng));
        }
    }
}
