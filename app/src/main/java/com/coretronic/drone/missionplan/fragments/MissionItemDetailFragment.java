package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelScrollListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;

/**
 * Created by karot.chuang on 2015/6/23.
 */
public class MissionItemDetailFragment extends Fragment {
    private final static String ARGUMENT_INDEX = "index";
    private final static String ARGUMENT_TYPE = "type";
    private final static String ARGUMENT_ALTITUDE = "altitude";
    private final static String ARGUMENT_STAY = "stay";
    private final static String ARGUMENT_LATITUDE = "latitude";
    private final static String ARGUMENT_LONGITUDE = "longitude";
    private final static String TXT_WAYPOINT = "Waypoint";
    private final static String TXT_TAKEOFF = "Take Off";
    private final static String TXT_LAND = "Land";
    private final static String TXT_RTL = "RTL";
    private final static String TXT_CAMERA = "Camera";
    private final static String[] POINT_TYPE = {TXT_WAYPOINT, TXT_TAKEOFF, TXT_LAND, TXT_RTL, TXT_CAMERA};

    private final static int WAYPOINT_INDEX = 0;
    private final static int TAKEOFF_INDEX = 1;
    private final static int LAND_INDEX = 2;
    private final static int RTL_INDEX = 3;
    private final static int CAMERA_INDEX = 4;
    public static final int ALTITUDE_MIN_VALUE = 1;
    public static final int ALTITUDE_MAX_VALUE = 200;
    public static final int DELAY_MIN_VALUE = 0;
    public static final int DELAY_MAX_VALUE = 30;

    private TextView mSerialNumberTextView = null;
    private EditText mLatitudeTextView = null;
    private EditText mLongitudeTextView = null;
    private Spinner mType = null;
    private ImageView mTypeImageView = null;
    private AbstractWheel mAltitudeWheel = null;
    private AbstractWheel mDelayWheel = null;
    private SelectedMissionUpdatedCallback mSelectedMissionUpdatedCallback;
    private int mWheelScrollingCount;

