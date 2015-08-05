package com.coretronic.drone.missionplan.fragments;

/**
 * Created by Morris on 15/8/5.
 */
public class CustomerEvent {
    private String msg;

    public CustomerEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
