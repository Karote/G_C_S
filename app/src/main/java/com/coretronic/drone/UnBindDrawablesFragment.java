package com.coretronic.drone;

import android.support.v4.app.Fragment;

import com.coretronic.drone.util.ViewManager;

/**
 * Created by jiaLian on 15/4/8.
 */
public class UnBindDrawablesFragment extends Fragment {
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ViewManager.unbindDrawables(getView());
    }

    public String getTitle() {
        return null;
    }
}
