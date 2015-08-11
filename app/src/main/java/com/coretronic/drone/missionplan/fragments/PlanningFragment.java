package com.coretronic.drone.missionplan.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.Mission;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter;
import com.coretronic.drone.utility.AppConfig;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class PlanningFragment extends MavInfoFragment {
    private static final String TAG = PlanningFragment.class.getSimpleName();

    private RecyclerView recyclerView = null;
    private static MissionItemListAdapter mMissionItemAdapter = null;
    private static FrameLayout layout_waypointDetail = null;

    private TextView tv_droneAltitude = null;
    private TextView tv_droneSpeed = null;
    private TextView tv_droneLat = null;
    private TextView tv_droneLng = null;


    private FragmentActivity fragmentActivity = null;
    private FragmentManager fragmentChildManager = null;
    private WaypointDetailFragment detailFragment = null;
    private DroneController drone = null;
    private ProgressDialog progressDialog = null;

    private static MissionAdapterListener mCallback = null;

    public interface MissionAdapterListener {
        void writeMissionsToMap(List<Mission> missions);

        void setMapToMyLocation();

        void setMapToDrone();

        void fitMapShowAllMission();
    }

    // Drone info
    private SharedPreferences sharedPreferences;
    private Gson gson;
//    private FileHelper fileHelper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (MissionAdapterListener) fragmentActivity.getSupportFragmentManager().findFragmentByTag("fragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = getActivity();
        sharedPreferences = getActivity().getSharedPreferences(AppConfig.SHAREDPREFERENCE_ID, 0);
        gson = new Gson();
//        fileHelper = new FileHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission_plan_planning, container, false);
        progressDialog = new ProgressDialog(fragmentActivity) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                dismiss();
            }
        };
        progressDialog.setCancelable(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentChildManager = getChildFragmentManager();

        // Mission List
        recyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.recyclerview_item_width);
        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(recyclerLayoutMgr);

        mMissionItemAdapter = new MissionItemListAdapter();
        recyclerView.setAdapter(mMissionItemAdapter);

        layout_waypointDetail = (FrameLayout) view.findViewById(R.id.waypoint_detail_container);
        layout_waypointDetail.setVisibility(View.GONE);

        mMissionItemAdapter.SetOnItemClickListener(new MissionItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemDeleteClick(View v, int position) {
                mMissionItemAdapter.remove(position);
                mMissionItemAdapter.notifyDataSetChanged();
                mCallback.writeMissionsToMap(mMissionItemAdapter.cloneMissionList());
            }

            @Override
            public void onItemPlanClick(View view, int position) {
                if (mMissionItemAdapter.getFocusIndex() < 0 || position != mMissionItemAdapter.getFocusIndex()) {
                    Mission itemMission = mMissionItemAdapter.getMission(position);
                    FragmentTransaction fragmentTransaction = fragmentChildManager.beginTransaction();
                    detailFragment = WaypointDetailFragment.newInstance(position + 1, itemMission);
                    fragmentTransaction
                            .replace(R.id.waypoint_detail_container, detailFragment, "DetailFragment")
                            .commit();
                    layout_waypointDetail.setVisibility(View.VISIBLE);
                } else {
                    layout_waypointDetail.setVisibility(View.GONE);
                }
            }
        });

        // Go & Stop & Location Button Panel
        final Button goButton = (Button) view.findViewById(R.id.btn_plan_go);
        goButton.setOnClickListener(onPlanningBtnClickListener);

        final Button landButton = (Button) view.findViewById(R.id.btn_plan_land);
        landButton.setOnClickListener(onPlanningBtnClickListener);

        final Button rtlButton = (Button) view.findViewById(R.id.btn_plan_rtl);
        rtlButton.setOnClickListener(onPlanningBtnClickListener);

        final Button stopButton = (Button) view.findViewById(R.id.btn_plan_stop);
        stopButton.setOnClickListener(onPlanningBtnClickListener);

        final Button myLocationButton = (Button) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(onPlanningBtnClickListener);

        final Button droneLocationButton = (Button) view.findViewById(R.id.button_drone_location);
        droneLocationButton.setOnClickListener(onPlanningBtnClickListener);

        final Button fitMapButton = (Button) view.findViewById(R.id.button_fit_map);
        fitMapButton.setOnClickListener(onPlanningBtnClickListener);

        // MAV Info
        tv_droneAltitude = (TextView) view.findViewById(R.id.altitude_text);
        tv_droneAltitude.setText("0m");
        tv_droneSpeed = (TextView) view.findViewById(R.id.speed_text);
        tv_droneSpeed.setText("0 km/h");
        tv_droneLat = (TextView) view.findViewById(R.id.location_lat_text);
        tv_droneLat.setText("0.000000,");
        tv_droneLng = (TextView) view.findViewById(R.id.location_lng_text);
        tv_droneLng.setText("0.000000");

    }

    View.OnClickListener onPlanningBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            drone = ((MainActivity) getActivity()).getDroneController();
