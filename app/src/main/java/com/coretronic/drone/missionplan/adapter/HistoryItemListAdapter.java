package com.coretronic.drone.missionplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM.dd.yyyy");
    private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private List<FlightHistory> mFlightHistoryList = null;
    private int mFocusIndex = -1;

    public HistoryItemListAdapter() {
        this.mFlightHistoryList = new ArrayList<>();
    }

    public interface onFlightHisorySelectedListener {

        void onFlightHistorySelected(FlightHistory flightHistory);

        void onNothingSelected();
    }

    private onFlightHisorySelectedListener mItemClickListener = null;

    public void setOnFlightHisorySelectedListener(final onFlightHisorySelectedListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public HistoryItemListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new HistoryItemListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_log_listitem, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(HistoryItemListViewHolder viewHolder, int i) {
        Date flightHistoryCreatedTime = new Date(mFlightHistoryList.get(i).getCreatedTime());
        viewHolder.logDateTextView.setText(DATE_FORMAT.format(flightHistoryCreatedTime));
        viewHolder.logTimeTextView.setText(TIME_FORMAT.format(flightHistoryCreatedTime));

        if (i == mFocusIndex) {
            viewHolder.focusBar.setVisibility(View.VISIBLE);
        } else {
            viewHolder.focusBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mFlightHistoryList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class HistoryItemListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView logDateTextView;
        final TextView logTimeTextView;
        final View focusBar;

        HistoryItemListViewHolder(View itemView) {
            super(itemView);
            logDateTextView = (TextView) itemView.findViewById(R.id.tv_log_date);
            logTimeTextView = (TextView) itemView.findViewById(R.id.tv_log_time);
            focusBar = itemView.findViewById(R.id.view_focusbar);
            itemView.findViewById(R.id.rowItemLayoutView).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {

                if (mFocusIndex != getAdapterPosition()) {
                    mFocusIndex = getAdapterPosition();
                    mItemClickListener.onFlightHistorySelected(mFlightHistoryList.get(mFocusIndex));
                    focusBar.setVisibility(View.VISIBLE);
                } else {
                    mFocusIndex = -1;
                    mItemClickListener.onNothingSelected();
                    focusBar.setVisibility(View.GONE);
                }
                
                notifyDataSetChanged();
            }
        }
    }

    public synchronized void add(FlightHistory flightHistory) {

        boolean historyExisted = false;
        for (FlightHistory history : mFlightHistoryList) {
            if (history.getId().equals(flightHistory.getId())) {
                history.replace(flightHistory);
                historyExisted = true;
                break;
            }
        }
        if (!historyExisted) {
            mFlightHistoryList.add(flightHistory);
        }

        Collections.sort(mFlightHistoryList, new Comparator<FlightHistory>() {
            @Override
            public int compare(FlightHistory lhs, FlightHistory rhs) {
                return Long.valueOf(rhs.getCreatedTime()).compareTo(lhs.getCreatedTime());
            }
        });

        notifyDataSetChanged();
    }

    public void clearList() {
        mFlightHistoryList.clear();
    }
}
