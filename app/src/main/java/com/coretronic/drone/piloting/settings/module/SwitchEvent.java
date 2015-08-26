package com.coretronic.drone.piloting.settings.module;

/**
 * Created by Morris on 15/8/26.
 */
public class SwitchEvent {
    int id;
    boolean isChecked;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
