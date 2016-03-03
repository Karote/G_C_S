package com.coretronic.drone.missionplan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2016/3/2.
 */
public class NotificationCenterListAdapter extends BaseAdapter {
    public final static int NOTIFICATION_TYPE_GPS = 1;
    public final static int NOTIFICATION_TYPE_DRONE_BATTERY = 2;
    public final static int NOTIFICATION_TYPE_REMOTE_CONTROL = 3;
    public final static int NOTIFICATION_TYPE_DRONE = 4;
    public final static int NOTIFICATION_TYPE_FAILSAFE = 5;

    private LayoutInflater mInflater;
    private List<Notification> mNotificationList;
    private NotificationCenterListChangedListener mListChangedListener;

    public NotificationCenterListAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
        this.mNotificationList = new ArrayList<>();
    }

    public interface NotificationCenterListChangedListener {
        void onNotificationCenterListGetOne();

        void onNotificationCenterListEmpty();
    }

    public void setNotificationCenterListChangedListener(NotificationCenterListChangedListener listChangedListener) {
        mListChangedListener = listChangedListener;
    }

    @Override
    public int getCount() {
        return mNotificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return mNotificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mNotificationList.get(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.notification_center_list_item, parent, false);
            mViewHolder.iconImage = (ImageView) convertView.findViewById(R.id.notification_icon);
            mViewHolder.contentText = (TextView) convertView.findViewById(R.id.notification_content_text);
            mViewHolder.timeText = (TextView) convertView.findViewById(R.id.notification_time_text);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.iconImage.setImageResource(getIconImageResource(mNotificationList.get(position).getType()));
        mViewHolder.contentText.setText(mNotificationList.get(position).getContent());
        mViewHolder.timeText.setText(getItemTimeString(mNotificationList.get(position).getTime()));

        return convertView;
    }

    private class ViewHolder {
        ImageView iconImage;
        TextView contentText;
        TextView timeText;
    }

    private int getIconImageResource(int type) {
        int resourceId = 0;
        switch (type) {
            case NOTIFICATION_TYPE_GPS:
                resourceId = R.drawable.icon_noti_gps;
                break;
            case NOTIFICATION_TYPE_DRONE_BATTERY:
                resourceId = R.drawable.icon_noti_battery;
                break;
            case NOTIFICATION_TYPE_REMOTE_CONTROL:
                resourceId = R.drawable.icon_noti_rc;
                break;
            case NOTIFICATION_TYPE_DRONE:
                resourceId = R.drawable.icon_noti_drone;
                break;
            case NOTIFICATION_TYPE_FAILSAFE:
                resourceId = R.drawable.icon_noti_failsafe;
                break;
        }
        return resourceId;
    }

    private String getItemTimeString(long time) {
        String timeText;
        Long timePassed = System.currentTimeMillis() - time;
        Long minutes = (timePassed / 1000) / 60;

        if (minutes == 0) {
            timeText = "now";
        } else {
            timeText = String.format("%d minutes ago", minutes);
        }

        return timeText;
    }

    public void updateList(Notification updateItem) {
        int itemType = updateItem.getType();
        int itemIndex = getIndexByType(itemType);

        if (mNotificationList.isEmpty()) {
            mListChangedListener.onNotificationCenterListGetOne();
        }

        if (itemIndex >= 0) {
            mNotificationList.remove(itemIndex);
        }
        mNotificationList.add(0, updateItem);
        notifyDataSetChanged();
    }

    public void removeItem(int type) {
        int itemIndex = getIndexByType(type);
        if (itemIndex >= 0) {
            mNotificationList.remove(itemIndex);
        }

        if (mNotificationList.isEmpty()) {
            mListChangedListener.onNotificationCenterListEmpty();
        }
        notifyDataSetChanged();
    }

    private int getIndexByType(int type) {
        for (Notification item : mNotificationList) {
            if (type == item.getType()) {
                return mNotificationList.indexOf(item);
            }
        }
        return -1;
    }
}
