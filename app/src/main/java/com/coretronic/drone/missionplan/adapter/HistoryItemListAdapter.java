package com.coretronic.drone.missionplan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.Mission;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.fragments.module.DroneInfo;
import com.coretronic.drone.missionplan.model.FlightLogItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by karot.chuang on 2015/8/4.
 */
public class HistoryItemListAdapter extends RecyclerView.Adapter<HistoryItemListAdapter.HistoryItemListViewHolder> {
    private static final String TAG = HistoryItemListAdapter.class.getSimpleName();

    private static Context context;
    private List<FlightLogItem> mFlightLogItems = null;

    private int focusIndex = -1;
    private String filePath;
    private File files[] = null;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public HistoryItemListAdapter(Context context) {
        this.context = context;
        this.mFlightLogItems = new ArrayList<FlightLogItem>();

        filePath = this.context.getExternalFilesDir(null).getAbsolutePath();
        File f = new File(filePath);
        files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            String filename = files[i].getName();
            this.mFlightLogItems.add(readFile(filename));
        }
    }

    private FlightLogItem readFile(String filename) {
        File file = new File(filePath + "/" + filename);
        Gson gson = new Gson();
        List<Mission> missionList = null;
        List<DroneInfo> pathList = new ArrayList<DroneInfo>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String bufferStr = bufferedReader.readLine();
            if (bufferStr != null) {
                missionList = gson.fromJson(bufferStr, new TypeToken<List<Mission>>() {}.getType());

                bufferStr = bufferedReader.readLine();
                while (bufferStr != null) {
                    DroneInfo droneInfoObj = gson.fromJson(bufferStr, DroneInfo.class);
                    pathList.add(droneInfoObj);
                    bufferStr = bufferedReader.readLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new FlightLogItem(missionList, pathList);
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
        String filename = files[i].getName();
        Date resultdate = new Date(Long.parseLong(filename));
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
        return mFlightLogItems.size();
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

    public FlightLogItem getFlightLog(int position) {
        return mFlightLogItems.get(position);
    }

    public int getFocusIndex() {
        return focusIndex;
    }

    public String getFilePath(int position) {
        return filePath + "/" + files[position].getName();
    }
}
