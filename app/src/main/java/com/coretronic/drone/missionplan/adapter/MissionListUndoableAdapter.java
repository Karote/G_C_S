package com.coretronic.drone.missionplan.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by karot.chuang on 2015/5/15.
 */
public class MissionListUndoableAdapter extends RecyclerView.Adapter<MissionListUndoableAdapter.MissionListUndoableViewHolder> {

    private final static int UNDO_LIST_MAX_SIZE = 64;
    private List<Mission> mMissionList = null;
    private OnListStateChangedListener mItemClickListener = null;
    private boolean mIsDeleteLayoutVisible = false;
    private int mFocusIndex = -1;
    private LimitedStack<List<Mission>> mUndoLists;
    private Context mContext;

    public MissionListUndoableAdapter() {
        this.mMissionList = new ArrayList<>();
        this.mUndoLists = new LimitedStack<>(UNDO_LIST_MAX_SIZE);
    }

    public Mission getSelectedItem() {
        return mMissionList.get(mFocusIndex);
    }

    public void removeSelected() {
        if (mFocusIndex == -1) {
            return;
        }
        mUndoLists.push(new ArrayList<>(mMissionList));
        mMissionList.remove(mFocusIndex);
        mItemClickListener.onSaveAndClearMissionEnable(canSaveAndClearFunction());
        mFocusIndex = -1;
        notifyDataSetChanged();
    }

