package com.coretronic.drone.missionplan.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.util.ConstantValue;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by karot.chuang on 2015/5/15.
 */
public class MissionListUndoableAdapter extends RecyclerView.Adapter<MissionListUndoableAdapter.MissionListUndoableViewHolder> {

    private final static int UNDO_LIST_MAX_SIZE = 64;
    private List<Mission> mMissionList;
    private OnListStateChangedListener mItemClickListener;
    private boolean mIsSelectLayoutVisible = false;
    private int mFocusIndex = -1;
    private LimitedStack<List<Mission>> mUndoLists;
    private Context mContext;
    private SparseBooleanArray mSelectedItems;

    public MissionListUndoableAdapter() {
        this.mMissionList = new ArrayList<>();
        this.mUndoLists = new LimitedStack<>(UNDO_LIST_MAX_SIZE);
        this.mSelectedItems = new SparseBooleanArray();
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
        mItemClickListener.onAdapterListIsEmptyOrNot(mMissionList.isEmpty());
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

    public void updateSelectedItemStay(int seconds) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(mFocusIndex).clone();
        mission.setWaitSeconds(seconds);
        mMissionList.set(mFocusIndex, mission);
        notifyDataSetChanged();
    }

    public void updateSelectedItemSpeed(int speed) {
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(mFocusIndex).clone();
        mission.setSpeed(speed);
        mMissionList.set(mFocusIndex, mission);
        notifyDataSetChanged();
    }

