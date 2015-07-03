package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coretronic.drone.DroneApplication;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.service.Parameter;
import com.coretronic.drone.ui.SeekBarTextView;
import com.coretronic.drone.ui.ViewManager;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingsFragmentPersonalPage extends UnBindDrawablesFragment implements DroneController.ParameterLoaderListener {
    private static final String TAG = SettingsFragmentPersonalPage.class.getSimpleName();
    private int currentFlipOrientationValue;
    private MainActivity activity;
    private EditText etNetworkName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_personal_page, container, false);
        SeekBarTextView.assignSettingSeekBarTextView(fragmentView, R.id.setting_bar_opacity, Setting.SettingType.INTERFACE_OPACTITY);
        ViewManager.assignSwitchView(fragmentView, R.id.switch_sdcard_enable, Setting.SettingType.SD_RECORD);
        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_flip_enable, Setting.SettingType.FLIP_ENABLE);

        final Button[] buttons = new Button[4];
        buttons[Setting.FLIP_ORIENTATION_FRONT] = (Button) fragmentView.findViewById(R.id.btn_front);
        buttons[Setting.FLIP_ORIENTATION_FRONT].setTag(Setting.FLIP_ORIENTATION_FRONT);

        buttons[Setting.FLIP_ORIENTATION_BACK] = (Button) fragmentView.findViewById(R.id.btn_back);
        buttons[Setting.FLIP_ORIENTATION_BACK].setTag(Setting.FLIP_ORIENTATION_BACK);

        buttons[Setting.FLIP_ORIENTATION_LEFT] = (Button) fragmentView.findViewById(R.id.btn_left);
        buttons[Setting.FLIP_ORIENTATION_LEFT].setTag(Setting.FLIP_ORIENTATION_LEFT);

        buttons[Setting.FLIP_ORIENTATION_RIGHT] = (Button) fragmentView.findViewById(R.id.btn_right);
        buttons[Setting.FLIP_ORIENTATION_RIGHT].setTag(Setting.FLIP_ORIENTATION_RIGHT);

        currentFlipOrientationValue = DroneApplication.settings[Setting.SettingType.FLIP_ORIENTATION.ordinal()].getValue();
        for (Button btn : buttons) {
            refreshBackground(currentFlipOrientationValue, btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentFlipOrientationValue = (int) view.getTag();
                    DroneApplication.settings[Setting.SettingType.FLIP_ORIENTATION.ordinal()].setValue(currentFlipOrientationValue);
                    for (Button btn : buttons) {
                        refreshBackground(currentFlipOrientationValue, btn);
                    }
                    activity.setParameters(DroneApplication.settings[Setting.SettingType.FLIP_ORIENTATION.ordinal()].getParameterType(), DroneApplication.settings[Setting.SettingType.FLIP_ORIENTATION.ordinal()].getParameter());

                }
            });
        }
        etNetworkName = (EditText) fragmentView.findViewById(R.id.et_network_name);
        etNetworkName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE /*||
                        event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER*/) {
                    activity.setParameters(Parameter.Type.NETWORK_NAME, Parameter.Text.getInstance().setValue(etNetworkName.getText().toString()));
                    Log.d(TAG, "onEditorAction: " + etNetworkName.getText().toString());
                }
                return false;
            }
        });
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.readParameters(this, Parameter.Type.NETWORK_NAME);
    }

    private void refreshBackground(int currentValue, Button btn) {
        if ((int) btn.getTag() == currentValue) {
            btn.setBackgroundColor(getResources().getColor(R.color.blue_sky));
        } else {
            btn.setBackgroundResource(R.drawable.btn_bg);
        }
    }

    @Override
    public void onParameterLoaded(Parameter.Type type, final Parameter parameter) {
        if (type == Parameter.Type.NETWORK_NAME) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etNetworkName.setText(parameter.getValue() + "");
                }
            });
        }
    }
}

