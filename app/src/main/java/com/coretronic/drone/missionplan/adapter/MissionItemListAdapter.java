package com.coretronic.drone.missionplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.Mission;
import com.coretronic.drone.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/5/15.
 */
public class MissionItemListAdapter extends RecyclerView.Adapter<MissionItemListAdapter.MissionItemListViewHolder> {
    private List<Mission> mMissionList;
    private Boolean isDeleteLayoutVisible = false;
    private int focusIndex = -1;

    public MissionItemListAdapter() {
        this.mMissionList = new ArrayList<Mission>();
    }

    public interface OnItemClickListener {
        void onItemDeleteClick(View view, int position);

        void onItemPlanClick(View view, int position);
    }

    public static OnItemClickListener mItemClickListener = null;

    public void SetOnItemClickListener(final OnItemClickListener listener) {
        mItemClickListener = listener;
    }


    @Override
    public MissionItemListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.points_list_item, viewGroup, false);
        return new MissionItemListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MissionItemListViewHolder viewHolder, int i) {
        viewHolder.nameView.setText(String.format("%2d", i + 1));
        viewHolder.altitudeView.setText(String.format("%d", (int) mMissionList.get(i).getAltitude()));
        switch (mMissionList.get(i).getType()) {
            case TAKEOFF:
                viewHolder.typeView.setBackgroundResource(R.drawable.ico_indicator_plan_takeoff);
                break;
            case LAND:
                viewHolder.typeView.setBackgroundResource(R.drawable.ico_indicator_plan_land);
                break;
            case WAY_POINT:
            default:
                viewHolder.typeView.setBackgroundResource(R.drawable.ico_indicator_plan_waypoint);
                break;
        }

        if (i == focusIndex) {
            viewHolder.focusBarView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.focusBarView.setVisibility(View.GONE);
        }

        if (isDeleteLayoutVisible) {
            viewHolder.deleteLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.deleteLayout.setVisibility(View.GONE);
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

    public class MissionItemListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView nameView, altitudeView;
        final View typeView;
        final LinearLayout deleteLayout;
        final ImageButton deleteButton;
        final RelativeLayout rowItemLayoutView;
        final View focusBarView;

        MissionItemListViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.rowNameView);
            typeView = (View) itemView.findViewById(R.id.icon_waypoint_type);
            altitudeView = (TextView) itemView.findViewById(R.id.rowAltitudeView);
            deleteLayout = (LinearLayout) itemView.findViewById(R.id.rowDeleteLayout);
            deleteLayout.setVisibility(LinearLayout.GONE);
            deleteButton = (ImageButton) itemView.findViewById(R.id.btn_plan_waypoint_delet);
            rowItemLayoutView = (RelativeLayout) itemView.findViewById(R.id.rowItemLayout);
            focusBarView = (View) itemView.findViewById(R.id.view_focusbar);

            deleteButton.setOnClickListener(this);
            rowItemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.equals(deleteButton)) {
                mItemClickListener.onItemDeleteClick(v, getAdapterPosition());
            } else if (mItemClickListener != null) {

                if (isDeleteLayoutVisible)
                    return;

                mItemClickListener.onItemPlanClick(v, getAdapterPosition());

                if (focusIndex != getAdapterPosition()) {
                    focusIndex = getAdapterPosition();
                } else {
                    focusIndex = -1;
                }
                notifyDataSetChanged();
            }
        }
    }

    public void add(Mission mission) {
        mMissionList.add(mission);
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
    }

    public List<Mission> cloneMissionList() {
        return new ArrayList<Mission>(mMissionList);
    }

    public Mission getMission(int position) {
        return mMissionList.get(position);
    }

    public int getFocusIndex() {
        return focusIndex;
    }

    public void clearFocusIndex() {
        focusIndex = -1;
    }

    public void setDeleteLayoutVisible(boolean isVisible) {
        isDeleteLayoutVisible = isVisible;

        if (isVisible) {
            focusIndex = -1;
            notifyDataSetChanged();
        }
    }

}