    public void updateLastItemToRTL(){
        int lastIndex = mMissionList.size() - 1;
        mUndoLists.push(cloneMissions());
        Mission mission = getMission(lastIndex).clone();
        mission.setType(Type.RTL);
        mMissionList.set(lastIndex, mission);
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
            speedList.add(mission.getSpeed());
        }
        return speedList;
    }

    public interface OnListStateChangedListener {

        void onItemDeleted(int position);

        void onItemSelected(Mission mission, int currentIndex);

        void onNothingSelected();

        void onUndoListIsEmptyOrNot(boolean empty);

        void onAdapterListIsEmptyOrNot(boolean isEmpty);

        void onItemChecked(int checkCount);

        void onListModified();
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

        viewHolder.serialNumberTextView.setText(position + "");
        viewHolder.altitudeTextView.setText((int) mission.getAltitude() + "");
        viewHolder.speedTextView.setText(mission.getSpeed() + "");
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

        if (mIsSelectLayoutVisible) {
            if(position == 0){
                hideHomePoint(viewHolder);
            }else {
                showHomePoint(viewHolder);

                viewHolder.serialNumberTextView.setVisibility(View.VISIBLE);
                viewHolder.takeOffView.setVisibility(View.GONE);
            }
            showSelectLayout(viewHolder, position);
        } else {
            if(position == 0){
                viewHolder.serialNumberTextView.setVisibility(View.GONE);
                viewHolder.takeOffView.setVisibility(View.VISIBLE);
            }else {
                viewHolder.serialNumberTextView.setVisibility(View.VISIBLE);
                viewHolder.takeOffView.setVisibility(View.GONE);
            }
            hideSelectLayout(viewHolder);

            showHomePoint(viewHolder);
        }

        viewHolder.rowItemLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSelectLayoutVisible) {
                    viewHolder.selectCheck.performClick();
                } else {
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
                notifyDataSetChanged();
            }
        });

        viewHolder.selectCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedItems.get(position, false)) {
                    mSelectedItems.delete(position);
                    viewHolder.selectCheck.setSelected(false);
                } else {
                    mSelectedItems.put(position, true);
                    viewHolder.selectCheck.setSelected(true);
                }
                mItemClickListener.onItemChecked(mSelectedItems.size());
            }
        });

    }

    private void showSelectLayout(MissionListUndoableViewHolder viewHolder, int position) {
        viewHolder.selectCheck.setVisibility(View.VISIBLE);
        viewHolder.selectCheck.setSelected(mSelectedItems.get(position, false));
    }

    private void hideSelectLayout(MissionListUndoableViewHolder viewHolder) {
        viewHolder.selectCheck.setVisibility(View.GONE);
    }

    private void showHomePoint(MissionListUndoableViewHolder viewHolder) {
        int rowHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.points_list_item_height);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, rowHeight);
        viewHolder.allOfRowLayout.setLayoutParams(params);
    }

    private void hideHomePoint(MissionListUndoableViewHolder viewHolder) {
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 0);
        viewHolder.allOfRowLayout.setLayoutParams(params);
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
        final RelativeLayout allOfRowLayout;
        final TextView serialNumberTextView, altitudeTextView, speedTextView;
        final View takeOffView;
        final View typeView;
        final View selectCheck;
        final RelativeLayout rowItemLayout;
        final View focusBar;

        MissionListUndoableViewHolder(View itemView) {
            super(itemView);
            allOfRowLayout = (RelativeLayout) itemView.findViewById(R.id.all_of_row_layout);
            takeOffView = itemView.findViewById(R.id.takeoff_view);
            serialNumberTextView = (TextView) itemView.findViewById(R.id.rowNameView);
            typeView = itemView.findViewById(R.id.icon_waypoint_type);
            altitudeTextView = (TextView) itemView.findViewById(R.id.rowAltitudeView);
            speedTextView = (TextView) itemView.findViewById(R.id.rowSpeedView);
            selectCheck = itemView.findViewById(R.id.btn_select_check);
            rowItemLayout = (RelativeLayout) itemView.findViewById(R.id.rowItemLayout);
            focusBar = itemView.findViewById(R.id.view_focusbar);
        }
    }

    public void addFirstPoint(Mission homePoint, Mission firstPoint) {
        mItemClickListener.onAdapterListIsEmptyOrNot(false);
        mUndoLists.push(new ArrayList<>(mMissionList));
        mMissionList.add(homePoint);
        mMissionList.add(firstPoint);
        notifyDataSetChanged();
    }

    public void add(Mission mission) {
        mUndoLists.push(new ArrayList<>(mMissionList));
        mMissionList.add(mission);
        notifyDataSetChanged();
    }

    public void clearMission() {
        if (mMissionList.isEmpty()) {
            return;
        }
        mUndoLists.push(new ArrayList<>(mMissionList));
        mMissionList.clear();
        mItemClickListener.onAdapterListIsEmptyOrNot(true);
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
        mItemClickListener.onAdapterListIsEmptyOrNot(false);
        notifyDataSetChanged();
    }

    private void remove(int position) {
        mUndoLists.push(new ArrayList<>(mMissionList));
        mMissionList.remove(position);
        mItemClickListener.onAdapterListIsEmptyOrNot(mMissionList.isEmpty());
        notifyDataSetChanged();
    }

    public List<Mission> getMissions() {
        return cloneMissions();
    }

    public List<Mission> getSelectedMissionList() {
        List<Mission> selectedList = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            Mission mission = getMission(mSelectedItems.keyAt(i));
            selectedList.add(mission);
        }
        return selectedList;
    }

    public void updateSelectedList(float altitude, int stay, int speed) {
        for (int i = 0; i < mSelectedItems.size(); i++) {
            Mission updateMission = getMission(mSelectedItems.keyAt(i)).clone();
            if (altitude >= ConstantValue.ALTITUDE_MIN_VALUE)
                updateMission.setAltitude(altitude);
            if (stay >= ConstantValue.STAY_MIN_VALUE)
                updateMission.setWaitSeconds(stay);
            if (speed >= ConstantValue.SPEED_MIN_VALUE)
                updateMission.setSpeed(speed);

            mMissionList.set(mSelectedItems.keyAt(i), updateMission);
        }
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public void undo() {
        mMissionList = mUndoLists.pop();
        mItemClickListener.onUndoListIsEmptyOrNot(mUndoLists.isEmpty());
        mItemClickListener.onAdapterListIsEmptyOrNot(mMissionList.isEmpty());
        notifyDataSetChanged();
    }

    public Mission getMission(int position) {
        return mMissionList.get(position);
    }

    public void setSelectLayoutVisible(boolean isVisible) {
        mSelectedItems.clear();
        mIsSelectLayoutVisible = isVisible;
        onNothingSelected();
    }

    public void onNothingSelected() {
        mFocusIndex = -1;
        notifyDataSetChanged();
    }

    public void setAllItemChecked(boolean isCheck) {
        if (isCheck) {
            for (int i = 1; i < mMissionList.size(); i++) {
                mSelectedItems.put(i, true);
            }
        } else {
            mSelectedItems.clear();
        }
        notifyDataSetChanged();
    }

    public void deleteSelectedItem() {
        List<Mission> selectedList = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            Mission mission = getMission(mSelectedItems.keyAt(i));
            selectedList.add(mission);
        }
        mMissionList.removeAll(selectedList);
        mSelectedItems.clear();
        mItemClickListener.onListModified();
        notifyDataSetChanged();
    }

    public void enterMissionListEditMode() {
        mUndoLists.push(new ArrayList<>(mMissionList));
    }

    public void exitMissionListEditMode() {
        mMissionList = mUndoLists.pop();
    }

    public void finishMissionListEditMode(){
        if(mMissionList.size() == 1){
            mMissionList.clear();
            mItemClickListener.onAdapterListIsEmptyOrNot(true);
            notifyDataSetChanged();
        }
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
            super.push(object);
            mItemClickListener.onUndoListIsEmptyOrNot(mUndoLists.isEmpty());
            return object;
        }
    }

}