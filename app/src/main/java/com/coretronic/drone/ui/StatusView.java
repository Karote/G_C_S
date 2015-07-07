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
    private ImageView wifiStatus;
    private ImageView gpsStatus;
    private ProgressBar batteryProgress;
    private TextView tvBattery;

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
        wifiStatus = (ImageView) view.findViewById(R.id.iv_wifi);
        gpsStatus = (ImageView) view.findViewById(R.id.iv_gps);
        batteryProgress = (ProgressBar) view.findViewById(R.id.progress_battery);
        tvBattery = (TextView) view.findViewById(R.id.tv_battery);
        addView(view);
    }

    public void setWifiStatus(int resid) {
        wifiStatus.setBackgroundResource(resid);
    }

    public void setGpsVisibility(int visibility) {
        gpsStatus.setVisibility(visibility);
    }

    private void setMaxProgress(int maxProgress) {
        batteryProgress.setMax(maxProgress);
    }

    public void setBatteryStatus(final int progress) {
        batteryProgress.post(new Runnable() {
            @Override
            public void run() {
                batteryProgress.setProgress(progress);
            }
        });
        tvBattery.setText(progress + "%");
    }
}
