package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.coretronic.drone.util.AppConfig;
import com.coretronic.drone.util.ConstantValue;
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
    private final static int DEFAULT_RADIUS = 0;
    private final static Type DEFAULT_TYPE = Type.WAY_POINT;
    private final static int WEBVIEW_SCREENSHOT_WIDTH = 432;
    private final static int WEBVIEW_SCREENSHOT_HEIGHT = 318;
    private final static int SELECT_NONE = 0;
    private final static int SELECTED_ONE = 1;
    private final static int SELECT_ALL = 2;
    private final static float DRONE_LOCATION_INVALID = Float.MAX_VALUE;

    private View mWaypointListTopView;
    private View mWaypointListEditHeaderPanel;
    private View mWaypointListHeaderPanel;
    private TextView mWaypointListHeaderCountText;
    private ToggleButton mWaypointListHeaderSelectAllToggleButton;
    private View mWaypointListHeaderDeleteButton;
    private View mWaypointListHeaderSettingButton;
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
    private WaypointListEditSettingDialog mWaypointSettingDialog;
    private RecyclerView mRecyclerView;
    private int mTopViewVisibility;
    private float mDroneLat = DRONE_LOCATION_INVALID;
    private float mDroneLon = DRONE_LOCATION_INVALID;
    private Dialog mAutoRTLPopDialog;

    private SharedPreferences mSharedPreferences;

    public static PlanningFragment newInstance(boolean isFromHistory) {
        PlanningFragment fragment = new PlanningFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_From_History, isFromHistory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMissionBuilder = new Builder();
        int defaultAlt = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.ALTITUDE_DEFAULT_VALUE);
        int defaultSpeed = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_HORIZONTAL_SPEED_DEFAULT_FOR_WAYPOINT, ConstantValue.SPEED_DEFAULT_VALUE);
        mMissionBuilder.setAltitude(defaultAlt).setType(DEFAULT_TYPE).setAutoContinue(DEFAULT_AUTO_CONTINUE)
                .setWaitSeconds(ConstantValue.STAY_DEFAULT_VALUE).setSpeed(defaultSpeed).setRadius(DEFAULT_RADIUS);
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
        mWaypointListTopView.findViewById(R.id.waypoint_list_header_edit_text).setOnClickListener(missionListEditFunctionListener);
        mWaypointListHeaderDeleteButton = mWaypointListTopView.findViewById(R.id.waypoint_list_header_delete_button);
        mWaypointListHeaderDeleteButton.setEnabled(false);
        mWaypointListHeaderDeleteButton.setOnClickListener(missionListEditFunctionListener);
        mWaypointListHeaderSettingButton = mWaypointListTopView.findViewById(R.id.waypoint_list_header_setting_button);
        mWaypointListHeaderSettingButton.setEnabled(false);
        mWaypointListHeaderSettingButton.setOnClickListener(missionListEditFunctionListener);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new FixedLinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mMissionItemAdapter = new MissionListUndoableAdapter();
        mRecyclerView.setAdapter(mMissionItemAdapter);

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
                mSaveAndClearMissionFlag = !isEmpty;
                mTopViewVisibility = isEmpty ? View.GONE : View.VISIBLE;
                mWaypointListTopView.setVisibility(mTopViewVisibility);
                if (mDroneController != null) {
                    mMapViewFragment.setGoButtonEnable(!isEmpty);
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

    private View.OnClickListener missionSaveLoadFunctionClickListener = new View.OnClickListener() {
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
                    updateMissionToMap();
                    break;
                case R.id.load_mission_cancel_button:
                    mLoadPlanningPopDialog.dismiss();
                    break;
            }
        }
    };

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
//                        if (mDroneController != null) {
//                            mDroneController.startMission(missions.get(0).getLatitude(), missions.get(0).getLongitude(), missions.get(0).getAltitude());
//                        }
                        mLoadMissionProgressDialog.dismiss();
                    }
                }
            });
        }

        @Override
        public void onWriteMissionStatusUpdate(int seq, int total, boolean isComplete) {
            if (isComplete && seq == total - 1) {
                if (mDroneController != null) {
                    List<Mission> droneMissionList = mMissionItemAdapter.getMissions();
                    mDroneController.startMission(droneMissionList.get(0).getLatitude(), droneMissionList.get(0).getLongitude(), droneMissionList.get(0).getAltitude());
                }
                mLoadMissionProgressDialog.dismiss();
            } else if (isComplete && seq != total - 1) {
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

    private void missionAdapterShowSelect(boolean isShow) {
        mWayPointDetailPanel.setVisibility(View.GONE);
        if (isShow) {
            mMissionItemAdapter.setSelectLayoutVisible(true);
        } else {
            mMissionItemAdapter.setSelectLayoutVisible(false);
        }
    }

    private void updateMissionToMap() {
        mMapViewFragment.updateMissions(mMissionItemAdapter.getMissions());
        mWaypointListHeaderCountText.setText(String.valueOf(mMissionItemAdapter.getItemCount() - 1));
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
            case R.id.plan_go_button:
                List<Mission> droneMissionList = mMissionItemAdapter.getMissions();
                if (droneMissionList == null || droneMissionList.size() == 0) {
                    showToastMessage("There is no mission existed");
                    return;
                }
                if (droneMissionList.get(droneMissionList.size() - 1).getType() != Type.RTL) {
                    showAutoRTLPopDialog();
                } else {
                    mDroneController.writeMissions(droneMissionList, missionLoaderListener);
                    showLoadProgressDialog("Writing Mission", "Please wait...");
                }
                break;
            case R.id.plan_stop_button:
                if (mDroneController != null) {
                    mDroneController.stopMission();
                }
                break;
            case R.id.plan_pause_button:
                if (mDroneController != null) {
                    mDroneController.pauseMission();
                }
                break;
            case R.id.plan_play_button:
                if (mDroneController != null) {
                    mDroneController.resumeMission();
                }
                break;
        }
    }

    private void showAutoRTLPopDialog() {
        mAutoRTLPopDialog = new Dialog(getActivity());
        mAutoRTLPopDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mAutoRTLPopDialog.setCanceledOnTouchOutside(false);
        mAutoRTLPopDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAutoRTLPopDialog.setContentView(R.layout.popdialog_auto_return_home);
        mAutoRTLPopDialog.show();

        mAutoRTLPopDialog.findViewById(R.id.go_without_rtl_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoRTLPopDialog.dismiss();
            }
        });

        mAutoRTLPopDialog.findViewById(R.id.go_with_rtl_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMissionItemAdapter.updateLastItemToRTL();
                updateMissionToMap();

                mAutoRTLPopDialog.dismiss();
            }
        });

        mAutoRTLPopDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                List<Mission> droneMissionList = mMissionItemAdapter.getMissions();
                mDroneController.writeMissions(droneMissionList, missionLoaderListener);
                showLoadProgressDialog("Writing Mission", "Please wait...");
            }
        });
    }

    private void showMoreFunctionPopupDialog(View v) {
        mMoreFunctionPopupDialog = new Dialog(getActivity());
        mMoreFunctionPopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMoreFunctionPopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mMoreFunctionPopupDialog.setContentView(R.layout.popwindow_more_function);
        WindowManager.LayoutParams wmlp = mMoreFunctionPopupDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.START;
        int[] viewLocationInPx = new int[2];
        v.getLocationOnScreen(viewLocationInPx);
        wmlp.x = viewLocationInPx[0] - getResources().getDimensionPixelOffset(R.dimen.popdialog_more_function_width) + getResources().getDimensionPixelOffset(R.dimen.popdialog_more_function_peak_right_margin) + (v.getWidth() / 2);
        wmlp.y = viewLocationInPx[1] + v.getHeight();
        mMoreFunctionPopupDialog.getWindow().setAttributes(wmlp);
        mMoreFunctionPopupDialog.show();

        mSaveMissionButton = (Button) mMoreFunctionPopupDialog.findViewById(R.id.save_mission_button);
        mSaveMissionButton.setOnClickListener(missionSaveLoadFunctionClickListener);
        mMoreFunctionPopupDialog.findViewById(R.id.load_mission_button).setOnClickListener(missionSaveLoadFunctionClickListener);
        mClearMissionButton = (Button) mMoreFunctionPopupDialog.findViewById(R.id.clear_mission_button);
        mClearMissionButton.setOnClickListener(missionSaveLoadFunctionClickListener);

        setSaveOrClearButtonEnable();
    }

    private void setSaveOrClearButtonEnable() {
        mSaveMissionButton.setEnabled(mSaveAndClearMissionFlag);
        mClearMissionButton.setEnabled(mSaveAndClearMissionFlag);
    }

    private void showLoadMissionPopDialog() {
        mLoadPlanningPopDialog = new Dialog(getActivity());
        mLoadPlanningPopDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadPlanningPopDialog.setCanceledOnTouchOutside(false);
        mLoadPlanningPopDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mLoadPlanningPopDialog.setContentView(R.layout.dialog_load_mission);
        WindowManager.LayoutParams wmlp = mLoadPlanningPopDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mLoadPlanningPopDialog.getWindow().setAttributes(wmlp);

        GridView mLoadPlanningGridView = (GridView) mLoadPlanningPopDialog.findViewById(R.id.load_mission_grid_view);
        mLoadPlanningGridView.setAdapter(mLoadPlanningListAdapter);

        mLoadPlanningPopDialog.show();

        mLoadPlanningPopDialog.findViewById(R.id.load_mission_cancel_button).setOnClickListener(missionSaveLoadFunctionClickListener);
    }

    private void loadMissionFromDrone() {
//        if (!mDroneController.readMissions(this)) {
//            return;
//        }
//        showLoadProgressDialog("Loading", "Please wait...");
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

    @Override
    public void onWriteMissionStatusUpdate(final int seq, final int total, final boolean isComplete) {

    }

    private void showLoadProgressDialog(String title, String message) {
        if (mLoadMissionProgressDialog == null) {
            mLoadMissionProgressDialog = new ProgressDialog(getActivity());
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
        if (mMissionItemAdapter.getItemCount() == 0) {
            mMissionItemAdapter.addFirstPoint(
                    mMissionBuilder.setLatitude(mDroneLat).setLongitude(mDroneLon).setType(Type.TAKEOFF).create(),
                    mMissionBuilder.setLatitude(lat).setLongitude(lon).setType(DEFAULT_TYPE).create());
        } else {
            mMissionItemAdapter.add(mMissionBuilder.setLatitude(lat).setLongitude(lon).setType(DEFAULT_TYPE).create());
        }
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
    public void onMissionStayUpdate(int seconds) {
        mMissionItemAdapter.updateSelectedItemStay(seconds);
    }

    @Override
    public void onMissionDeleted() {
        mMissionItemAdapter.removeSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
        updateMissionToMap();
    }

    @Override
    public void onMissionSpeedUpdate(int missionSpeed) {
        mMissionItemAdapter.updateSelectedItemSpeed(missionSpeed);
    }

    @Override
    public void onFPVShowed() {
        mWaypointListTopView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mMissionItemAdapter.onNothingSelected();
        mWayPointDetailPanel.setVisibility(View.GONE);
    }

    @Override
    public void onFPVHided() {
        mWaypointListTopView.setVisibility(mTopViewVisibility);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateDroneLocation(float droneLat, float droneLon) {
        mDroneLat = droneLat;
        mDroneLon = droneLon;
    }

}
