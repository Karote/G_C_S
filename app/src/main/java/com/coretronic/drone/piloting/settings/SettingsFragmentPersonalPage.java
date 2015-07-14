package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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

        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_opacity, Setting.SettingType.INTERFACE_OPACITY);

        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_sdcard_enable, Setting.SettingType.SD_RECORD);
        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_flip_enable, Setting.SettingType.FLIP_ENABLE);

        ViewManager.assignSingleSelectionButton(activity, fragmentView, Setting.SettingType.FLIP_ORIENTATION,
                new int[]{
                        R.id.btn_front,
                        R.id.btn_back,
                        R.id.btn_left,
                        R.id.btn_right},
                new int[]{
                        Setting.FLIP_ORIENTATION_FRONT,
                        Setting.FLIP_ORIENTATION_BACK,
                        Setting.FLIP_ORIENTATION_LEFT,
                        Setting.FLIP_ORIENTATION_RIGHT});

        etNetworkName = (EditText) fragmentView.findViewById(R.id.et_network_name);
        etNetworkName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    activity.setParameters(Parameter.Type.NETWORK_NAME, Parameter.Text.getInstance().setValue(etNetworkName.getText().toString()));
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

