package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

    private TextView tx_lat, tx_lng;
    private AbstractWheel altitudeWheel;
    private static final String TAG = TapAndGoDialogFragment.class.getSimpleName();

    private int tapGo_altitude;
    private float tapGo_lat, tapGo_lng;

    public static TapAndGoDialogFragment newInstance(int altitude, float latitude, float longitude) {
        TapAndGoDialogFragment f = new TapAndGoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_ALTITUDE, altitude);
        args.putFloat(ARGUMENT_LATITUDE, latitude);
        args.putFloat(ARGUMENT_LONGITUDE, longitude);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_tap_and_go, container, false);
        findviews(fragmentView);

        return fragmentView;
    }

    private void findviews(View fragmentView) {
        tx_lat = (TextView) fragmentView.findViewById(R.id.text_tap_and_go_lat);
        tx_lng = (TextView) fragmentView.findViewById(R.id.text_tap_and_go_lng);

        altitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.tap_and_go_altitude_wheel);
        altitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, 0, 20, "%01d"));
        altitudeWheel.setCyclic(false);
        altitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {

            }
        });

        final Button btn_go = (Button) fragmentView.findViewById(R.id.btn_tap_and_go_go);
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btn_go");
                PlanningFragment.hideTapAndGoDialogFragment(true, tapGo_altitude, tapGo_lat, tapGo_lng);
            }
        });

        final TextView btn_cancel = (TextView) fragmentView.findViewById(R.id.btn_tap_and_go_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlanningFragment.hideTapAndGoDialogFragment(false, 0, 0, 0);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setviews();
    }

    private void setviews() {
        Bundle arguments = getArguments();

        tapGo_altitude = arguments.getInt(ARGUMENT_ALTITUDE);
        tapGo_lat = arguments.getFloat(ARGUMENT_LATITUDE);
        tapGo_lng = arguments.getFloat(ARGUMENT_LONGITUDE);

        if (arguments != null) {
            altitudeWheel.setCurrentItem(tapGo_altitude);
            tx_lat.setText(String.format("%.07f", tapGo_lat));
            tx_lng.setText(String.format("%.07f", tapGo_lng));
        }
    }
}