//            if (drone == null) {
//                return;
//            }
            switch (v.getId()) {
                case R.id.btn_plan_go:
                    List<Mission> droneMissionList = mMissionItemAdapter.cloneMissionList();
                    if(droneMissionList == null || droneMissionList.size() == 0){
                        Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
                        return;
                    }
                    // mission list
                    sharedPreferences.edit()
                            .putString(AppConfig.PREF_MISSION_LIST, gson.toJson(droneMissionList))
                            .apply();

                    if (drone != null) {
                        drone.writeMissions(droneMissionList, missionLoaderListener);
                    }
                    progressDialog.setTitle("Sending");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();

                    break;
                case R.id.btn_plan_land:
                    Log.d("morris", "btn_plan_land");
                    if (drone != null) {
                        drone.land();
                    }
                    break;
                case R.id.btn_plan_rtl:
                    Log.d("morris", "btn_plan_rtl");
                    if (drone != null) {
                        drone.returnToLaunch();
                    }
                    break;
                case R.id.btn_plan_stop:
                    Log.d("morris", "btn_plan_stop");
                    if (drone != null) {
                        Log.d("morris", "pauseMission");
                        drone.pauseMission();
                    }
                    break;
                case R.id.button_my_location:
                    mCallback.setMapToMyLocation();
                    break;
                case R.id.button_drone_location:
                    mCallback.setMapToDrone();
                    break;
                case R.id.button_fit_map:
                    if (mMissionItemAdapter.getItemCount() > 0) {
                        mCallback.fitMapShowAllMission();
                    }
                    break;
            }
        }
    };

    DroneController.MissionLoaderListener missionLoaderListener = new DroneController.MissionLoaderListener() {
        @Override
        public void onLoadCompleted(final List<Mission> missions) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (missions == null || missions.size() == 0) {
                        Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
                    } else {
                        if (drone != null)
                            drone.startMission();

                        progressDialog.dismiss();
                    }
                }
            });
        }
    };

    private Mission createNewMission(float latitude, float longitude, float altitude,
                                     int waitSeconds, boolean autoContinue, int radius, Mission.Type type) {
        Mission.Builder builder = new Mission.Builder();

        builder.setLatitude(latitude);
        builder.setLongitude(longitude);
        builder.setAltitude(altitude);
        builder.setWaitSeconds(waitSeconds);
        builder.setAutoContinue(autoContinue);
        builder.setRadius(radius);
        builder.setType(type);

        return builder.create();
    }

    // Public Method
    @Override
    public void setMavInfoAltitude(float altitude) {
        if (tv_droneAltitude == null)
            return;

        String tx_alt = String.format("%d", (int) altitude);
        tv_droneAltitude.setText(tx_alt + "m");
    }

    @Override
    public void setMavInfoSpeed(float groundSpeed) {
        if (tv_droneSpeed == null)
            return;

        tv_droneSpeed.setText(groundSpeed + " km/h");
    }

    @Override
    public void setMavInfoLocation(long droneLat, long droneLng) {
        if (tv_droneLat == null)
            return;
        String latLongStr = String.valueOf(droneLat);
        String lngLongStr = String.valueOf(droneLng);
        int latDecimalPos = latLongStr.length() - 7;
        int lngDecimalPos = lngLongStr.length() - 7;

        String lat_output, lng_output;
        if (latDecimalPos <= 0) {
            lat_output = String.format("0.%07d", droneLat);
        } else {
            lat_output = latLongStr.substring(0, latDecimalPos) + "." + latLongStr.substring(latDecimalPos);
        }

        if (lngDecimalPos <= 0) {
            lng_output = String.format("0.%07d", droneLng);
        } else {
            lng_output = lngLongStr.substring(0, lngDecimalPos) + "." + lngLongStr.substring(lngDecimalPos);
        }

        tv_droneLat.setText(lat_output + ",");
        tv_droneLng.setText(lng_output);
    }

    public List<Mission> missionAdapterGetList() {
        return mMissionItemAdapter.cloneMissionList();
    }

    public void missionAdapterSetData(List<Mission> missions) {
        mMissionItemAdapter.update(missions);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    public void missionAdapterAddData(float latitude, float longitude, float altitude,
                                      int waitSeconds, boolean autoContinue, int radius, Mission.Type type) {
        mMissionItemAdapter.add(createNewMission(latitude, longitude, altitude, waitSeconds, autoContinue, radius, type));
        mMissionItemAdapter.notifyDataSetChanged();
    }

    public void missionAdapterClearData() {
        mMissionItemAdapter.clearMission();
        mMissionItemAdapter.notifyDataSetChanged();
    }

    public void missionAdapterShowDelete(boolean isShow) {
        if (isShow) {
            recyclerView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.recyclerview_deleteitem_width);
            mMissionItemAdapter.setDeleteLayoutVisible(true);
        } else {
            recyclerView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.recyclerview_item_width);
            mMissionItemAdapter.setDeleteLayoutVisible(false);
        }
        layout_waypointDetail.setVisibility(View.GONE);
        mMissionItemAdapter.notifyDataSetChanged();
    }


    // static methods for DetailFragment
    public static void setItemMissionType(Mission.Type missionType) {
        mMissionItemAdapter.getMission(mMissionItemAdapter.getFocusIndex()).setType(missionType);
    }

    public static void setItemMissionAltitude(float missionAltidude) {
        mMissionItemAdapter.getMission(mMissionItemAdapter.getFocusIndex()).setAltitude(missionAltidude);
    }

    public static void setItemMissionDelay(int missionDelay) {
        mMissionItemAdapter.getMission(mMissionItemAdapter.getFocusIndex()).setWaitSeconds(missionDelay);
    }

    public static void deleteItemMission() {
        mMissionItemAdapter.remove(mMissionItemAdapter.getFocusIndex());
        mMissionItemAdapter.clearFocusIndex();
        mMissionItemAdapter.notifyDataSetChanged();
        mCallback.writeMissionsToMap(mMissionItemAdapter.cloneMissionList());
        layout_waypointDetail.setVisibility(View.GONE);
    }


}
