package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.HistoryItemListAdapter;
import com.coretronic.drone.missionplan.adapter.HistoryItemListAdapter.onFlightHisorySelectedListener;
import com.coretronic.drone.model.FlightHistory;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.OnFlightHistoryUpdateListener;
import com.coretronic.drone.model.RecordItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/7/22.
 */
public class HistoryFragment extends MapChildFragment {
    private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("mm:ss");

    private View mDroneHistoryInfoPanel = null;
    private TextView mFlightDistanceTextView = null;
    private HistoryItemListAdapter mHistoryItemAdapter = null;
    private Button mActivatePlanButton = null;

    private MapViewFragment mMapViewFragment = null;
    private FlightHistory mFlightHistory;

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

        mDroneHistoryInfoPanel = view.findViewById(R.id.drone_log_info);
        mFlightDistanceTextView = (TextView) view.findViewById(R.id.tv_flight_distance);
        final TextView flightDurationTextView = (TextView) view.findViewById(R.id.tv_flight_time);
        mActivatePlanButton = (Button) view.findViewById(R.id.btn_activate_plan);
        mActivatePlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightHistory == null) {
                    return;
                }
                mMapViewFragment.activateWithExistedMission(mFlightHistory.getMissions());
            }
        });

        mHistoryItemAdapter = new HistoryItemListAdapter();
        mHistoryItemAdapter.setOnFlightHisorySelectedListener(new onFlightHisorySelectedListener() {

            @Override
            public void onFlightHistorySelected(FlightHistory flightHistory) {
                List<Float> missionLocations = new ArrayList<>();
                List<Long> flightRecords = new ArrayList<>();
                for (Mission mission : flightHistory.getMissions()) {
                    missionLocations.add(mission.getLatitude());
                    missionLocations.add(mission.getLongitude());
                }

                for (RecordItem recordItem : flightHistory.getRecordItems()) {
                    flightRecords.add(recordItem.getLatitude());
                    flightRecords.add(recordItem.getLongitude());
                }
                mMapViewFragment.loadHistory(missionLocations, flightRecords);
                flightDurationTextView.setText(TIME_FORMAT.format(flightHistory.getDuration()));
                mDroneHistoryInfoPanel.setVisibility(View.VISIBLE);
                mActivatePlanButton.setVisibility(View.VISIBLE);
                mFlightHistory = flightHistory;
            }

            @Override
            public void onNothingSelected() {
                mMapViewFragment.clearHistoryMarkerPath();
                mDroneHistoryInfoPanel.setVisibility(View.GONE);
                mActivatePlanButton.setVisibility(View.GONE);
                mFlightHistory = null;
            }

        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.flight_history_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new FixedLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mHistoryItemAdapter);

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
            mHistoryItemAdapter.add(flightHistory);
        }
    };

    @Override
    public void onMapPolylineLengthCalculated(int lengthInMeters) {
        mFlightDistanceTextView.setText(lengthInMeters + " m");
    }

}
