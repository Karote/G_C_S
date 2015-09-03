package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;

import org.mavlink.messages.MAV_CMD;

import java.util.Arrays;

/**
 * Created by karot.chuang on 2015/6/23.
 */
public class WaypointDetailFragment extends Fragment {
    private final static String ARGUMENT_INDEX = "index";
    private final static String ARGUMENT_TYPE = "type";
    private final static String ARGUMENT_ALTITUDE = "altitude";
    private final static String ARGUMENT_DELAY = "delay";
    private final static String ARGUMENT_LATITUDE = "latitude";
    private final static String ARGUMENT_LONGITUDE = "longitude";
    private final static String TXT_WAYPOINT = "Waypoint";
    private final static String TXT_TAKEOFF = "Take Off";
    private final static String TXT_LAND = "Land";
    private final static String TXT_RTL = "RTL";
    private final static String[] POINT_TYPE = {TXT_WAYPOINT, TXT_TAKEOFF, TXT_LAND, TXT_RTL};

    private TextView tx_name = null;
    private TextView tx_lat = null;
    private TextView tx_lng = null;
    private Spinner spinner_type = null;
    private View icon_type = null;
    private AbstractWheel altitudeWheel = null;
    private AbstractWheel delayWheel = null;

    public static WaypointDetailFragment newInstance(int index, Mission mission) {
        WaypointDetailFragment fragment = new WaypointDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_INDEX, index);
        args.putInt(ARGUMENT_TYPE, mission.getType() == null ? Type.WAY_POINT.getId() : mission.getType().getId());
        args.putFloat(ARGUMENT_ALTITUDE, mission.getAltitude());
        args.putInt(ARGUMENT_DELAY, mission.getWaitSeconds());
        args.putFloat(ARGUMENT_LATITUDE, mission.getLatitude());
        args.putFloat(ARGUMENT_LONGITUDE, mission.getLongitude());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_waypoint_detail, container, false);
        findviews(fragmentView);

        return fragmentView;
    }

    private void findviews(View fragmentView) {
        tx_name = (TextView) fragmentView.findViewById(R.id.text_detail_waypoint_name);
        tx_lat = (TextView) fragmentView.findViewById(R.id.text_waypoint_lat);
        tx_lng = (TextView) fragmentView.findViewById(R.id.text_waypoint_lng);
        spinner_type = (Spinner) fragmentView.findViewById(R.id.spinner_detail_waypoint_type);
        icon_type = (View) fragmentView.findViewById(R.id.icon_detail_waypoint_type);
        final Button bt_delete = (Button) fragmentView.findViewById(R.id.btn_detail_delete);
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PlanningFragment) getParentFragment()).deleteItemMission();
            }
        });

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.spinner_waypoint_detail_style, POINT_TYPE);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_waypoint_detail_dropdown_style);
        spinner_type.setAdapter(spinnerAdapter);
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MissionType", "position:" + position);
                switch (position) {
                    case 0: // Waypoint
                        ((PlanningFragment) getParentFragment()).setItemMissionType(Mission.Type.WAY_POINT);
                        icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_waypoint);
                        break;
                    case 1: // Take Off
                        ((PlanningFragment) getParentFragment()).setItemMissionType(Mission.Type.TAKEOFF);
                        icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_takeoff);
                        break;
                    case 2: // Land
                        ((PlanningFragment) getParentFragment()).setItemMissionType(Mission.Type.LAND);
                        icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_land);
                        break;
                    case 3: // RTL
                        ((PlanningFragment) getParentFragment()).setItemMissionType(Mission.Type.RTL);
                        icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_home);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        altitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.altitude_wheel);
        altitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, 0, 20, "%01d"));
        altitudeWheel.setCyclic(false);
        altitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                ((PlanningFragment) getParentFragment()).setItemMissionAltitude((float) newValue);
            }
        });

        delayWheel = (AbstractWheel) fragmentView.findViewById(R.id.delay_wheel);
        delayWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, 0, 99, "%01d"));
        delayWheel.setCyclic(false);
        delayWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                ((PlanningFragment) getParentFragment()).setItemMissionDelay(newValue);
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

        if (arguments != null) {
            tx_name.setText(String.valueOf(arguments.getInt(ARGUMENT_INDEX)));
            tx_lat.setText(String.valueOf(arguments.getFloat(ARGUMENT_LATITUDE)));
            tx_lng.setText(String.valueOf(arguments.getFloat(ARGUMENT_LONGITUDE)));
            spinner_type.setSelection(typeToIndex(arguments.getInt(ARGUMENT_TYPE)));
            switch (arguments.getInt(ARGUMENT_TYPE)) {
                case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
                    icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_takeoff);
                    break;
                case MAV_CMD.MAV_CMD_NAV_LAND:
                    icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_land);
                    break;
                case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
                    icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_home);
                    break;
                default:
                    icon_type.setBackgroundResource(R.drawable.ico_indicator_plan_waypoint);
                    break;
            }
            altitudeWheel.setCurrentItem((int) arguments.getFloat(ARGUMENT_ALTITUDE));
            delayWheel.setCurrentItem(arguments.getInt(ARGUMENT_DELAY));
        }
    }

    private int typeToIndex(int type) {
        switch (type) {
            case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
                return Arrays.asList(POINT_TYPE).indexOf(TXT_TAKEOFF);
            case MAV_CMD.MAV_CMD_NAV_LAND:
                return Arrays.asList(POINT_TYPE).indexOf(TXT_LAND);
            case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
                return Arrays.asList(POINT_TYPE).indexOf(TXT_RTL);
            case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
            default:
                return Arrays.asList(POINT_TYPE).indexOf(TXT_WAYPOINT);
        }
    }
}
