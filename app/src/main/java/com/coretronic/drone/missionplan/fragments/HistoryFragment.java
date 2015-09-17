package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.HistoryItemListAdapter;
import com.coretronic.drone.model.FlightHistory;
import com.coretronic.drone.model.OnFlightHistoryUpdateListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/7/22.
 */
public class HistoryFragment extends MavInfoFragment {
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

    private LinearLayout drone_log_info = null;
    private TextView tv_flightDistance = null;
    private HistoryItemListAdapter mHistoryItemAdapter = null;
    private Button btn_activate_plan = null;

    private MapViewFragment mMapViewFragment = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapViewFragment = (MapViewFragment) getParentFragment();
        ((MainActivity) getActivity()).registerOnFlightHistoryUpdateListener(onFlightHistoryUpdateListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_history, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drone_log_info = (LinearLayout) view.findViewById(R.id.drone_log_info);
        drone_log_info.setVisibility(View.GONE);

        tv_flightDistance = (TextView) view.findViewById(R.id.tv_flight_distance);
        tv_flightDistance.setText("0 m");

        final TextView tv_flightTime = (TextView) view.findViewById(R.id.tv_flight_time);
        tv_flightTime.setText("00:00");

        btn_activate_plan = (Button) view.findViewById(R.id.btn_activate_plan);
        btn_activate_plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapViewFragment.activateWithMission(mHistoryItemAdapter.getFocusHistory().getMissions());
            }
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.flight_history_recycler_view);
        recyclerView.setHasFixedSize(true);
        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(recyclerLayoutMgr);

        mHistoryItemAdapter = new HistoryItemListAdapter();
        recyclerView.setAdapter(mHistoryItemAdapter);

        mHistoryItemAdapter.SetOnItemClickListener(new HistoryItemListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                try {
                    if (mHistoryItemAdapter.getFocusIndex() < 0 || position != mHistoryItemAdapter.getFocusIndex()) {
                        List<Float> markerList = new ArrayList<Float>();
                        List<Long> flightPath = new ArrayList<Long>();
                        int j = mHistoryItemAdapter.getFlightLog(position).getMissions().size();
                        for (int i = 0; i < j; i++) {
                            markerList.add(mHistoryItemAdapter.getFlightLog(position).getMissions().get(i).getLatitude());
                            markerList.add(mHistoryItemAdapter.getFlightLog(position).getMissions().get(i).getLongitude());
                        }

                        j = mHistoryItemAdapter.getFlightLog(position).getRecordItems().size();
                        for (int i = 0; i < j; i++) {
                            flightPath.add(mHistoryItemAdapter.getFlightLog(position).getRecordItems().get(i).getLatitude());
                            flightPath.add(mHistoryItemAdapter.getFlightLog(position).getRecordItems().get(i).getLongitude());
                        }
                        mMapViewFragment.loadHistory(markerList, flightPath);

                        long durationTime = 0;
                        if (j > 2) {
                            durationTime = mHistoryItemAdapter.getFlightLog(position).getRecordItems().get(j - 1).getCurrentTimeStamp() -
                                    mHistoryItemAdapter.getFlightLog(position).getRecordItems().get(0).getCurrentTimeStamp();
                        }

                        tv_flightTime.setText(timeFormat.format(durationTime));
                        drone_log_info.setVisibility(View.VISIBLE);
                        btn_activate_plan.setVisibility(View.VISIBLE);
                    } else {
                        mMapViewFragment.clearHistoryMarkerPath();
                        drone_log_info.setVisibility(View.GONE);
                        btn_activate_plan.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "History content was wrong", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        view.findViewById(R.id.map_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapViewFragment.changeMapType();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).unregisterOnFlightHistoryUpdateListener(onFlightHistoryUpdateListener);
    }

    private OnFlightHistoryUpdateListener onFlightHistoryUpdateListener = new OnFlightHistoryUpdateListener() {
        @Override
        public void OnFlightHistoryUpdated(final FlightHistory flightHistory) {
            if (flightHistory != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHistoryItemAdapter.add(flightHistory);
                    }
                });
            }
        }
    };

    @Override
    public void onMapPolylineLengthCalculated(int lengthInMeters) {
        tv_flightDistance.setText(String.valueOf(lengthInMeters) + " m");
    }

}
