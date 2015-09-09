package com.coretronic.drone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coretronic.drone.R;

/**
 * Created by jiaLian on 15/6/9.
 */
public class StatusView extends LinearLayout {
    private static final int[] GPS_RESOURCE_ID = {
            R.drawable.icon_indicator_status_gps_00,
            R.drawable.icon_indicator_status_gps_01,
            R.drawable.icon_indicator_status_gps_02,
            R.drawable.icon_indicator_status_gps_03,
            R.drawable.icon_indicator_status_gps_04,
            R.drawable.icon_indicator_status_gps_05
    };
    private static final int[] RF_RESOURCE_ID = {
            R.drawable.icon_indicator_status_rf_00,
            R.drawable.icon_indicator_status_rf_01,
            R.drawable.icon_indicator_status_rf_02,
            R.drawable.icon_indicator_status_rf_03,
            R.drawable.icon_indicator_status_rf_04,
            R.drawable.icon_indicator_status_rf_05
    };
    private ImageView gpsStatus;
    private ImageView rfStatus;
    private ProgressBar batteryProgress;
    private TextView tvBattery;
    private TextView gpsNumber;

    public StatusView(Context context) {
        super(context);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.status_view, null);
        gpsStatus = (ImageView) view.findViewById(R.id.iv_gps);
        gpsNumber = (TextView) view.findViewById(R.id.tv_gps);
        rfStatus = (ImageView) view.findViewById(R.id.iv_rf);
        batteryProgress = (ProgressBar) view.findViewById(R.id.progress_battery);
        tvBattery = (TextView) view.findViewById(R.id.tv_battery);
        addView(view);
    }

    public void setRFStatus(int resid) {
        rfStatus.setBackgroundResource(RF_RESOURCE_ID[resid]);
    }

    public void setGpsStatus(int resid){
        gpsStatus.setBackgroundResource(GPS_RESOURCE_ID[resid]);
        gpsNumber.setText(String.valueOf(resid));
    }

    public void setGpsVisibility(int visibility) {
        gpsStatus.setVisibility(visibility);
    }

    private void setMaxProgress(int maxProgress) {
        batteryProgress.setMax(maxProgress);
    }

    public void setBatteryStatus(final int progress) {
        tvBattery.setText(progress + "%");
        batteryProgress.post(new Runnable() {
            @Override
            public void run() {
                batteryProgress.setProgress(progress);
            }
        });
    }
}
