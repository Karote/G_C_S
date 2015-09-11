package com.coretronic.drone.ui;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.ibs.log.Logger;

/**
 * Created by jiaLian on 15/6/9.
 */
public class StatusView extends LinearLayout {

    private final static int GPS_LEVEL_0_SATELLITE_COUNT = 0;
    private final static int GPS_LEVEL_1_SATELLITE_COUNT = 4;
    private final static int GPS_LEVEL_2_SATELLITE_COUNT = 6;
    private final static int GPS_LEVEL_3_SATELLITE_COUNT = 8;
    private final static int GPS_LEVEL_4_SATELLITE_COUNT = 10;

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
        initView();
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

    public void setRFStatus(int rssi) {
        rfStatus.setImageLevel(WifiManager.calculateSignalLevel(rssi, 5));
    }

    public void setGpsStatus(int satellites) {
        Logger.debug(satellites);
        gpsStatus.setImageLevel(calculateGpsLevel(satellites));
        if (calculateGpsLevel(satellites) > 0) {
            gpsNumber.setText(satellites + "");
            gpsNumber.setVisibility(View.VISIBLE);
        } else {
            gpsNumber.setVisibility(View.GONE);
        }
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

    private int calculateGpsLevel(int satellites) {

        if (satellites < GPS_LEVEL_1_SATELLITE_COUNT) {
            return 0;
        }

        if (satellites < GPS_LEVEL_2_SATELLITE_COUNT) {
            return 1;
        }
        if (satellites < GPS_LEVEL_3_SATELLITE_COUNT) {
            return 2;
        }
        if (satellites < GPS_LEVEL_4_SATELLITE_COUNT) {
            return 3;
        }
        return 4;
    }
}
