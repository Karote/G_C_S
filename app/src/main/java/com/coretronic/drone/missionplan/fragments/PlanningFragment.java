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
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;

import java.util.List;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class PlanningFragment extends MavInfoFragment implements MissionLoaderListener {

    private final static String ARG_From_History = "argFromHistory";
    private final static boolean DEFAULT_AUTO_CONTINUE = false;
    private final static int DEFAULT_ALTITUDE = 8;
    private final static int DEFAULT_WAIT_SECONDS = 0;
    private final static int DEFAULT_RADIUS = 0;
    private final static Type DEFAULT_TYPE = Type.WAY_POINT;

    private MissionItemListAdapter mMissionItemAdapter;
    private FrameLayout mWayPointDetailPanel;
    private MissionItemDetailFragment mMissionItemDetailFragment;
    private ProgressDialog mLoadMissionProgressDialog;
    private Button mPlanGoButton = null;
    private Button mDroneLandingButton = null;

    public static PlanningFragment newInstance(boolean isFromHistory) {
        PlanningFragment fragment = new PlanningFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_From_History, isFromHistory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null || !arguments.getBoolean(ARG_From_History)) {
            loadMissionFromDrone();
            return;
        }
        List<Mission> missionList = ((MapViewFragment) getParentFragment()).getMissionList();
        if (missionList == null) {
            return;
        }
        mMissionItemAdapter.update(missionList);
        mMapViewFragment.updateMissions(mMissionItemAdapter.cloneMissionList());
        mMapViewFragment.fitMapShowAllMission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission_plan_planning, container, false);
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
                mMapViewFragment.updateMissions(mMissionItemAdapter.cloneMissionList());
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
    }

    private View.OnClickListener onPlanningBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            DroneController droneController = mMapViewFragment.getDroneController();
            switch (v.getId()) {
                case R.id.plan_go_button:
                    List<Mission> droneMissionList = mMissionItemAdapter.cloneMissionList();
                    if (droneMissionList == null || droneMissionList.size() == 0) {
                        Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (droneController == null) {
                        return;
                    }
                    droneController.writeMissions(droneMissionList, missionLoaderListener);
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
            }
        }
    };

    private DroneController.MissionLoaderListener missionLoaderListener = new DroneController.MissionLoaderListener() {
        @Override
        public void onLoadCompleted(final List<Mission> missions) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (missions == null || missions.size() == 0) {
                        Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
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
    };

    public void missionAdapterSetData(List<Mission> missions) {
        mMissionItemAdapter.update(missions);
    }

    public void missionAdapterClearData() {
        mMissionItemAdapter.clearMission();
        mMissionItemAdapter.notifyDataSetChanged();
    }

    public void missionAdapterShowDelete(boolean isShow) {
        if (isShow) {
            mMissionItemAdapter.setDeleteLayoutVisible(true);
        } else {
            mMissionItemAdapter.setDeleteLayoutVisible(false);
        }
        mWayPointDetailPanel.setVisibility(View.GONE);
    }

    // methods for DetailFragment
    public void setItemMissionType(Mission.Type missionType) {
        mMissionItemAdapter.getSelectedItem().setType(missionType);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    public void setItemMissionAltitude(float missionAltidude) {
        mMissionItemAdapter.getSelectedItem().setAltitude(missionAltidude);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    public void setItemMissionDelay(int missionDelay) {
        mMissionItemAdapter.getSelectedItem().setWaitSeconds(missionDelay);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    public void deleteSelectedMission() {
        mMissionItemAdapter.removeSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
        updateMissionToMap();
    }

    private void updateMissionToMap() {
        mMapViewFragment.updateMissions(mMissionItemAdapter.cloneMissionList());
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
            case R.id.undo_button:
                loadMissionFromDrone();
                break;
            case R.id.delete_all_button:
                missionAdapterClearData();
                missionAdapterShowDelete(false);
                updateMissionToMap();
                break;
            case R.id.delete_done_button:
                missionAdapterShowDelete(false);
                break;
            case R.id.delete_button:
                missionAdapterShowDelete(true);
                break;
        }
    }

    private void loadMissionFromDrone() {
        if (mMapViewFragment.getDroneController() == null) {
            return;
        }
        mMapViewFragment.getDroneController().readMissions(this);
        showLoadProgressDialog("Loading", "Please wait...");
    }

    @Override
    public void onLoadCompleted(final List<Mission> missions) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoadMissionProgressDialog != null) {
                    mLoadMissionProgressDialog.dismiss();
                }
                if (missions == null || missions.size() == 0) {
                    Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
                } else {
                    missionAdapterSetData(missions);
                    mMapViewFragment.updateMissions(missions);
                }
            }
        });
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
        mMissionItemAdapter.getMission(index).setLatitude(lat);
        mMissionItemAdapter.getMission(index).setLongitude(lon);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
        mMissionItemAdapter.add(MapViewFragment.createNewMission(lat, lon, DEFAULT_ALTITUDE, DEFAULT_WAIT_SECONDS, DEFAULT_AUTO_CONTINUE,
                DEFAULT_RADIUS, DEFAULT_TYPE));
        updateMissionToMap();
    }

    @Override
    public void onMapDragStartEvent() {
        mMissionItemAdapter.onNothingSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
    }



}
