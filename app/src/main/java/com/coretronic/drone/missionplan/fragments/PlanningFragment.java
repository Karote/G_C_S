package com.coretronic.drone.missionplan.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneController.MissionLoaderListener;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.LoadPlanningListAdapter;
import com.coretronic.drone.missionplan.adapter.MissionListUndoableAdapter;
import com.coretronic.drone.missionplan.adapter.MissionListUndoableAdapter.OnListStateChangedListener;
import com.coretronic.drone.missionplan.model.LoadPlanningDataAccessObject;
import com.coretronic.drone.missionplan.model.PlanningData;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Builder;
import com.coretronic.drone.model.Mission.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
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
    private final static int WEBVIEW_SCREENSHOT_WIDTH = 432;
    private final static int WEBVIEW_SCREENSHOT_HEIGHT = 318;

    private MissionListUndoableAdapter mMissionItemAdapter;
    private FrameLayout mWayPointDetailPanel;
    private MissionItemDetailFragment mMissionItemDetailFragment;
    private ProgressDialog mLoadMissionProgressDialog;
    private Mission.Builder mMissionBuilder;
    private Dialog mMoreFunctionPopupDialog;
    private Button mSaveMissionButton;
    private Button mClearMissionButton;
    private boolean mSaveAndClearMissionFlag = false;
    private Dialog mLoadPlanningPopDialog;
    private LoadPlanningDataAccessObject mLoadPlanningDAO;
    private LoadPlanningListAdapter mLoadPlanningListAdapter;

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
        mMapViewFragment.fitMapShowAll();
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
        mMissionItemAdapter = new MissionListUndoableAdapter();
        recyclerView.setAdapter(mMissionItemAdapter);

        mWayPointDetailPanel = (FrameLayout) view.findViewById(R.id.way_point_detail_container);
        mWayPointDetailPanel.setVisibility(View.GONE);

        mMissionItemAdapter.setOnAdapterListChangedListener(new OnListStateChangedListener() {
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

            @Override
            public void onUndoOptionEnable(boolean enable) {
                mMapViewFragment.setUndoButtonEnable(enable);
            }

            @Override
            public void onSaveAndClearMissionEnable(boolean enable) {
                mSaveAndClearMissionFlag = enable;
            }
        });

        mLoadPlanningDAO = new LoadPlanningDataAccessObject(getActivity().getApplicationContext());
        mLoadPlanningListAdapter = new LoadPlanningListAdapter(getActivity().getApplicationContext(), mLoadPlanningDAO.getAll());
        mLoadPlanningListAdapter.setOnGridItemClickListener(new LoadPlanningListAdapter.OnGridItemClickListener() {
            @Override
            public void onItemSelected(PlanningData planningData) {
                Gson gson = new Gson();
                List<Mission> selectedList = gson.fromJson(planningData.getPlanningContent(), new TypeToken<List<Mission>>() {
                }.getType());
                mMissionItemAdapter.update(selectedList);
                updateMissionToMap();
                mMapViewFragment.fitMapShowAllMissions();
                mLoadPlanningPopDialog.dismiss();
            }
        });
    }

    private View.OnClickListener missionFunctionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMoreFunctionPopupDialog.dismiss();

            switch (v.getId()) {
                case R.id.save_mission_button:
                    mMapViewFragment.getMissionPlanPathLengthAndTime(mMissionItemAdapter.getMissionsSpeed());
                    break;
                case R.id.load_mission_button:
                    showLoadMissionPopDialog();
                    break;
                case R.id.clear_mission_button:
                    mMissionItemAdapter.clearMission();
                    missionAdapterShowDelete(false);
                    updateMissionToMap();
                    break;
            }
        }
    };

    private View.OnClickListener loadMissionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.load_mission_cancel_button:
                    mLoadPlanningPopDialog.dismiss();
                    break;
            }
        }
    };

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
        mLoadPlanningDAO.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.undo_button:
                mMissionItemAdapter.undo();
                mMissionItemAdapter.onNothingSelected();
                mWayPointDetailPanel.setVisibility(View.GONE);
                updateMissionToMap();
                break;
            case R.id.more_button:
                showMoreFunctionPopupDialog(v);
                break;
            case R.id.delete_all_button:
                mMissionItemAdapter.clearMission();
                missionAdapterShowDelete(false);
                updateMissionToMap();
                break;
            case R.id.delete_done_button:
                missionAdapterShowDelete(false);
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
        }
    }

    private void showMoreFunctionPopupDialog(View v) {
        mMoreFunctionPopupDialog = new Dialog(getActivity());
        mMoreFunctionPopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMoreFunctionPopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mMoreFunctionPopupDialog.setContentView(R.layout.popwindow_more_function);
        WindowManager.LayoutParams wmlp = mMoreFunctionPopupDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        int[] viewLocationInPx = new int[2];
        v.getLocationOnScreen(viewLocationInPx);
        wmlp.x = viewLocationInPx[0] - getResources().getDimensionPixelOffset(R.dimen.popdialog_more_function_width) + getResources().getDimensionPixelOffset(R.dimen.popdialog_more_function_peak_right_margin) + (v.getWidth() / 2);
        wmlp.y = viewLocationInPx[1] + v.getHeight();
        mMoreFunctionPopupDialog.getWindow().setAttributes(wmlp);
        mMoreFunctionPopupDialog.show();

        mSaveMissionButton = (Button) mMoreFunctionPopupDialog.findViewById(R.id.save_mission_button);
        mSaveMissionButton.setOnClickListener(missionFunctionClickListener);
        mMoreFunctionPopupDialog.findViewById(R.id.load_mission_button).setOnClickListener(missionFunctionClickListener);
        mClearMissionButton = (Button) mMoreFunctionPopupDialog.findViewById(R.id.clear_mission_button);
        mClearMissionButton.setOnClickListener(missionFunctionClickListener);

        setSaveOrClearButtonEnable();
    }

    private void setSaveOrClearButtonEnable() {
        mSaveMissionButton.setEnabled(mSaveAndClearMissionFlag);
        mClearMissionButton.setEnabled(mSaveAndClearMissionFlag);
    }

    private void showLoadMissionPopDialog() {
        mLoadPlanningPopDialog = new Dialog(getActivity());
        mLoadPlanningPopDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadPlanningPopDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mLoadPlanningPopDialog.setContentView(R.layout.popwindow_load_mission);

        GridView mLoadPlanningGridView = (GridView) mLoadPlanningPopDialog.findViewById(R.id.load_mission_grid_view);
        mLoadPlanningGridView.setAdapter(mLoadPlanningListAdapter);

        mLoadPlanningPopDialog.show();

        mLoadPlanningPopDialog.findViewById(R.id.load_mission_cancel_button).setOnClickListener(loadMissionClickListener);
    }

    private void loadMissionFromDrone() {
        if (!mMapViewFragment.getDroneController().readMissions(this)) {
            return;
        }
        showLoadProgressDialog("Loading", "Please wait...");
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
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
        if (mWayPointDetailPanel.getVisibility() == View.VISIBLE) {
            mMissionItemAdapter.onNothingSelected();
            mWayPointDetailPanel.setVisibility(View.GONE);
            return;
        }
        mMissionItemAdapter.add(mMissionBuilder.setLatitude(lat).setLongitude(lon).create());
        mMissionItemAdapter.onNothingSelected();
        updateMissionToMap();
    }

    @Override
    public void onMapDragStartEvent() {
        mMissionItemAdapter.onNothingSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
    }

    @Override
    public void onGetMissionPlanPathDistanceAndFlightTimeCallback(int lengthInMeters, int timeInSeconds) {
        PlanningData.Builder missionListsBuilder = new PlanningData.Builder();
        PlanningData newMissionListItem = missionListsBuilder.setDistance(lengthInMeters)
                .setFlightTime(timeInSeconds)
                .setPlanningContent(mMissionItemAdapter.getMissionToJSON())
                .setImageContent(getBitmapAsByteArray(mMapViewFragment.getWebViewScreenShot(WEBVIEW_SCREENSHOT_WIDTH, WEBVIEW_SCREENSHOT_HEIGHT)))
                .create();
        mLoadPlanningDAO.insert(newMissionListItem);
        mLoadPlanningListAdapter.update(mLoadPlanningDAO.getAll());
    }

    @Override
    public void onMissionLatitudeUpdate(float latitude) {
        mMissionItemAdapter.updateSelectedItemLatitude(latitude);
        updateMissionToMap();
    }

    @Override
    public void onMissionLongitudeUpdate(float longitude) {
        mMissionItemAdapter.updateSelectedItemLongitude(longitude);
        updateMissionToMap();
    }

    @Override
    public void onMissionTypeUpdate(Mission.Type missionType) {
        mMissionItemAdapter.updateSelectedItemType(missionType);
        updateMissionToMap();
    }

    @Override
    public void onMissionAltitudeUpdate(float missionAltidude) {
        mMissionItemAdapter.updateSelectedItemAltitude(missionAltidude);
    }

    @Override
    public void onMissionDelayUpdate(int seconds) {
        mMissionItemAdapter.updateSelectedItemDelay(seconds);
    }

    @Override
    public void onMissionDeleted() {
        mMissionItemAdapter.removeSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
        updateMissionToMap();
    }

}
