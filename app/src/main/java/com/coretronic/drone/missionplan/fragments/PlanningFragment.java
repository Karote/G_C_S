package com.coretronic.drone.missionplan.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneController.MissionLoaderListener;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter.OnItemSelectedListener;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Builder;
import com.coretronic.drone.model.Mission.Type;

import java.util.List;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class PlanningFragment extends MapChildFragment implements MissionLoaderListener, SelectedMissionUpdatedCallback {
    private final static String ARG_From_History = "argFromHistory";
    private final static boolean DEFAULT_AUTO_CONTINUE = true;
    private final static int DEFAULT_ALTITUDE = 8;
    private final static int DEFAULT_WAIT_SECONDS = 0;
    private final static int DEFAULT_RADIUS = 0;
    private final static Type DEFAULT_TYPE = Type.WAY_POINT;

    private MissionItemListAdapter mMissionItemAdapter;
    private FrameLayout mWayPointDetailPanel;
    private MissionItemDetailFragment mMissionItemDetailFragment;
    private ProgressDialog mLoadMissionProgressDialog;
    private Mission.Builder mMissionBuilder;

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
        mMissionBuilder = new Builder();
        mMissionBuilder.setAltitude(DEFAULT_ALTITUDE).setType(DEFAULT_TYPE).setAutoContinue(DEFAULT_AUTO_CONTINUE)
                .setWaitSeconds(DEFAULT_WAIT_SECONDS).setRadius(DEFAULT_RADIUS);
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
        updateMissionToMap();
        mMapViewFragment.fitMapShowAllMission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_planning, container, false);
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
                updateMissionToMap();
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

    }

    private void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private DroneController.MissionLoaderListener missionLoaderListener = new DroneController.MissionLoaderListener() {
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
                    }
                }
            });
        }
    };

    private void missionAdapterShowDelete(boolean isShow) {
        if (isShow) {
            mMissionItemAdapter.setDeleteLayoutVisible(true);
        } else {
            mMissionItemAdapter.setDeleteLayoutVisible(false);
        }
        mWayPointDetailPanel.setVisibility(View.GONE);
    }

    private void updateMissionToMap() {
        mMapViewFragment.updateMissions(mMissionItemAdapter.getMissions());
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
                if (mMissionItemAdapter.undo()) {
                    updateMissionToMap();
                } else {
                    showToastMessage("There is no undo item existed");
                }
                break;
            case R.id.delete_all_button:
                mMissionItemAdapter.clearMission();
                missionAdapterShowDelete(false);
                updateMissionToMap();
                break;
            case R.id.delete_done_button:
                missionAdapterShowDelete(false);
                break;
            case R.id.delete_button:
                missionAdapterShowDelete(true);
                break;
            case R.id.plan_go_button:
                List<Mission> droneMissionList = mMissionItemAdapter.getMissions();
                if (droneMissionList == null || droneMissionList.size() == 0) {
                    showToastMessage("There is no mission existed");
                    return;
                }
                mMapViewFragment.getDroneController().clearMission();
                mMapViewFragment.getDroneController().writeMissions(droneMissionList, missionLoaderListener);
                showLoadProgressDialog("Writing Mission", "Please wait...");
                break;
            case R.id.fit_map_button:
                if (mMissionItemAdapter.getItemCount() > 0) {
                    mMapViewFragment.fitMapShowAllMission();
                }
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
                    showToastMessage("There is no mission existed");
                } else {
                    mMissionItemAdapter.update(missions);
                    updateMissionToMap();
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
        mMissionItemAdapter.updateMissionItemLocation(index, lat, lon);
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
        mMissionItemAdapter.add(mMissionBuilder.setLatitude(lat).setLongitude(lon).create());
        updateMissionToMap();
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
    public void onMissionTypeUpdate(Mission.Type missionType) {
        mMissionItemAdapter.getSelectedItem().setType(missionType);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionAltitudeUpdate(float missionAltidude) {
        mMissionItemAdapter.getSelectedItem().setAltitude(missionAltidude);
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

}
