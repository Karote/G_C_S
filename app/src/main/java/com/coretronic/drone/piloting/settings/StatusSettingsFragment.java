package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coretronic.drone.BuildConfig;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.service.Parameter;

/**
 * Created by jiaLian on 15/4/1.
 */
public class StatusSettingsFragment extends UnBindDrawablesFragment implements DroneController.ParameterLoaderListener {

    private MainActivity activity;
    private TextView tvFlightSoftware;
    private TextView tvFlightHardware;
    private TextView tvCameraSoftware;
    private TextView tvCameraHardware;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_status_page, container, false);
        tvFlightSoftware = (TextView) fragmentView.findViewById(R.id.tv_flight_software_version);
        tvFlightHardware = (TextView) fragmentView.findViewById(R.id.tv_flight_hardware_version);
        tvCameraSoftware = (TextView) fragmentView.findViewById(R.id.tv_camera_software_version);
        tvCameraHardware = (TextView) fragmentView.findViewById(R.id.tv_camera_hardware_version);
        TextView tvAppVersion = (TextView) fragmentView.findViewById(R.id.tv_app_version);
        tvAppVersion.setText(BuildConfig.VERSION_NAME);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.readParameters(this, Parameter.Type.FLIGHT_BOARD_SOFTWARE_VERSION, Parameter.Type.FLIGHT_BOARD_HARDWARE_VERSION,
                Parameter.Type.CAMERA_BOARD_SOFTWARE_VERSION, Parameter.Type.CAMERA_BOARD_HARDWARE_VERSION);
    }

    @Override
    public void onParameterLoaded(final Parameter.Type type, final Parameter parameter) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case FLIGHT_BOARD_HARDWARE_VERSION:
                        tvFlightHardware.setText(parameter.getValue() + "");
                        break;
                    case FLIGHT_BOARD_SOFTWARE_VERSION:
                        tvFlightSoftware.setText(parameter.getValue() + "");
                        break;
                    case CAMERA_BOARD_HARDWARE_VERSION:
                        tvCameraHardware.setText(parameter.getValue() + "");
                        break;
                    case CAMERA_BOARD_SOFTWARE_VERSION:
                        tvCameraSoftware.setText(parameter.getValue() + "");
                        break;
                }
            }
        });
    }
}