    public static MissionItemDetailFragment newInstance(int index, Mission mission) {
        MissionItemDetailFragment fragment = new MissionItemDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_INDEX, index);
        args.putSerializable(ARGUMENT_TYPE, mission.getType() == null ? Type.WAY_POINT : mission.getType());
        args.putFloat(ARGUMENT_ALTITUDE, mission.getAltitude());
        args.putInt(ARGUMENT_STAY, mission.getWaitSeconds());
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
        mLatitudeTextView = (EditText) fragmentView.findViewById(R.id.way_point_detail_lat_text);
        mLongitudeTextView = (EditText) fragmentView.findViewById(R.id.way_point_detail_lng_text);
        mLatitudeTextView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSelectedMissionUpdatedCallback.onMissionLatitudeUpdate(Float.parseFloat(v.getText().toString()));
                }
                return false;
            }
        });

        mLongitudeTextView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSelectedMissionUpdatedCallback.onMissionLongitudeUpdate(Float.parseFloat(v.getText().toString()));
                }
                return false;
            }
        });
        mType = (Spinner) fragmentView.findViewById(R.id.way_point_detail_type_spinner);
        mTypeImageView = (ImageView) fragmentView.findViewById(R.id.way_point_detail_type_icon);
        fragmentView.findViewById(R.id.btn_detail_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMissionUpdatedCallback.onMissionDeleted();
            }
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity().getBaseContext(), R.layout.way_point_detail_type_spinner_text_layout, POINT_TYPE);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_waypoint_detail_dropdown_style);
        mType.setAdapter(spinnerAdapter);
        mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            protected Adapter initializedAdapter = null;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initializedAdapter != parent.getAdapter()) {
                    initializedAdapter = parent.getAdapter();
                    return;
                }
                switch (position) {
                    case WAYPOINT_INDEX: // Waypoint
                        mSelectedMissionUpdatedCallback.onMissionTypeUpdate(Mission.Type.WAY_POINT);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_waypoint);
                        break;
                    case TAKEOFF_INDEX: // Take Off
                        mSelectedMissionUpdatedCallback.onMissionTypeUpdate(Mission.Type.TAKEOFF);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_takeoff);
                        break;
                    case LAND_INDEX: // Land
                        mSelectedMissionUpdatedCallback.onMissionTypeUpdate(Mission.Type.LAND);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_landing);
                        break;
                    case RTL_INDEX: // RTL_INDEX
                        mSelectedMissionUpdatedCallback.onMissionTypeUpdate(Mission.Type.RTL);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_home);
                        break;
                    case CAMERA_INDEX:
                        mSelectedMissionUpdatedCallback.onMissionTypeUpdate(Mission.Type.CAMERA_TRIGGER_DISTANCE);
                        mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_camera);
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAltitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.altitude_wheel);
        mAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, ALTITUDE_MIN_VALUE, ALTITUDE_MAX_VALUE, "%01d"));
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.addScrollingListener(new LockedWheelScrollingListener());

        mDelayWheel = (AbstractWheel) fragmentView.findViewById(R.id.delay_wheel);
        mDelayWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, DELAY_MIN_VALUE, DELAY_MAX_VALUE, "%01d"));
        mDelayWheel.setCyclic(false);
        mDelayWheel.addScrollingListener(new LockedWheelScrollingListener());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSelectedMissionUpdatedCallback = (SelectedMissionUpdatedCallback) getParentFragment();
        initView();
    }

    private void initView() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            mSerialNumberTextView.setText(String.valueOf(arguments.getInt(ARGUMENT_INDEX)));
            mLatitudeTextView.setText(String.valueOf(arguments.getFloat(ARGUMENT_LATITUDE)));
            mLongitudeTextView.setText(String.valueOf(arguments.getFloat(ARGUMENT_LONGITUDE)));

            Mission.Type missionType = (Mission.Type) arguments.getSerializable(ARGUMENT_TYPE);
            mType.setSelection(typeToIndex(missionType));
            switch (missionType) {
                case TAKEOFF:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_takeoff);
                    break;
                case LAND:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_landing);
                    break;
                case RTL:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_home);
                    break;
                case CAMERA_TRIGGER_DISTANCE:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_camera);
                    break;
                default:
                    mTypeImageView.setImageResource(R.drawable.ico_indicator_plan_waypoint);
                    break;
            }
            mAltitudeWheel.setCurrentItem((int) arguments.getFloat(ARGUMENT_ALTITUDE) - ALTITUDE_MIN_VALUE);
            mDelayWheel.setCurrentItem(arguments.getInt(ARGUMENT_STAY));
        }
    }

    private int typeToIndex(Mission.Type type) {
        switch (type) {
            case TAKEOFF:
                return TAKEOFF_INDEX;
            case LAND:
                return LAND_INDEX;
            case RTL:
                return RTL_INDEX;
            case CAMERA_TRIGGER_DISTANCE:
                return CAMERA_INDEX;
            case WAY_POINT:
            default:
                return WAYPOINT_INDEX;
        }
    }

    private void updateAltitudeValue(int altitude) {
        mSelectedMissionUpdatedCallback.onMissionAltitudeUpdate((float) altitude + ALTITUDE_MIN_VALUE);
    }

    private void updateDelayValue(int delay) {
        mSelectedMissionUpdatedCallback.onMissionDelayUpdate(delay);
    }

    private void lockWheelScrolling() {
        synchronized (MissionItemDetailFragment.class) {
            mWheelScrollingCount++;
        }
    }

    private void releaseWheelScrolling() {
        synchronized (MissionItemDetailFragment.class) {
            mWheelScrollingCount--;
        }
    }

    private boolean isWheelingLocked() {
        synchronized (MissionItemDetailFragment.class) {
            return mWheelScrollingCount != 0;
        }
    }

    private class LockedWheelScrollingListener implements OnWheelScrollListener {
        private int oldValue;

        @Override
        public void onScrollingStarted(AbstractWheel wheel) {
            lockWheelScrolling();
            oldValue = wheel.getCurrentItem();
        }

        @Override
        public void onScrollingFinished(AbstractWheel wheel) {
            releaseWheelScrolling();
            if (isWheelingLocked() || oldValue == wheel.getCurrentItem()) {
                return;
            }
            try {
                switch (wheel.getId()) {
                    case R.id.altitude_wheel:
                        updateAltitudeValue(wheel.getCurrentItem());
                        break;
                    case R.id.delay_wheel:
                        updateDelayValue(wheel.getCurrentItem());
                        break;
                }
            } catch (Exception e) {
                wheel.setCurrentItem(oldValue);
            }
        }
    }
}
