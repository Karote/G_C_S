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
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneController.MissionLoaderListener;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter.OnItemSelectedListener;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.survey.RouterBuilder;
import com.coretronic.drone.survey.RouterBuilder.SurveyBuilderException;
import com.coretronic.drone.survey.SurveyRouter;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class AerialSurveyFragment extends MavInfoFragment implements SelectedMissionUpdatedCallback {

    private MissionItemListAdapter mMissionItemAdapter;
    private FrameLayout mWayPointDetailPanel;
    private MissionItemDetailFragment mMissionItemDetailFragment;
    private ProgressDialog mLoadMissionProgressDialog;
    private View mDroneControlButtonGroup = null;
    private View mRoutePropertiesDialog = null;
    private Button mPlanGoButton = null;
    private Button mDroneLandingButton = null;
    private Button mCreateRouteButton = null;
    private RouterBuilder mRouterBuilder;
    private List<Coord2D> mPolygonPoints;
    private SurveyRouter mAerialSurveyRouter;

    private AbstractWheel mHatchAngleWheel, mAltitudeWheel, mOverlapWheel, mSidelapWheel;
    private int mHatchAngle, mAltitude, mOverlap, mSidelap;

    public static AerialSurveyFragment newInstance() {
        AerialSurveyFragment fragment = new AerialSurveyFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRouterBuilder = new RouterBuilder().setAutoChoose(true);
        mPolygonPoints = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission_plan_aerial_survey, container, false);
        initMavInfoView(view, R.id.altitude_text, R.id.speed_text, R.id.location_lat_text, R.id.location_lng_text, R.id.flight_time_text);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mission List
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new FixedLinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mMissionItemAdapter = new MissionItemListAdapter();
        recyclerView.setAdapter(mMissionItemAdapter);

        mWayPointDetailPanel = (FrameLayout) view.findViewById(R.id.way_point_detail_container);
        mWayPointDetailPanel.setVisibility(View.GONE);

        mMissionItemAdapter.setOnItemClickListener(new OnItemSelectedListener() {
            @Override
            public void onItemDeleted(int position) {
                updatePolygon();
            }

            @Override
            public void onItemSelected(Mission mission, int currentIndex) {
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                mMissionItemDetailFragment = MissionItemDetailFragment.newInstance(currentIndex + 1, mission);
                fragmentTransaction.replace(R.id.way_point_detail_container, mMissionItemDetailFragment, "DetailFragment").commit();
                mWayPointDetailPanel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected() {
                mWayPointDetailPanel.setVisibility(View.GONE);
            }
        });

        // Go & Stop & Location Button Panel
        mDroneControlButtonGroup = view.findViewById(R.id.drone_control_button_group);
        mPlanGoButton = (Button) view.findViewById(R.id.plan_go_button);
        mPlanGoButton.setOnClickListener(onPlanningBtnClickListener);
        mPlanGoButton.setVisibility(View.VISIBLE);
        mDroneLandingButton = (Button) view.findViewById(R.id.drone_landing_button);
        mDroneLandingButton.setOnClickListener(onPlanningBtnClickListener);
        mDroneLandingButton.setVisibility(View.GONE);
        view.findViewById(R.id.drone_rtl_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.plan_stop_button).setOnClickListener(onPlanningBtnClickListener);

        view.findViewById(R.id.my_location_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.drone_location_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.fit_map_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.map_type_button).setOnClickListener(onPlanningBtnClickListener);
        mCreateRouteButton = (Button) view.findViewById(R.id.create_route_button);
        mCreateRouteButton.setOnClickListener(onPlanningBtnClickListener);
        mRoutePropertiesDialog = view.findViewById(R.id.route_properties_dialog);
        initRoutePropertiesDialog(view);

        layoutStatusMachine(1);
    }

    private View.OnClickListener onPlanningBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            DroneController droneController = mMapViewFragment.getDroneController();
            switch (v.getId()) {
                case R.id.plan_go_button:
                    List<Mission> droneMissionList = mMissionItemAdapter.getMissions();
                    if (droneMissionList == null || droneMissionList.size() == 0) {
                        showToastMessage("There is no mission existed");
                        return;
                    }
                    if (droneController == null) {
                        return;
                    }
                    droneController.writeMissions(droneMissionList, new MissionLoaderListener() {
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
                                            droneController.startMission();
                                        }
                                        mLoadMissionProgressDialog.dismiss();
                                        mPlanGoButton.setVisibility(View.GONE);
                                        mDroneLandingButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    });
                    showLoadProgressDialog("Loading", "Please wait...");
                    break;
                case R.id.drone_landing_button:
                    if (droneController != null) {
                        droneController.land();
                        mDroneLandingButton.setVisibility(View.GONE);
                        mPlanGoButton.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.drone_rtl_button:
                    if (droneController != null) {
                        droneController.returnToLaunch();
                    }
                    break;
                case R.id.plan_stop_button:
                    if (droneController != null) {
                        droneController.pauseMission();
                    }
                    break;
                case R.id.my_location_button:
                    mMapViewFragment.setMapToMyLocation();
                    break;
                case R.id.drone_location_button:
                    mMapViewFragment.setMapToDrone();
                    break;
                case R.id.fit_map_button:
                    if (mMissionItemAdapter.getItemCount() > 0) {
                        mMapViewFragment.fitMapShowAllMission();
                    }
                    break;
                case R.id.map_type_button:
                    mMapViewFragment.changeMapType();
                    break;
                case R.id.create_route_button:
                    updateFootprints();
                    layoutStatusMachine(3);
                    break;
                case R.id.route_properties_ok_button:
                    if (mAerialSurveyRouter == null) {
                        showToastMessage("No Polygon Existed");
                        return;
                    }
                    onAerialSurveyMissionCreated();
                    layoutStatusMachine(4);
                    break;
                case R.id.route_properties_back_button:
                    mMapViewFragment.clearFootprint();
                    layoutStatusMachine(2);
                    break;
            }
        }
    };

    private void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void missionAdapterShowDelete(boolean isShow) {
        if (isShow) {
            mMissionItemAdapter.setDeleteLayoutVisible(true);
        } else {
            mMissionItemAdapter.setDeleteLayoutVisible(false);
        }
        mWayPointDetailPanel.setVisibility(View.GONE);
    }

    private void updatePolygon() {
        mMapViewFragment.updatePolygon(mPolygonPoints);
//        updateFootprints();
    }

    private void updateFootprints() {

        try {
            mAerialSurveyRouter = mRouterBuilder.setPoints(mPolygonPoints).build();
            mMapViewFragment.updateFootprints(mAerialSurveyRouter);
            return;
        } catch (SurveyBuilderException e) {
            showToastMessage(e.getMessage());
        }
//        mMapViewFragment.clearSurvey();
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
            case R.id.delete_all_button:
                mMissionItemAdapter.clearMission();
                missionAdapterShowDelete(false);
                updateMissionToMap();
                mMapViewFragment.clearSurvey();
                mPolygonPoints.clear();
                mAerialSurveyRouter = null;
                layoutStatusMachine(1);
                break;
            case R.id.undo_button:
                if (mMissionItemAdapter.undo()) {
                    updateMissionToMap();
                } else {
                    showToastMessage("There is no undo item existed");
                }
                break;
            case R.id.delete_done_button:
                missionAdapterShowDelete(false);
                break;
            case R.id.delete_button:
                missionAdapterShowDelete(true);
                break;
        }
    }

    private void onAerialSurveyMissionCreated() {
        List<Mission> surveyMissions = mAerialSurveyRouter.toMissions();
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

        mMissionItemAdapter.updateMissionItemLocation(index, lat, lon);
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
        mPolygonPoints.add(new Coord2D(lat, lon));
        if (mPolygonPoints.size() > 2) {
            layoutStatusMachine(2);
        }
        updatePolygon();
    }

    @Override
    public void onMapDragStartEvent() {
        mMissionItemAdapter.onNothingSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
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
    }

    @Override
    public void onMissionAltitudeUpdate(float missionAltitude) {
        mMissionItemAdapter.getSelectedItem().setAltitude(missionAltitude);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionDelayUpdate(int seconds) {
        mMissionItemAdapter.getSelectedItem().setWaitSeconds(seconds);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionDeleted() {
        mMissionItemAdapter.removeSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
        updateMissionToMap();
    }

    private void updateMissionToMap() {
        mMapViewFragment.updateMissions(mMissionItemAdapter.getMissions());
    }

    private void layoutStatusMachine(int status) {
        switch (status) {
            case 1:
                mDroneControlButtonGroup.setVisibility(View.GONE);
                mCreateRouteButton.setVisibility(View.VISIBLE);
                mCreateRouteButton.setEnabled(false);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                break;
            case 2:
                mCreateRouteButton.setVisibility(View.VISIBLE);
                mCreateRouteButton.setEnabled(true);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                break;
            case 3:
                mDroneControlButtonGroup.setVisibility(View.GONE);
                mCreateRouteButton.setVisibility(View.GONE);
                mRoutePropertiesDialog.setVisibility(View.VISIBLE);
                break;
            case 4:
                mDroneControlButtonGroup.setVisibility(View.VISIBLE);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                break;
            case 5:
                break;
        }
    }

    private void initRoutePropertiesDialog(View view) {
        view.findViewById(R.id.route_properties_ok_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.route_properties_back_button).setOnClickListener(onPlanningBtnClickListener);

        mHatchAngleWheel = (AbstractWheel) view.findViewById(R.id.route_properties_hatch_angle_wheel);
        mHatchAngleWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, 0, 180, "%01d"));
        mHatchAngleWheel.setCyclic(false);
        mHatchAngleWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mHatchAngle = newValue;
            }
        });

        mAltitudeWheel = (AbstractWheel) view.findViewById(R.id.route_properties_altitude_wheel);
        mAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, 1, 20, "%01d0"));
        mAltitudeWheel.setCurrentItem(4);
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mAltitude = newValue;
            }
        });

        mOverlapWheel = (AbstractWheel) view.findViewById(R.id.route_properties_overlap_wheel);
        mOverlapWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, 50, 90, "%01d"));
        mOverlapWheel.setCurrentItem(20);
        mOverlapWheel.setCyclic(false);
        mOverlapWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mOverlap = newValue;
            }
        });

        mSidelapWheel = (AbstractWheel) view.findViewById(R.id.route_properties_sidelap_wheel);
        mSidelapWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, 15, 90, "%01d"));
        mSidelapWheel.setCurrentItem(55);
        mSidelapWheel.setCyclic(false);
        mSidelapWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mSidelap = newValue;
            }
        });
    }
}
