package com.coretronic.drone.missionplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/5/15.
 */
public class MissionItemListAdapter extends RecyclerView.Adapter<MissionItemListAdapter.MissionItemListViewHolder> {

    private List<Mission> mMissionList = null;
    private OnItemSelectedListener mItemClickListener = null;
    private boolean isDeleteLayoutVisible = false;
    private int focusIndex = -1;

    public MissionItemListAdapter() {
        this.mMissionList = new ArrayList<>();
    }

    public Mission getSelectedItem() {
        return mMissionList.get(focusIndex);
    }

    public void removeSelected() {
        if (focusIndex == -1) {
            return;
        }
        mMissionList.remove(focusIndex);
        focusIndex = -1;
        notifyDataSetChanged();
    }

    public interface OnItemSelectedListener {

        void onItemDeleted(int position);

        void onItemSelected(Mission mission, int currentIndex);

        void onNothingSelected();
    }

    public void setOnItemClickListener(final OnItemSelectedListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public MissionItemListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.points_list_item, viewGroup, false);
        return new MissionItemListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MissionItemListViewHolder viewHolder, final int position) {

        final Mission mission = mMissionList.get(position);

        viewHolder.nameView.setText(position + 1 + "");
        viewHolder.altitudeView.setText((int) mission.getAltitude() + "");
        viewHolder.typeView.setBackgroundResource(getTypeResource(mission.getType()));

        if (position == focusIndex) {
            viewHolder.focusBarView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.focusBarView.setVisibility(View.GONE);
        }

        if (isDeleteLayoutVisible) {
            viewHolder.deleteLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.deleteLayout.setVisibility(View.GONE);
        }

        viewHolder.rowItemLayoutView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (focusIndex == position) {
                    viewHolder.focusBarView.setVisibility(View.GONE);
                    focusIndex = -1;
                    if (mItemClickListener != null) {
                        mItemClickListener.onNothingSelected();
                    }
                } else {
                    viewHolder.focusBarView.setVisibility(View.VISIBLE);
                    focusIndex = position;
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemSelected(mission, focusIndex);
                    }
                }
            }
        });

        viewHolder.deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMissionList.remove(position);
                notifyDataSetChanged();
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
                return R.drawable.ico_indicator_plan_land;
            case RTL:
                return R.drawable.ico_indicator_plan_home;
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

    public class MissionItemListViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView, altitudeView;
        final View typeView;
        final LinearLayout deleteLayout;
        final ImageButton deleteButton;
        final RelativeLayout rowItemLayoutView;
        final View focusBarView;

        MissionItemListViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.rowNameView);
            typeView = itemView.findViewById(R.id.icon_waypoint_type);
            altitudeView = (TextView) itemView.findViewById(R.id.rowAltitudeView);
            deleteLayout = (LinearLayout) itemView.findViewById(R.id.rowDeleteLayout);
            deleteButton = (ImageButton) itemView.findViewById(R.id.btn_plan_waypoint_delet);
            rowItemLayoutView = (RelativeLayout) itemView.findViewById(R.id.rowItemLayout);
            focusBarView = itemView.findViewById(R.id.view_focusbar);
        }
    }

    public void add(Mission mission) {
        mMissionList.add(mission);
        notifyDataSetChanged();
    }

    public void addAt(Mission mission, int position) {
        mMissionList.add(position, mission);
    }

    public void remove(int position) {
        mMissionList.remove(position);
    }

    public void clearMission() {
        mMissionList.clear();
    }

    public void update(List<Mission> missions) {
        mMissionList = missions;
        notifyDataSetChanged();
    }

    public List<Mission> cloneMissionList() {
        return new ArrayList<>(mMissionList);
    }

    public Mission getMission(int position) {
        return mMissionList.get(position);
    }

    public void setDeleteLayoutVisible(boolean isVisible) {
        isDeleteLayoutVisible = isVisible;
        onNothingSelected();
    }

    public void onNothingSelected() {
        focusIndex = -1;
        notifyDataSetChanged();
    }

}