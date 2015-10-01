package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;

import org.mavlink.messages.MAV_CMD;

/**
 * Created by karot.chuang on 2015/6/23.
 */
public class MissionItemDetailFragment extends Fragment {
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

    private final static int WAYPOINT_INDEX = 0;
    private final static int TAKEOFF_INDEX = 1;
    private final static int LAND_INDEX = 2;
    private final static int RTL_INDEX = 3;

    private TextView mSerialNumberTextView = null;
    private TextView mLatitudeTextView = null;
    private TextView mLongitudeTextView = null;
    private Spinner mType = null;
    private ImageView mTypeImageView = null;
    private AbstractWheel mAltitudeWheel = null;
    private AbstractWheel mDelayWheel = null;
    private PlanningFragment mPlanningFragment;

    public static MissionItemDetailFragment newInstance(int index, Mission mission) {
        MissionItemDetailFragment fragment = new MissionItemDetailFragment();
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
        findViews(fragmentView);
        return fragmentView;
    }

    private void findViews(View fragmentView) {
        mSerialNumberTextView = (TextView) fragmentView.findViewById(R.id.way_point_detail_name_text);
        mLatitudeTextView = (TextView) fragmentView.findViewById(R.id.way_point_detail_lat_text);
        mLongitudeTextView = (TextView) fragmentView.findViewById(R.id.way_point_detail_lng_text);
        mType = (Spinner) fragmentView.findViewById(R.id.way_point_detail_type_spinner);
        mTypeImageView = (ImageView) fragmentView.findViewById(R.id.way_point_detail_type_icon);
        fragmentView.findViewById(R.id.btn_detail_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PlanningFragment) getParentFragment()).deleteSelectedMission();
            }
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity().getBaseContext(), R.layout.way_point_detail_type_spinner_text_layout, POINT_TYPE);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_waypoint_detail_dropdown_style);
        mType.setAdapter(spinnerAdapter);
        mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case WAYPOINT_INDEX: // Waypoint
                        mPlanningFragment.setItemMissionType(Mission.Type.WAY_POINT);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_waypoint);
                        break;
                    case TAKEOFF_INDEX: // Take Off
                        mPlanningFragment.setItemMissionType(Mission.Type.TAKEOFF);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_takeoff);
                        break;
                    case LAND_INDEX: // Land
                        mPlanningFragment.setItemMissionType(Mission.Type.LAND);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_land);
                        break;
                    case RTL_INDEX: // RTL_INDEX
                        mPlanningFragment.setItemMissionType(Mission.Type.RTL);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_home);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAltitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.altitude_wheel);
        mAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, 0, 20, "%01d"));
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mPlanningFragment.setItemMissionAltitude((float) newValue);
            }
        });

        mDelayWheel = (AbstractWheel) fragmentView.findViewById(R.id.delay_wheel);
        mDelayWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, 0, 99, "%01d"));
        mDelayWheel.setCyclic(false);
        mDelayWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mPlanningFragment.setItemMissionDelay(newValue);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPlanningFragment = (PlanningFragment) getParentFragment();
        initView();
    }

    private void initView() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            mSerialNumberTextView.setText(String.valueOf(arguments.getInt(ARGUMENT_INDEX)));
            mLatitudeTextView.setText(String.valueOf(arguments.getFloat(ARGUMENT_LATITUDE)));
            mLongitudeTextView.setText(String.valueOf(arguments.getFloat(ARGUMENT_LONGITUDE)));
            mType.setSelection(typeToIndex(arguments.getInt(ARGUMENT_TYPE)));
            switch (arguments.getInt(ARGUMENT_TYPE)) {
                case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_takeoff);
                    break;
                case MAV_CMD.MAV_CMD_NAV_LAND:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_land);
                    break;
                case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_home);
                    break;
                default:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_waypoint);
                    break;
            }
            mAltitudeWheel.setCurrentItem((int) arguments.getFloat(ARGUMENT_ALTITUDE));
            mDelayWheel.setCurrentItem(arguments.getInt(ARGUMENT_DELAY));
        }
    }

    private int typeToIndex(int type) {
        switch (type) {
            case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
                return TAKEOFF_INDEX;
            case MAV_CMD.MAV_CMD_NAV_LAND:
                return LAND_INDEX;
            case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
                return RTL_INDEX;
            case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
            default:
                return WAYPOINT_INDEX;
        }
    }
}
