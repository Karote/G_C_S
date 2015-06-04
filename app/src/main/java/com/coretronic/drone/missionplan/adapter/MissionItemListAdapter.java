package com.coretronic.drone.missionplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.Waypoint;

import java.util.List;

/**
 * Created by karot.chuang on 2015/5/15.
 */
public class MissionItemListAdapter extends RecyclerView.Adapter<MissionItemListAdapter.MissionItemListViewHolder> {
    private List<Waypoint> waypointList;
//    OnItemClickListener mItemClickListener;

    public MissionItemListAdapter(List<Waypoint> waypointList) {
        this.waypointList = waypointList;
    }

    public Boolean isVisible = false;

    public class MissionItemListViewHolder extends RecyclerView.ViewHolder/* implements View.OnClickListener*/ {
        final TextView nameView;
        final TextView altitudeView;
        final FrameLayout deleteLayout;
        final Button deleteButton;

        MissionItemListViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.rowNameView);
            altitudeView = (TextView) itemView.findViewById(R.id.rowAltitudeView);
            deleteLayout = (FrameLayout) itemView.findViewById(R.id.rowDeleteLayout);
            deleteLayout.setVisibility(FrameLayout.GONE);
            deleteButton = (Button) itemView.findViewById(R.id.button_delete_marker);

//            deleteButton.setOnClickListener(this);
//            itemView.setOnClickListener(this);
        }
        /*
        @Override
        public void onClick(View v) {
//            if(v.equals(deleteButton)){
//                removeAt(getPosition());
//            }else if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
//            }
        }
        */
    }

    /*
    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
    */
    @Override
    public int getItemCount() {
        return waypointList.size();
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
        viewHolder.altitudeView.setText(String.valueOf(waypointList.get(i).getAltitude()));
        if (isVisible == false) {
            viewHolder.deleteLayout.setVisibility(FrameLayout.GONE);
        } else {
            viewHolder.deleteLayout.setVisibility(FrameLayout.VISIBLE);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}