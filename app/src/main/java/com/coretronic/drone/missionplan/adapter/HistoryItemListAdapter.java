package com.coretronic.drone.missionplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.model.FlightHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by karot.chuang on 2015/8/4.
 */
public class HistoryItemListAdapter extends RecyclerView.Adapter<HistoryItemListAdapter.HistoryItemListViewHolder> {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private List<FlightHistory> flightHistoryList = null;
    private int focusIndex = -1;

    public HistoryItemListAdapter() {
        this.flightHistoryList = new ArrayList<>();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mItemClickListener = null;

    public void SetOnItemClickListener(final OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public HistoryItemListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_log_listitem, viewGroup, false);
        return new HistoryItemListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HistoryItemListViewHolder viewHolder, int i) {
        Date resultdate = new Date(flightHistoryList.get(i).getCreatedTime());
        viewHolder.tvLogDate.setText(dateFormat.format(resultdate));
        viewHolder.tvLogTime.setText(timeFormat.format(resultdate));

        if (i == focusIndex) {
            viewHolder.focusBarView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.focusBarView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return flightHistoryList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class HistoryItemListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvLogDate;
        final TextView tvLogTime;
        final View focusBarView;
        final RelativeLayout rowItemLayoutView;

        HistoryItemListViewHolder(View itemView) {
            super(itemView);
            tvLogDate = (TextView) itemView.findViewById(R.id.tv_log_date);
            tvLogTime = (TextView) itemView.findViewById(R.id.tv_log_time);
            focusBarView = (View) itemView.findViewById(R.id.view_focusbar);
            rowItemLayoutView = (RelativeLayout) itemView.findViewById(R.id.rowItemLayoutView);

            rowItemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {

                mItemClickListener.onItemClick(v, getAdapterPosition());

                if (focusIndex != getAdapterPosition()) {
                    focusIndex = getAdapterPosition();
                } else {
                    focusIndex = -1;
                }
                notifyDataSetChanged();
            }
        }
    }

    public FlightHistory getFlightLog(int position) {
        return flightHistoryList.get(position);
    }

    public int getFocusIndex() {
        return focusIndex;
    }

    public FlightHistory getFocusHistory() {
        return flightHistoryList.get(getFocusIndex());
    }

    public synchronized void add(FlightHistory flightHistory) {

        boolean historyExisted = false;
        for (FlightHistory history : flightHistoryList) {
            if (history.getId().equals(flightHistory.getId())) {
                history.replace(flightHistory);
                historyExisted = true;
                break;
            }
        }
        if (!historyExisted) {
            flightHistoryList.add(flightHistory);
        }

        Collections.sort(flightHistoryList, new Comparator<FlightHistory>() {
            @Override
            public int compare(FlightHistory lhs, FlightHistory rhs) {
                return Long.valueOf(rhs.getCreatedTime()).compareTo(lhs.getCreatedTime());
            }
        });

        notifyDataSetChanged();
    }

    public void clearList() {
        flightHistoryList.clear();
    }
}