    public void updateMissionItemLocation(int index, float lat, float lon) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(index).clone();
        mission.setLatitude(lat);
        mission.setLongitude(lon);
        mMissionList.set(index, mission);
        notifyDataSetChanged();
    }

    public void updateSelectedItemLatitude(float latitude) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(mFocusIndex).clone();
        mission.setLatitude(latitude);
        mMissionList.set(mFocusIndex, mission);
        notifyDataSetChanged();
    }

    public void updateSelectedItemLongitude(float longitude) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(mFocusIndex).clone();
        mission.setLongitude(longitude);
        mMissionList.set(mFocusIndex, mission);
        notifyDataSetChanged();
    }

    public void updateSelectedItemType(Mission.Type missionType) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(mFocusIndex).clone();
        mission.setType(missionType);
        mMissionList.set(mFocusIndex, mission);
        notifyDataSetChanged();
    }

    public void updateSelectedItemAltitude(float altidude) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(mFocusIndex).clone();
        mission.setAltitude(altidude);
        mMissionList.set(mFocusIndex, mission);
        notifyDataSetChanged();
    }

    public void updateSelectedItemDelay(int seconds) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(mFocusIndex).clone();
        mission.setWaitSeconds(seconds);
        mMissionList.set(mFocusIndex, mission);
        notifyDataSetChanged();
    }

    private List<Mission> cloneMissions() {
        List<Mission> clonedMissions = new ArrayList<>();
        for (Mission mission : mMissionList) {
            clonedMissions.add(mission.clone());
        }
        return clonedMissions;
    }

    public List<Integer> getMissionsSpeed() {
        List<Integer> speedList = new ArrayList<>();
        for (Mission mission : mMissionList) {
//            speedList.add(mission.getSpeed());
            speedList.add(8);
        }
        return speedList;
    }

    public interface OnListStateChangedListener {

        void onItemDeleted(int position);

        void onItemSelected(Mission mission, int currentIndex);

        void onNothingSelected();

        void onUndoOptionEnable(boolean enable);

        void onSaveAndClearMissionEnable(boolean enable);
    }

    public void setOnAdapterListChangedListener(final OnListStateChangedListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public MissionListUndoableViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.points_list_item, viewGroup, false);
        mContext = viewGroup.getContext();
        return new MissionListUndoableViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MissionListUndoableViewHolder viewHolder, final int position) {

        final Mission mission = mMissionList.get(position);

        viewHolder.serialNumberTextView.setText(position + 1 + "");
        viewHolder.altitudeTextView.setText((int) mission.getAltitude() + "");
        ((ImageView) viewHolder.typeView).setImageResource(getTypeResource(mission.getType()));

        if (position == mFocusIndex) {
            viewHolder.focusBar.setVisibility(View.VISIBLE);
            viewHolder.serialNumberTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            viewHolder.serialNumberTextView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        } else {
            viewHolder.focusBar.setVisibility(View.INVISIBLE);
            viewHolder.serialNumberTextView.setTextColor(mContext.getResources().getColor(R.color.point_list_item_row_name_unselected));
            viewHolder.serialNumberTextView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        }

        if (mIsDeleteLayoutVisible) {
            viewHolder.deleteButton.setVisibility(View.VISIBLE);
            viewHolder.rowItemLayout.setEnabled(false);
        } else {
            viewHolder.deleteButton.setVisibility(View.GONE);
            viewHolder.rowItemLayout.setEnabled(true);
        }

        viewHolder.rowItemLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFocusIndex == position) {
                    viewHolder.focusBar.setVisibility(View.INVISIBLE);
                    viewHolder.serialNumberTextView.setTextColor(mContext.getResources().getColor(R.color.point_list_item_row_name_unselected));
                    viewHolder.serialNumberTextView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                    mFocusIndex = -1;
                    if (mItemClickListener != null) {
                        mItemClickListener.onNothingSelected();
                    }
                } else {
                    viewHolder.focusBar.setVisibility(View.VISIBLE);
                    viewHolder.serialNumberTextView.setTextColor(mContext.getResources().getColor(R.color.white));
                    viewHolder.serialNumberTextView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                    mFocusIndex = position;
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemSelected(mission, mFocusIndex);
                    }
                }
            }
        });

        viewHolder.deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(position);
                if (mItemClickListener != null) {
                    mItemClickListener.onItemDeleted(position);
                }
            }
        });

    }

    private int getTypeResource(Type type) {

        if (type == null) {
            return R.drawable.ico_indicator_plan_waypoint;
        }

        switch (type) {
            case TAKEOFF:
                return R.drawable.ico_indicator_plan_takeoff;
            case LAND:
                return R.drawable.ico_indicator_plan_landing;
            case RTL:
                return R.drawable.ico_indicator_plan_home;
            case CAMERA_TRIGGER_DISTANCE:
                return R.drawable.ico_indicator_plan_camera;
            case WAY_POINT:
            default:
                return R.drawable.ico_indicator_plan_waypoint;
        }
    }

    @Override
    public int getItemCount() {
        return mMissionList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class MissionListUndoableViewHolder extends RecyclerView.ViewHolder {
        final TextView serialNumberTextView, altitudeTextView;
        final View typeView;
        final ImageButton deleteButton;
        final RelativeLayout rowItemLayout;
        final View focusBar;

        MissionListUndoableViewHolder(View itemView) {
            super(itemView);
            serialNumberTextView = (TextView) itemView.findViewById(R.id.rowNameView);
            typeView = itemView.findViewById(R.id.icon_waypoint_type);
            altitudeTextView = (TextView) itemView.findViewById(R.id.rowAltitudeView);
            deleteButton = (ImageButton) itemView.findViewById(R.id.btn_plan_waypoint_delet);
            rowItemLayout = (RelativeLayout) itemView.findViewById(R.id.rowItemLayout);
            focusBar = itemView.findViewById(R.id.view_focusbar);
        }
    }

    public void add(Mission mission) {
        mUndoLists.push(new ArrayList<>(mMissionList));
        if (!canSaveAndClearFunction()) {
            mItemClickListener.onSaveAndClearMissionEnable(true);
        }
        mMissionList.add(mission);
        notifyDataSetChanged();
    }

    public void clearMission() {
        if (mMissionList.isEmpty()) {
            return;
        }
        mUndoLists.push(new ArrayList<>(mMissionList));
        mMissionList.clear();
        mItemClickListener.onSaveAndClearMissionEnable(false);
        notifyDataSetChanged();
    }

    public String getMissionToJSON() {
        Gson gson = new Gson();
        return gson.toJson(mMissionList);
    }

    public void update(List<Mission> missions) {
        mUndoLists.clear();
        if (missions.isEmpty()) {
            clearMission();
            return;
        }
        mMissionList = missions;
        mItemClickListener.onSaveAndClearMissionEnable(true);
        notifyDataSetChanged();
    }

    private void remove(int position) {
        mUndoLists.push(new ArrayList<>(mMissionList));
        mMissionList.remove(position);
        mItemClickListener.onSaveAndClearMissionEnable(canSaveAndClearFunction());
        notifyDataSetChanged();
    }

    public List<Mission> getMissions() {
        return mMissionList;
    }

    public void undo() {
        mMissionList = mUndoLists.pop();
        if (!canUndo()) {
            mItemClickListener.onUndoOptionEnable(false);
        }
        mItemClickListener.onSaveAndClearMissionEnable(canSaveAndClearFunction());
        notifyDataSetChanged();
    }

    private boolean canUndo() {
        return !mUndoLists.isEmpty();
    }

    private boolean canSaveAndClearFunction() {
        return !mMissionList.isEmpty();
    }

    public Mission getMission(int position) {
        return mMissionList.get(position);
    }

    public void setDeleteLayoutVisible(boolean isVisible) {
        mIsDeleteLayoutVisible = isVisible;
        onNothingSelected();
    }

    public void onNothingSelected() {
        mFocusIndex = -1;
        notifyDataSetChanged();
    }

    private List<Mission> deepCopyMissionList() {
        List<Mission> missions = new ArrayList<>();
        for (Mission mission : mMissionList) {
            missions.add(mission.clone());
        }
        return missions;
    }

    private class LimitedStack<E> extends Stack<E> {

        private int maxSize = 50;

        public LimitedStack(int undoListMaxSize) {
            this.maxSize = undoListMaxSize;
        }

        @Override
        public E push(E object) {
            while (size() >= maxSize) {
                remove(0);
            }
            if (!canUndo()) {
                mItemClickListener.onUndoOptionEnable(true);
            }
            super.push(object);
            return object;
        }
    }

}