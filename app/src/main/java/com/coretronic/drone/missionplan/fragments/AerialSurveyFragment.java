package com.coretronic.drone.missionplan.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneController.MissionLoaderListener;
import com.coretronic.drone.R;
import com.coretronic.drone.controller.SimpleDroneController;
import com.coretronic.drone.missionplan.adapter.MissionListUndoableAdapter;
import com.coretronic.drone.missionplan.adapter.MissionListUndoableAdapter.OnListStateChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelScrollListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.survey.RouterBuilder;
import com.coretronic.drone.survey.RouterBuilder.SurveyBuilderException;
import com.coretronic.drone.survey.SurveyRouter;
import com.coretronic.drone.util.Utils;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.survey.SurveyData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class AerialSurveyFragment extends MapChildFragment implements SelectedMissionUpdatedCallback {

    private final static String SQUARE_SYMBOL = "\u00B2";
    private final static String VALUE_FORMAT = "%.01f";
    private final static String LENGTH_FORMAT = VALUE_FORMAT + "m";
    private final static String GSD_FORMAT = VALUE_FORMAT + "mm" + SQUARE_SYMBOL + "/px";
    private final static String FOOTPRINT_FORMAT = LENGTH_FORMAT + "X" + LENGTH_FORMAT;
    private final static String AREA_FORMAT = LENGTH_FORMAT + SQUARE_SYMBOL;
    private final static double DEFAULT_DRONE_SPEED = 3;

    private static final int INIT_STATUS = 1;
    private static final int SCOPE_STATUS = 2;
    private static final int FOOTPRINT_STATUS = 3;
    private static final int ROUTE_CREATED_STATUS = 4;
    private static final int PLAN_GO_STATUS = 5;

    private static final int HATCH_ANGLE_MIN_VALUE = 0;
    private static final int HATCH_ANGLE_MAX_VALUE = 180;
    private static final int ALTITUDE_MIN_VALUE = 1;
    private static final int ALTITUDE_MAX_VALUE = 20;
    private static final int ALTITUDE_GAP = 10;
    private static final int OVERLAP_MIN_VALUE = 50;
    private static final int OVERLAP_MAX_VALUE = 90;
    private static final int SIDELAP_MIN_VALUE = 15;
    private static final int SIDELAP_MAX_VALUE = 90;

    private final static int SELECT_NONE = 0;
    private final static int SELECTED_ONE = 1;
    private final static int SELECT_ALL = 2;

    private final static float DRONE_LOCATION_INVALID = Float.MAX_VALUE;

    private MissionListUndoableAdapter mMissionItemAdapter;

    private View mWaypointListTopView;
    private View mWaypointListEditHeaderPanel;
    private View mWaypointListHeaderPanel;
    private TextView mWaypointListHeaderCountText;
    private ToggleButton mWaypointListHeaderSelectAllToggleButton;
    private View mWaypointListHeaderDeleteButton;
    private View mWaypointListHeaderSettingButton;
    private WaypointListEditSettingDialog mWaypointSettingDialog;

    private FrameLayout mWayPointDetailPanel;
    private MissionItemDetailFragment mMissionItemDetailFragment;
    private ProgressDialog mLoadMissionProgressDialog;
    private View mRoutePropertiesDialog = null;
    private View mDistanceAndTimeInfo = null;
    private View mRouteDetailInfo = null;
    private TextView mDistanceValue;
    private TextView mDurationValue;
    private TextView mFootprintValue;
    private TextView mGSDValue;
    private TextView mLongitudinalValue;
    private TextView mLateralValue;
    private TextView mAreaValue;
    private TextView mPictureValue;
    private TextView mStripsValue;
    private Button mCreateRouteButton = null;
    private View mGoEditPanel = null;
    private RouterBuilder mRouterBuilder;
    private List<Coord2D> mPolygonPoints;
    private SurveyRouter mAerialSurveyRouter;

    private AbstractWheel mHatchAngleWheel;
    private AbstractWheel mAltitudeWheel;
    private AbstractWheel mOverlapWheel;
    private AbstractWheel mSidelapWheel;
    private TextView mCameraTextView;

    private int mWheelScrollingCount;
    private int mTopViewVisibility;

    private float mDroneLat = DRONE_LOCATION_INVALID;
    private float mDroneLon = DRONE_LOCATION_INVALID;
    private View mRouteGoButton;
    private View mWaypointListEditButton;

    public static AerialSurveyFragment newInstance() {
        AerialSurveyFragment fragment = new AerialSurveyFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRouterBuilder = new RouterBuilder().setAutoChoose(true);
        mPolygonPoints = new ArrayList<>();
        changeLayoutStatus(INIT_STATUS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_aerial_survey, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mission List
        mWaypointListTopView = view.findViewById(R.id.waypoint_list_top_view);
        mWaypointListTopView.setVisibility(View.GONE);

        mWaypointListEditHeaderPanel = mWaypointListTopView.findViewById(R.id.waypoint_list_edit_header_panel);
        mWaypointListHeaderPanel = mWaypointListTopView.findViewById(R.id.waypoint_list_header_panel);
        mWaypointListHeaderCountText = (TextView) mWaypointListTopView.findViewById(R.id.waypoint_list_header_count_text);
        mWaypointListHeaderSelectAllToggleButton = (ToggleButton) mWaypointListTopView.findViewById(R.id.waypoint_list_header_select_all_toggle_button);
        mWaypointListHeaderSelectAllToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWaypointListHeaderSelectAllToggleButton.isChecked()) {
                    mMissionItemAdapter.setAllItemChecked(true);
                    setEditOptionButtonState(SELECT_ALL, 0);
                } else {
                    mMissionItemAdapter.setAllItemChecked(false);
                    setEditOptionButtonState(SELECT_NONE, 0);
                }
            }
        });
        mWaypointListEditButton = mWaypointListTopView.findViewById(R.id.waypoint_list_header_edit_text);
        mWaypointListEditButton.setOnClickListener(missionListEditFunctionListener);
        mWaypointListHeaderDeleteButton = mWaypointListTopView.findViewById(R.id.waypoint_list_header_delete_button);
        mWaypointListHeaderDeleteButton.setEnabled(false);
        mWaypointListHeaderDeleteButton.setOnClickListener(missionListEditFunctionListener);
        mWaypointListHeaderSettingButton = mWaypointListTopView.findViewById(R.id.waypoint_list_header_setting_button);
        mWaypointListHeaderSettingButton.setEnabled(false);
        mWaypointListHeaderSettingButton.setOnClickListener(missionListEditFunctionListener);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new FixedLinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mMissionItemAdapter = new MissionListUndoableAdapter();
        recyclerView.setAdapter(mMissionItemAdapter);

        mWayPointDetailPanel = (FrameLayout) view.findViewById(R.id.way_point_detail_container);
        mWayPointDetailPanel.setVisibility(View.GONE);
        mGoEditPanel = view.findViewById(R.id.go_edit_button_layout);
        mGoEditPanel.setVisibility(View.GONE);
        mRouteGoButton = view.findViewById(R.id.route_go_button);
        mRouteGoButton.setOnClickListener(onPlanningBtnClickListener);
        mRouteGoButton.setEnabled(false);
        view.findViewById(R.id.route_edit_button).setOnClickListener(onPlanningBtnClickListener);

        mMissionItemAdapter.setOnAdapterListChangedListener(new OnListStateChangedListener() {
            @Override
            public void onItemDeleted(int position) {
                updatePolygon();
            }

            @Override
            public void onItemSelected(Mission mission, int currentIndex) {
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                mMissionItemDetailFragment = MissionItemDetailFragment.newInstance(currentIndex, mission);
                fragmentTransaction.replace(R.id.way_point_detail_container, mMissionItemDetailFragment, "DetailFragment").commit();
                mWayPointDetailPanel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected() {
                mWayPointDetailPanel.setVisibility(View.GONE);
            }

            @Override
            public void onUndoListIsEmptyOrNot(boolean empty) {
                mMapViewFragment.setUndoButtonEnable(!empty);
            }

            @Override
            public void onAdapterListIsEmptyOrNot(boolean isEmpty) {
                mTopViewVisibility = isEmpty ? View.GONE : View.VISIBLE;
                mWaypointListTopView.setVisibility(mTopViewVisibility);
                if (mDroneController != SimpleDroneController.FAKE_DRONE) {
                    mRouteGoButton.setEnabled(!isEmpty);
                }
            }

            @Override
            public void onItemChecked(int checkCount) {
                int selectState;
                if (checkCount == mMissionItemAdapter.getItemCount() - 1) {
                    selectState = SELECT_ALL;
                } else if (checkCount == 0) {
                    selectState = SELECT_NONE;
                } else {
                    selectState = SELECTED_ONE;
                }
                setEditOptionButtonState(selectState, checkCount);
            }

            @Override
            public void onListModified() {
                mMapViewFragment.setEditDoneEnable();
            }
        });

        mCreateRouteButton = (Button) view.findViewById(R.id.create_route_button);
        mCreateRouteButton.setOnClickListener(onPlanningBtnClickListener);
        mRoutePropertiesDialog = view.findViewById(R.id.route_properties_dialog);
        initRoutePropertiesDialog(view);
        initRouteInfo(view);
    }

    private View.OnClickListener onPlanningBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.create_route_button:
                    try {
                        updateFootprints();
                    } catch (SurveyBuilderException e) {
                        return;
                    }
                    initRoutePropertiesValue();
                    changeLayoutStatus(FOOTPRINT_STATUS);
                    break;
                case R.id.route_properties_ok_button:
                    onAerialSurveyMissionCreated();
                    changeLayoutStatus(ROUTE_CREATED_STATUS);
                    break;
                case R.id.route_properties_back_button:
                    mAerialSurveyRouter = null;
                    mMapViewFragment.clearFootprint();
                    changeLayoutStatus(SCOPE_STATUS);
                    break;
                case R.id.route_go_button:
                    List<Mission> droneMissionList = mMissionItemAdapter.getMissions();
                    if (droneMissionList == null || droneMissionList.size() == 0) {
                        showToastMessage("There is no mission existed");
                        return;
                    }
                    if (mDroneController == SimpleDroneController.FAKE_DRONE) {
                        return;
                    }
                    mDroneController.registerMissionLoaderListener(missionLoaderListener);
                    mDroneController.writeMissions(droneMissionList);
                    showLoadProgressDialog("Loading", "Please wait...");
                    break;
                case R.id.route_edit_button:
                    mMapViewFragment.clearMap();
                    mMissionItemAdapter.clearMission();
                    mMissionItemAdapter.onNothingSelected();
                    updatePolygon();
                    try {
                        updateFootprints();
                    } catch (SurveyBuilderException e) {
                        return;
                    }
                    initRoutePropertiesValue();
                    changeLayoutStatus(FOOTPRINT_STATUS);
                    break;
            }
        }
    };

    private void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void missionAdapterShowSelect(boolean isShow) {
        if (isShow) {
            mMissionItemAdapter.setSelectLayoutVisible(true);
        } else {
            mMissionItemAdapter.setSelectLayoutVisible(false);
        }
        mWayPointDetailPanel.setVisibility(View.GONE);
    }

    private void updatePolygon() {
        mMapViewFragment.updatePolygon(mPolygonPoints);
    }

    private void updateFootprints() throws SurveyBuilderException {
        try {
            mAerialSurveyRouter = mRouterBuilder.setPoints(mPolygonPoints).build();
            mMapViewFragment.updateFootprints(mAerialSurveyRouter);
        } catch (SurveyBuilderException e) {
            showToastMessage(e.getMessage());
            throw e;
        }
        updateRouteInfoValue();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLoadMissionProgressDialog != null && mLoadMissionProgressDialog.isShowing()) {
            mLoadMissionProgressDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.edit_cancel_button:
                mMissionItemAdapter.exitMissionListEditMode();
                updateMissionToMap();
                mWaypointListEditHeaderPanel.setVisibility(View.GONE);
                mWaypointListHeaderPanel.setVisibility(View.VISIBLE);
                missionAdapterShowSelect(false);
                break;
            case R.id.edit_done_button:
                mMissionItemAdapter.finishMissionListEditMode();
                updateMissionToMap();
                mWaypointListEditHeaderPanel.setVisibility(View.GONE);
                mWaypointListHeaderPanel.setVisibility(View.VISIBLE);
                missionAdapterShowSelect(false);
                break;
            case R.id.plan_stop_button:
                if (mDroneController != SimpleDroneController.FAKE_DRONE) {
                    mDroneController.stopMission();
                }
                break;
            case R.id.plan_pause_button:
                if (mDroneController != SimpleDroneController.FAKE_DRONE) {
                    mDroneController.pauseMission();
                }
                break;
            case R.id.plan_play_button:
                if (mDroneController != SimpleDroneController.FAKE_DRONE) {
                    mDroneController.resumeMission();
                }
                break;
        }
    }

    private void onAerialSurveyMissionCreated() {
        List<Mission> surveyMissions = mAerialSurveyRouter.toMissions(mDroneLat, mDroneLon);
        mMissionItemAdapter.update(surveyMissions);
        mMapViewFragment.clearSurvey();
        updateMissionToMap();
    }

    private void showLoadProgressDialog(String title, String message) {
        if (mLoadMissionProgressDialog == null) {
            mLoadMissionProgressDialog = new ProgressDialog(getActivity()) {
                @Override
                public void onBackPressed() {
                    super.onBackPressed();
                    dismiss();
                }
            };
            mLoadMissionProgressDialog.setCancelable(false);
        } else {
            mLoadMissionProgressDialog.dismiss();
        }
        mLoadMissionProgressDialog.setTitle(title);
        mLoadMissionProgressDialog.setMessage(message);
        mLoadMissionProgressDialog.show();
    }

    @Override
    public void onMapDragEndEvent(int index, float lat, float lon) {
        if (mMissionItemAdapter.getItemCount() > 0) {
            mMissionItemAdapter.updateMissionItemLocation(index, lat, lon);
        } else {
            mPolygonPoints.set(index, new Coord2D(lat, lon));
        }
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
        if (mAerialSurveyRouter != null)
            return;
        mPolygonPoints.add(new Coord2D(lat, lon));
        if (mPolygonPoints.size() > 2) {
            changeLayoutStatus(SCOPE_STATUS);
        }
        updatePolygon();
    }

    @Override
    public void onMapDragStartEvent() {
        mMissionItemAdapter.onNothingSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
    }

    @Override
    public void onMapDeleteMarker(int index) {
        index--;
        if (index < 0) {
            mPolygonPoints.clear();
        } else {
            mPolygonPoints.remove(index);
        }
        if (mPolygonPoints.size() < 3) {
            changeLayoutStatus(INIT_STATUS);
        }
        updatePolygon();
    }

    @Override
    public void onMissionLatitudeUpdate(float latitude) {
        mMissionItemAdapter.getSelectedItem().setLatitude(latitude);
        mMissionItemAdapter.notifyDataSetChanged();
        mMapViewFragment.clearMap();
        updateMissionToMap();
    }

    @Override
    public void onMissionLongitudeUpdate(float longitude) {
        mMissionItemAdapter.getSelectedItem().setLongitude(longitude);
        mMissionItemAdapter.notifyDataSetChanged();
        mMapViewFragment.clearMap();
        updateMissionToMap();
    }

    @Override
    public void onMissionTypeUpdate(Type missionType) {
        mMissionItemAdapter.getSelectedItem().setType(missionType);
        mMissionItemAdapter.notifyDataSetChanged();
        updateMissionToMap();
    }

    @Override
    public void onMissionAltitudeUpdate(float missionAltitude) {
        mMissionItemAdapter.getSelectedItem().setAltitude(missionAltitude);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionStayUpdate(int seconds) {
        mMissionItemAdapter.getSelectedItem().setWaitSeconds(seconds);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionDeleted() {
        mMissionItemAdapter.removeSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
        updateMissionToMap();
    }

    @Override
    public void onMissionSpeedUpdate(int missionSpeed) {
        mMissionItemAdapter.getSelectedItem().setSpeed(missionSpeed);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    private void updateMissionToMap() {
        mMapViewFragment.updateMissions(mMissionItemAdapter.getMissions());
        mWaypointListHeaderCountText.setText(String.valueOf(mMissionItemAdapter.getItemCount() - 1));
    }

    private void changeLayoutStatus(int status) {
        switch (status) {
            case INIT_STATUS:
                mCreateRouteButton.setVisibility(View.VISIBLE);
                mCreateRouteButton.setEnabled(false);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                mMapViewFragment.setMavInfoViewVisibility(View.GONE);
                mMapViewFragment.setDroneControlBarVisibility(View.GONE);
                mRouteDetailInfo.setVisibility(View.GONE);
                mDistanceAndTimeInfo.setVisibility(View.GONE);
                break;
            case SCOPE_STATUS:
                mCreateRouteButton.setVisibility(View.VISIBLE);
                mCreateRouteButton.setEnabled(true);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                mRouteDetailInfo.setVisibility(View.GONE);
                mDistanceAndTimeInfo.setVisibility(View.GONE);
                break;
            case FOOTPRINT_STATUS:
                mCreateRouteButton.setVisibility(View.GONE);
                mRoutePropertiesDialog.setVisibility(View.VISIBLE);
                mMapViewFragment.setMavInfoViewVisibility(View.GONE);
                mRouteDetailInfo.setVisibility(View.VISIBLE);
                mDistanceAndTimeInfo.setVisibility(View.VISIBLE);
                mGoEditPanel.setVisibility(View.GONE);
                mWayPointDetailPanel.setVisibility(View.GONE);
                break;
            case ROUTE_CREATED_STATUS:
                mRoutePropertiesDialog.setVisibility(View.GONE);
                mMapViewFragment.setMavInfoViewVisibility(View.VISIBLE);
                mMapViewFragment.setDroneControlBarVisibility(View.GONE);
                mRouteDetailInfo.setVisibility(View.GONE);
                mDistanceAndTimeInfo.setVisibility(View.GONE);
                mGoEditPanel.setVisibility(View.VISIBLE);
                break;
            case PLAN_GO_STATUS:
                mGoEditPanel.setVisibility(View.GONE);
                mMapViewFragment.setDroneControlBarVisibility(View.VISIBLE);
                break;
        }
    }

    private void lockWheelScrolling() {
        synchronized (AerialSurveyFragment.class) {
            mWheelScrollingCount++;
        }
    }

    private void releaseWheelScrolling() {
        synchronized (AerialSurveyFragment.class) {
            mWheelScrollingCount--;
        }
    }

    private boolean isWheelingLocked() {
        synchronized (AerialSurveyFragment.class) {
            return mWheelScrollingCount != 0;
        }
    }

    private void initRoutePropertiesDialog(View view) {
        view.findViewById(R.id.route_properties_ok_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.route_properties_back_button).setOnClickListener(onPlanningBtnClickListener);

        mCameraTextView = (TextView) view.findViewById(R.id.route_properties_camera);

        mHatchAngleWheel = (AbstractWheel) view.findViewById(R.id.route_properties_hatch_angle_wheel);
        mHatchAngleWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, HATCH_ANGLE_MIN_VALUE, HATCH_ANGLE_MAX_VALUE, "%01d"));
        mHatchAngleWheel.setCyclic(false);
        mHatchAngleWheel.addScrollingListener(new LockedWheelScrollingListener());

        mAltitudeWheel = (AbstractWheel) view.findViewById(R.id.route_properties_altitude_wheel);
        mAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, ALTITUDE_MIN_VALUE, ALTITUDE_MAX_VALUE, "%01d0"));
        mAltitudeWheel.setCurrentItem(4);
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.addScrollingListener(new LockedWheelScrollingListener());

        mOverlapWheel = (AbstractWheel) view.findViewById(R.id.route_properties_overlap_wheel);
        mOverlapWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, OVERLAP_MIN_VALUE, OVERLAP_MAX_VALUE, "%01d"));
        mOverlapWheel.setCurrentItem(20);
        mOverlapWheel.setCyclic(false);
        mOverlapWheel.addScrollingListener(new LockedWheelScrollingListener());

        mSidelapWheel = (AbstractWheel) view.findViewById(R.id.route_properties_sidelap_wheel);
        mSidelapWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, SIDELAP_MIN_VALUE, SIDELAP_MAX_VALUE, "%01d"));
        mSidelapWheel.setCurrentItem(55);
        mSidelapWheel.setCyclic(false);
        mSidelapWheel.addScrollingListener(new LockedWheelScrollingListener());
    }

    private void updateAngleValue(int angle) throws SurveyBuilderException {
        mRouterBuilder.setAngle(angle);
        updateFootprints();
    }

    private void updateAltitudeValue(int altitude) throws SurveyBuilderException {
        mRouterBuilder.setAltitude((ALTITUDE_MIN_VALUE + altitude) * ALTITUDE_GAP);
        updateFootprints();
    }

    private void updateOverlapValue(int overlap) throws SurveyBuilderException {
        mRouterBuilder.setOverlap(OVERLAP_MIN_VALUE + overlap);
        updateFootprints();
    }

    private void updateSidelapValue(int sidelap) throws SurveyBuilderException {
        mRouterBuilder.setSidelap(SIDELAP_MIN_VALUE + sidelap);
        updateFootprints();
    }

    private void initRoutePropertiesValue() {
        SurveyData surveyData = mAerialSurveyRouter.getSurveyData();
        mCameraTextView.setText(surveyData.getCameraInfo().getName());
        mHatchAngleWheel.setCurrentItem(((int) surveyData.getAngle() + HATCH_ANGLE_MAX_VALUE) % HATCH_ANGLE_MAX_VALUE);
        mAltitudeWheel.setCurrentItem(((int) surveyData.getAltitude() / ALTITUDE_GAP) - ALTITUDE_MIN_VALUE);
        mOverlapWheel.setCurrentItem((int) surveyData.getOverlap() - OVERLAP_MIN_VALUE);
        mSidelapWheel.setCurrentItem((int) surveyData.getSidelap() - SIDELAP_MIN_VALUE);

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
                    case R.id.route_properties_overlap_wheel:
                        updateOverlapValue(wheel.getCurrentItem());
                        break;
                    case R.id.route_properties_altitude_wheel:
                        updateAltitudeValue(wheel.getCurrentItem());
                        break;
                    case R.id.route_properties_hatch_angle_wheel:
                        updateAngleValue(wheel.getCurrentItem());
                        break;
                    case R.id.route_properties_sidelap_wheel:
                        updateSidelapValue(wheel.getCurrentItem());
                        break;
                }
            } catch (SurveyBuilderException e) {
                wheel.setCurrentItem(oldValue);
            }
        }
    }

    private void initRouteInfo(View view) {
        mDistanceAndTimeInfo = view.findViewById(R.id.distance_and_time_info);
        mRouteDetailInfo = view.findViewById(R.id.route_detail_info);

        mDistanceValue = (TextView) view.findViewById(R.id.distance_value_text_view);
        mDurationValue = (TextView) view.findViewById(R.id.time_value_text_view);
        mFootprintValue = (TextView) view.findViewById(R.id.footprint_value_text_view);
        mGSDValue = (TextView) view.findViewById(R.id.gsd_value_text_view);
        mLongitudinalValue = (TextView) view.findViewById(R.id.longitudinal_value_text_view);
        mLateralValue = (TextView) view.findViewById(R.id.lateral_value_text_view);
        mAreaValue = (TextView) view.findViewById(R.id.area_value_text_view);
        mPictureValue = (TextView) view.findViewById(R.id.picture_value_text_view);
        mStripsValue = (TextView) view.findViewById(R.id.strips_value_text_view);
    }

    private void updateRouteInfoValue() {

        SurveyData surveyData = mAerialSurveyRouter.getSurveyData();
        double flightDistance = mAerialSurveyRouter.getLength();

        mDistanceValue.setText(String.format(LENGTH_FORMAT, flightDistance));
        mDurationValue.setText(Utils.getDurationInHMSFormat((flightDistance / DEFAULT_DRONE_SPEED)));

        mFootprintValue.setText(String.format(FOOTPRINT_FORMAT, surveyData.getLongitudinalFootPrint(), surveyData.getLateralFootPrint()));
        mGSDValue.setText(String.format(GSD_FORMAT, surveyData.getGroundResolution()));
        mLongitudinalValue.setText(String.format(LENGTH_FORMAT, mAerialSurveyRouter.getSurveyData().getLongitudinalPictureDistance()));
        mLateralValue.setText(String.format(LENGTH_FORMAT, mAerialSurveyRouter.getSurveyData().getLateralPictureDistance()));

        mAreaValue.setText(String.format(AREA_FORMAT, surveyData.getArea()));
        mPictureValue.setText("" + mAerialSurveyRouter.getCameraShutterCount());
        mStripsValue.setText("" + mAerialSurveyRouter.getNumberOfLines());
    }

    private MissionLoaderListener missionLoaderListener = new MissionLoaderListener() {
        @Override
        public void onLoadCompleted(final List<Mission> missions) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (missions == null || missions.size() == 0) {
                        showToastMessage("There is no mission existed");
                    } else {
                        DroneController droneController = mMapViewFragment.getDroneController();
                        if (droneController != null) {
//                            droneController.startMission();
                        }
                        mLoadMissionProgressDialog.dismiss();
                    }
                }
            });
        }

        @Override
        public void onWriteMissionStatusUpdate(int seq, int total, boolean isComplete) {
            if (isComplete && seq == total - 1) {
                mDroneController.unregisterMissionLoaderListener(missionLoaderListener);
                if (mDroneController != SimpleDroneController.FAKE_DRONE) {
                    List<Mission> droneMissionList = mMissionItemAdapter.getMissions();
                    mDroneController.startMission(droneMissionList.get(0).getLatitude(), droneMissionList.get(0).getLongitude(), droneMissionList.get(0).getAltitude());
                }
                mLoadMissionProgressDialog.dismiss();
                changeLayoutStatus(PLAN_GO_STATUS);
            } else if (isComplete && seq != total - 1) {
                mDroneController.unregisterMissionLoaderListener(missionLoaderListener);
                if (mLoadMissionProgressDialog != null) {
                    mLoadMissionProgressDialog.dismiss();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToastMessage("Write mission fail");
                        }
                    });
                }
            }
        }
    };

    private void setEditOptionButtonState(int state, int selectCount) {
        switch (state) {
            case SELECT_ALL:
                mWaypointListHeaderSelectAllToggleButton.setBackgroundResource(R.drawable.waypoint_list_header_select_all_selected_all_icon);
                mWaypointListHeaderSelectAllToggleButton.setChecked(true);
                mWaypointListHeaderSelectAllToggleButton.setText(String.valueOf(mMissionItemAdapter.getItemCount() - 1));

                mWaypointListHeaderSettingButton.setEnabled(true);
                mWaypointListHeaderDeleteButton.setEnabled(true);
                break;
            case SELECTED_ONE:
                mWaypointListHeaderSelectAllToggleButton.setBackgroundResource(R.drawable.waypoint_list_header_select_all_selected_icon);
                mWaypointListHeaderSelectAllToggleButton.setChecked(false);
                mWaypointListHeaderSelectAllToggleButton.setText(String.valueOf(selectCount));

                mWaypointListHeaderSettingButton.setEnabled(true);
                mWaypointListHeaderDeleteButton.setEnabled(true);
                break;
            case SELECT_NONE:
                mWaypointListHeaderSelectAllToggleButton.setBackgroundResource(R.drawable.waypoint_list_header_select_all_unselect_icon);
                mWaypointListHeaderSelectAllToggleButton.setChecked(false);
                mWaypointListHeaderSelectAllToggleButton.setText("");

                mWaypointListHeaderSettingButton.setEnabled(false);
                mWaypointListHeaderDeleteButton.setEnabled(false);
                break;
        }
    }

    private View.OnClickListener missionListEditFunctionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.waypoint_list_header_edit_text:
                    mMissionItemAdapter.enterMissionListEditMode();

                    setEditOptionButtonState(SELECT_NONE, 0);
                    mWaypointListEditHeaderPanel.setVisibility(View.VISIBLE);
                    mWaypointListHeaderPanel.setVisibility(View.GONE);
                    missionAdapterShowSelect(true);
                    mMapViewFragment.setMapCanAddMarker(false);
                    mMapViewFragment.setEditOptionShow(true);
                    break;

                case R.id.waypoint_list_header_delete_button:
                    mMissionItemAdapter.deleteSelectedItem();
                    setEditOptionButtonState(SELECT_NONE, 0);
                    updateMissionToMap();
                    break;

                case R.id.waypoint_list_header_setting_button:
                    mWaypointSettingDialog = new WaypointListEditSettingDialog(getActivity(), v, mMissionItemAdapter.getSelectedMissionList());
                    mWaypointSettingDialog.setDialogObjectEventListener(new WaypointListEditSettingDialog.OnSettingDialogObjectEventListener() {
                        @Override
                        public void onOkButtonClick(float altitude, int stay, int speed) {
                            mMissionItemAdapter.updateSelectedList(altitude, stay, speed);
                            mMapViewFragment.setEditDoneEnable();
                            setEditOptionButtonState(SELECT_NONE, 0);
                        }
                    });
                    mWaypointSettingDialog.show();
                    break;
            }
        }
    };

    @Override
    public void updateDroneLocation(float droneLat, float droneLon) {
        mDroneLat = droneLat;
        mDroneLon = droneLon;
        if (mMissionItemAdapter.getItemCount() != 0) {
            mMissionItemAdapter.updateMissionItemLocation(0, droneLat, droneLon);
        }
    }

    public void setMissionListEditable(boolean editable) {
        mWaypointListEditButton.setEnabled(editable);
        mMissionItemAdapter.setItemClickable(editable);
        if (mWayPointDetailPanel.getVisibility() == View.VISIBLE) {
            mMissionItemAdapter.onNothingSelected();
            mWayPointDetailPanel.setVisibility(View.GONE);
        }
    }

}
