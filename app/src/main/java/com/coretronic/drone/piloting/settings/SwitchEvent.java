package com.coretronic.drone.piloting.settings;

/**
 * Created by Morris on 15/8/26.
 */
public class SwitchEvent {
    private int mId;
    private boolean mIsChecked;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }
}
