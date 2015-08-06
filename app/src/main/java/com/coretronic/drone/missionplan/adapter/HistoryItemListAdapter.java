package com.coretronic.drone.missionplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.model.FlightLogItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/8/4.
 */
public class HistoryItemListAdapter extends RecyclerView.Adapter<HistoryItemListAdapter.HistoryItemListViewHolder> {
    private static final String TAG = HistoryItemListAdapter.class.getSimpleName();

    private List<FlightLogItem> mLogList = new ArrayList<FlightLogItem>();
    private int focusIndex = -1;
    private String[] FILENAME = {"2015/08/03", "2015/08/04", "2015/08/05"};
    private String[] FILETIME = {"09:08", "11:28", "10:40"};
    private String[] FLIGHTDURATION = {"01:48", "00:54", "03:21"};
    private Double[] FLIGHTLAT = {24.713700, 24.713724, 24.709226, 24.709221,
            24.712004, 24.711405, 24.709967,
            24.712038, 24.710337, 24.711911, 24.710288, 24.712125};
    private Double[] FLIGHTLNG = {120.908586, 120.914433, 120.914422, 120.909079,
            120.916278, 120.913006, 120.916032,
            120.909938, 120.910668, 120.912256, 120.913619, 120.915142};
    private int[] pointCount = {4, 7, 12};

    public HistoryItemListAdapter() {
        this.mLogList = new ArrayList<FlightLogItem>();

        int j = 0;
        int max;
        for (int i = 0; i < 3; i++) {
            max = pointCount[i];
            List<Double> path = new ArrayList<Double>();
            Double lat, lng;
            for (; j < max; j++) {
                lat = FLIGHTLAT[j];
                lng = FLIGHTLNG[j];
                path.add(lat);
                path.add(lng);
            }
            FlightLogItem item = new FlightLogItem(FILENAME[i], FILETIME[i], path, FLIGHTDURATION[i]);
            this.mLogList.add(i, item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static OnItemClickListener mItemClickListener = null;

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
        viewHolder.tvLogDate.setText(mLogList.get(i).getFlightFileName());
        viewHolder.tvLogTime.setText(mLogList.get(i).getFlightDate());

        if (i == focusIndex) {
            viewHolder.focusBarView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.focusBarView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mLogList.size();
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
                Log.d(TAG, "focusIndex:" + focusIndex);
                notifyDataSetChanged();
            }
        }
    }

    public FlightLogItem getFlightLog(int position) {
        return mLogList.get(position);
    }

    public int getFocusIndex() {
        return focusIndex;
    }
}
