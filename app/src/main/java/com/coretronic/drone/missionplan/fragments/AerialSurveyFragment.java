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
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelScrollListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.survey.RouterBuilder;
import com.coretronic.drone.survey.RouterBuilder.SurveyBuilderException;
import com.coretronic.drone.survey.SurveyRouter;
import com.coretronic.ibs.log.Logger;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class AerialSurveyFragment extends MavInfoFragment implements SelectedMissionUpdatedCallback {

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
    private MissionItemListAdapter mMissionItemAdapter;
    private FrameLayout mWayPointDetailPanel;
    private MissionItemDetailFragment mMissionItemDetailFragment;
    private ProgressDialog mLoadMissionProgressDialog;
    private View mDroneControlButtonGroup = null;
    private View mRoutePropertiesDialog = null;
    private View mMavInfoPanel = null;
    private Button mPlanGoButton = null;
    private Button mDroneLandingButton = null;
    private Button mCreateRouteButton = null;
    private RouterBuilder mRouterBuilder;
    private List<Coord2D> mPolygonPoints;
    private SurveyRouter mAerialSurveyRouter;

    private AbstractWheel mHatchAngleWheel, mAltitudeWheel, mOverlapWheel, mSidelapWheel;
    private int mHatchAngleValue, mAltitudeValue, mOverlapValue, mSidelapValue;
    private boolean mAngleWheelScrolled = false;
    private boolean mAltitudeWheelScrolled = false;
    private boolean mOverlapWheelScrolled = false;
    private boolean mSidelapWheelScrolled = false;

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
        mMavInfoPanel = view.findViewById(R.id.mav_info_panel);

        changeLayoutStatus(INIT_STATUS);
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
                    initRoutePropertiesValue();
                    changeLayoutStatus(FOOTPRINT_STATUS);
                    break;
                case R.id.route_properties_ok_button:
                    if (mAerialSurveyRouter == null) {
                        showToastMessage("No Polygon Existed");
                        return;
                    }
                    onAerialSurveyMissionCreated();
                    changeLayoutStatus(ROUTE_CREATED_STATUS);
                    break;
                case R.id.route_properties_back_button:
                    mMapViewFragment.clearFootprint();
                    changeLayoutStatus(SCOPE_STATUS);
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
                changeLayoutStatus(INIT_STATUS);
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
        if (mMissionItemAdapter.getItemCount() > 0) {
            mMissionItemAdapter.updateMissionItemLocation(index, lat, lon);
        } else {
            mPolygonPoints.set(index, new Coord2D(lat, lon));
        }
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
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

    private void changeLayoutStatus(int status) {
        switch (status) {
            case INIT_STATUS:
                mDroneControlButtonGroup.setVisibility(View.GONE);
                mCreateRouteButton.setVisibility(View.VISIBLE);
                mCreateRouteButton.setEnabled(false);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                mMavInfoPanel.setVisibility(View.GONE);
                break;
            case SCOPE_STATUS:
                mCreateRouteButton.setVisibility(View.VISIBLE);
                mCreateRouteButton.setEnabled(true);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                break;
            case FOOTPRINT_STATUS:
                mDroneControlButtonGroup.setVisibility(View.GONE);
                mCreateRouteButton.setVisibility(View.GONE);
                mRoutePropertiesDialog.setVisibility(View.VISIBLE);
                mMavInfoPanel.setVisibility(View.GONE);
                break;
            case ROUTE_CREATED_STATUS:
                mDroneControlButtonGroup.setVisibility(View.VISIBLE);
                mRoutePropertiesDialog.setVisibility(View.GONE);
                mMavInfoPanel.setVisibility(View.VISIBLE);
                break;
            case PLAN_GO_STATUS:
                break;
        }
    }

    private void initRoutePropertiesDialog(View view) {
        view.findViewById(R.id.route_properties_ok_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.route_properties_back_button).setOnClickListener(onPlanningBtnClickListener);

        mHatchAngleWheel = (AbstractWheel) view.findViewById(R.id.route_properties_hatch_angle_wheel);
        mHatchAngleWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, HATCH_ANGLE_MIN_VALUE, HATCH_ANGLE_MAX_VALUE, "%01d"));
        mHatchAngleWheel.setCyclic(false);
        mHatchAngleWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {
                mAngleWheelScrolled = true;
            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mAngleWheelScrolled = false;
                updateAngleValue();

            }
        });
        mHatchAngleWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                if (!mAngleWheelScrolled) {
                    updateAngleValue();
                }
            }
        });

        mAltitudeWheel = (AbstractWheel) view.findViewById(R.id.route_properties_altitude_wheel);
        mAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, ALTITUDE_MIN_VALUE, ALTITUDE_MAX_VALUE, "%01d0"));
        mAltitudeWheel.setCurrentItem(4);
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {
                mAltitudeWheelScrolled = true;
            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mAltitudeWheelScrolled = false;
                updateAltitudeValue();

            }
        });
        mAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                if (!mAltitudeWheelScrolled) {
                    updateAltitudeValue();
                }
            }
        });

        mOverlapWheel = (AbstractWheel) view.findViewById(R.id.route_properties_overlap_wheel);
        mOverlapWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, OVERLAP_MIN_VALUE, OVERLAP_MAX_VALUE, "%01d"));
        mOverlapWheel.setCurrentItem(20);
        mOverlapWheel.setCyclic(false);
        mOverlapWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {
                mOverlapWheelScrolled = true;
            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mOverlapWheelScrolled = false;
                updateOverlapValue();

            }
        });
        mOverlapWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                if (!mOverlapWheelScrolled) {
                    updateOverlapValue();
                }
            }
        });

        mSidelapWheel = (AbstractWheel) view.findViewById(R.id.route_properties_sidelap_wheel);
        mSidelapWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.spinner_wheel_text_layout, SIDELAP_MIN_VALUE, SIDELAP_MAX_VALUE, "%01d"));
        mSidelapWheel.setCurrentItem(55);
        mSidelapWheel.setCyclic(false);
        mSidelapWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {
                mSidelapWheelScrolled = true;
            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mSidelapWheelScrolled = false;
                updateSidelapValue();

            }
        });
        mSidelapWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                if (!mSidelapWheelScrolled) {
                    updateSidelapValue();
                }
            }
        });
    }

    private void updateAngleValue() {
        mHatchAngleValue = mHatchAngleWheel.getCurrentItem();
        Logger.d("Angle newValue:" + mHatchAngleValue);
        mRouterBuilder.setAngle(mHatchAngleValue);
        updateFootprints();
    }

    private void updateAltitudeValue() {
        mAltitudeValue = (ALTITUDE_MIN_VALUE + mAltitudeWheel.getCurrentItem()) * ALTITUDE_GAP;
        Logger.d("Altitude newValue:" + mAltitudeValue);
        mRouterBuilder.setAltitude(mAltitudeValue);
        updateFootprints();
    }

    private void updateOverlapValue() {
        mOverlapValue = OVERLAP_MIN_VALUE + mOverlapWheel.getCurrentItem();
        Logger.d("Overlap newValue:" + mOverlapValue);
        mRouterBuilder.setOverlap(mOverlapValue);
        updateFootprints();
    }

    private void updateSidelapValue() {
        mSidelapValue = SIDELAP_MIN_VALUE + mSidelapWheel.getCurrentItem();
        Logger.d("Sidelap newValue:" + mSidelapValue);
        mRouterBuilder.setSidelap(mSidelapValue);
        updateFootprints();
    }

    private void initRoutePropertiesValue() {
        mHatchAngleValue = ((int) mAerialSurveyRouter.getSurveyData().getAngle() + HATCH_ANGLE_MAX_VALUE) % HATCH_ANGLE_MAX_VALUE;
        mAltitudeValue = (int) mAerialSurveyRouter.getSurveyData().getAltitude();
        mOverlapValue = (int) mAerialSurveyRouter.getSurveyData().getOverlap();
        mSidelapValue = (int) mAerialSurveyRouter.getSurveyData().getSidelap();
        Logger.d("getAngle:" + mHatchAngleValue);
        Logger.d("getAltitude:" + mAltitudeValue);
        Logger.d("getOverlap:" + mOverlapValue);
        Logger.d("getSidelap:" + mSidelapValue);
        mHatchAngleWheel.setCurrentItem(mHatchAngleValue);
        mAltitudeWheel.setCurrentItem((mAltitudeValue / ALTITUDE_GAP) - ALTITUDE_MIN_VALUE);
        mOverlapWheel.setCurrentItem(mOverlapValue - OVERLAP_MIN_VALUE);
        mSidelapWheel.setCurrentItem(mSidelapValue - SIDELAP_MIN_VALUE);
    }
}
