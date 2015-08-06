package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.HistoryItemListAdapter;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter;

import java.util.List;

/**
 * Created by karot.chuang on 2015/7/22.
 */
public class HistoryFragment extends MavInfoFragment {

    private static final String TAG = HistoryFragment.class.getSimpleName();

    private FragmentActivity fragmentActivity = null;
    private LinearLayout drone_log_info = null;
    private TextView tv_flightDistance = null;
    private static HistoryItemListAdapter mHistoryItemAdapter = null;

    private static HistoryAdapterListener mCallback = null;

    public interface HistoryAdapterListener {
        void LoadPathLog(List<Double> path);
        void ClearPath();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (HistoryAdapterListener) fragmentActivity.getSupportFragmentManager().findFragmentByTag("fragment");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission_plan_history, container, false);
        return view;
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
                if (mHistoryItemAdapter.getFocusIndex() < 0 || position != mHistoryItemAdapter.getFocusIndex()) {
                    mCallback.LoadPathLog(mHistoryItemAdapter.getFlightLog(position).getFlightPath());
                    tv_flightTime.setText(mHistoryItemAdapter.getFlightLog(position).getFlightTime());
                    drone_log_info.setVisibility(View.VISIBLE);
                } else {
                    mCallback.ClearPath();
                    drone_log_info.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setFlightDistance(int lengthInMeters) {
        tv_flightDistance.setText(String.valueOf(lengthInMeters) + " m");
    }
}
