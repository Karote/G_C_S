package com.coretronic.drone.missionplan.fragments;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.coretronic.drone.model.Mission.Type;

import java.util.List;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class PlanningFragment extends MavInfoFragment implements MissionLoaderListener {
    private final static String ARG_From_History = "argFromHistory";

    private final static int DEFAULT_ALTITUDE = 8;
    private final static int DEFAULT_WAIT_SECONDS = 0;
    private final static boolean DEFAULT_AUTO_CONTINUE = false;
    private final static int DEFAULT_RADIUS = 0;
    private final static Type DEFAULT_TYPE = Type.WAY_POINT;

    private MissionItemListAdapter mMissionItemAdapter = null;
    private FrameLayout layout_waypointDetail = null;

    private WaypointDetailFragment detailFragment = null;
    private ProgressDialog progressDialog = null;

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

        layout_waypointDetail = (FrameLayout) view.findViewById(R.id.waypoint_detail_container);
        layout_waypointDetail.setVisibility(View.GONE);

        mMissionItemAdapter.setOnItemClickListener(new OnItemSelectedListener() {
            @Override
            public void onItemDeleted(int position) {
                mMapViewFragment.updateMissions(mMissionItemAdapter.cloneMissionList());
            }

            @Override
            public void onItemSelected(Mission mission, int currentIndex) {
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                detailFragment = WaypointDetailFragment.newInstance(currentIndex + 1, mission);
                fragmentTransaction.replace(R.id.waypoint_detail_container, detailFragment, "DetailFragment").commit();
                layout_waypointDetail.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected() {
                layout_waypointDetail.setVisibility(View.GONE);
            }
        });

        // Go & Stop & Location Button Panel
        view.findViewById(R.id.plan_go_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.drone_landing_button).setOnClickListener(onPlanningBtnClickListener);
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
                    showProgressDialog("Loading", "Please wait...");
                    break;
                case R.id.drone_landing_button:
                    if (droneController != null) {
                        droneController.land();
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
                        progressDialog.dismiss();
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
        layout_waypointDetail.setVisibility(View.GONE);
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

    public void deleteItemMission() {
        mMissionItemAdapter.removeSelected();
        layout_waypointDetail.setVisibility(View.GONE);
        updateMissionToMap();
    }

    private void updateMissionToMap() {
        mMapViewFragment.updateMissions(mMissionItemAdapter.cloneMissionList());
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
        showProgressDialog("Loading", "Please wait...");
    }

    @Override
    public void onLoadCompleted(final List<Mission> missions) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
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

    private void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity()) {
                @Override
                public void onBackPressed() {
                    super.onBackPressed();
                    dismiss();
                }
            };
            progressDialog.setCancelable(false);
        } else {
            progressDialog.dismiss();
        }
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    @Override
    public void onDragEnd(int index, float lat, float lon) {
        mMissionItemAdapter.getMission(index).setLatitude(lat);
        mMissionItemAdapter.getMission(index).setLongitude(lon);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(float lat, float lon) {
        mMissionItemAdapter.add(MapViewFragment.createNewMission(lat, lon, DEFAULT_ALTITUDE, DEFAULT_WAIT_SECONDS, DEFAULT_AUTO_CONTINUE,
                DEFAULT_RADIUS, DEFAULT_TYPE));
        updateMissionToMap();
    }

    @Override
    public void onDragStart() {
        mMissionItemAdapter.onNothingSelected();
        layout_waypointDetail.setVisibility(View.GONE);
    }

    private class FixedLinearLayoutManager extends LinearLayoutManager {

        public FixedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        private int[] mMeasuredDimension = new int[2];

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                              int widthSpec, int heightSpec) {
            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);
            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {
                measureScrapChild(recycler, i,
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        mMeasuredDimension);

                if (getOrientation() == HORIZONTAL) {
                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }
            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                    width = widthSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }
            setMeasuredDimension(width, Math.min(height, heightSize));
        }

        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                       int heightSpec, int[] measuredDimension) {
            View view = recycler.getViewForPosition(position);
            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);
                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);

                view.measure(childWidthSpec, childHeightSpec);
                int width = view.getMeasuredWidth();
                if (view instanceof ViewGroup) {
                    int childTotalWidth = 0;
                    for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                        View childView = ((ViewGroup) view).getChildAt(i);
                        if (childView.getVisibility() == View.VISIBLE) {
                            childTotalWidth += childView.getMeasuredWidth();
                        }
                    }
                    width = childTotalWidth;
                }

                measuredDimension[0] = width + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        }
    }

}
