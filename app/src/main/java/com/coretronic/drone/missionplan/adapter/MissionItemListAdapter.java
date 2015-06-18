package com.coretronic.drone.missionplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
public class MissionItemListAdapter extends RecyclerView.Adapter<MissionItemListAdapter.MissionItemListViewHolder>{
    private List<Mission> mMissionList;
    public Boolean isVisible = false;

    public MissionItemListAdapter(){
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
        MissionItemListViewHolder pvh = new MissionItemListViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(MissionItemListViewHolder viewHolder, int i) {
        viewHolder.nameView.setText(String.format("%2d", i + 1));
        viewHolder.altitudeView.setText(String.valueOf(mMissionList.get(i).getAltitude()));
        if( isVisible == false ){
            viewHolder.deleteLayout.setVisibility(LinearLayout.GONE);
        }else{
            viewHolder.deleteLayout.setVisibility(LinearLayout.VISIBLE);
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

    public class MissionItemListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView nameView;
        final TextView altitudeView;
        final LinearLayout deleteLayout;
        final ImageButton deleteButton;
        final RelativeLayout rowItemLayoutView;

        MissionItemListViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView)itemView.findViewById(R.id.rowNameView);
            altitudeView = (TextView)itemView.findViewById(R.id.rowAltitudeView);
            deleteLayout = (LinearLayout)itemView.findViewById(R.id.rowDeleteLayout);
            deleteLayout.setVisibility(LinearLayout.GONE);
            deleteButton = (ImageButton)itemView.findViewById(R.id.btn_plan_waypoint_delet);
            rowItemLayoutView = (RelativeLayout)itemView.findViewById(R.id.rowItemLayout);

            deleteButton.setOnClickListener(this);
            rowItemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.equals(deleteButton)){
                mItemClickListener.onItemDeleteClick(v, getAdapterPosition());
            }else if (mItemClickListener != null) {
                mItemClickListener.onItemPlanClick(v, getAdapterPosition());
            }
        }
    }

    public void add(Mission mission) {
        mMissionList.add(mission);
    }

    public void addAt(Mission mission, int position){
        mMissionList.add(position, mission);
    }

    public void remove(int position){
        mMissionList.remove(position);
    }

    public void clearMission(){
        mMissionList.clear();
    }

    public void update(List<Mission> missions) {
        mMissionList = missions;
    }

    public List<Mission> getMissionList() {
        return mMissionList;
    }

}