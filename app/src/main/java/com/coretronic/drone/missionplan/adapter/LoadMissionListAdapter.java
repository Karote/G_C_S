package com.coretronic.drone.missionplan.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.StickyGridHeaders.StickyGridHeadersSimpleAdapter;
import com.coretronic.drone.util.MissionLists;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by karot.chuang on 2015/12/29.
 */
public class LoadMissionListAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {
    private final static String[] DAY_OF_WEEK = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private final static String[] MONTH_OF_YEAR = {"", "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};

    private List<MissionLists> mLoadMissionList;
    private LayoutInflater mInflater;
    private OnGridItemClickListener mItemClickListener;

    public LoadMissionListAdapter(Context context, List<MissionLists> loadMissionList) {
        this.mInflater = LayoutInflater.from(context);
        this.mLoadMissionList = loadMissionList;
    }

    public interface OnGridItemClickListener {
        void onItemSelected(MissionLists missionLists);
    }

    @Override
    public long getHeaderId(int position) {
        String headerText = getItemYMString(mLoadMissionList.get(position));
        return Long.parseLong(headerText.replace("/", ""));
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder mHeaderHolder;

        if (convertView == null) {
            mHeaderHolder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.grid_header_load_mission, parent, false);
            mHeaderHolder.headerText = (TextView) convertView.findViewById(R.id.grid_header_text);
            convertView.setTag(mHeaderHolder);
        } else {
            mHeaderHolder = (HeaderViewHolder) convertView.getTag();
        }

        mHeaderHolder.headerText.setText(getHeaderText(mLoadMissionList.get(position)));

        return convertView;
    }

    @Override
    public int getCount() {
        return mLoadMissionList.size();
    }

    @Override
    public Object getItem(int position) {
        return mLoadMissionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mLoadMissionList.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.grid_item_load_mission, parent, false);
            mViewHolder.datetimeText = (TextView) convertView.findViewById(R.id.datetime_text);
            mViewHolder.screenShotImage = (ImageView) convertView.findViewById(R.id.screen_shot_image);
            mViewHolder.flightDistanceText = (TextView) convertView.findViewById(R.id.flight_distance_text);
            mViewHolder.flightTimeText = (TextView) convertView.findViewById(R.id.flight_time_text);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.datetimeText.setText(getItemDateTimeString(mLoadMissionList.get(position)));
        mViewHolder.flightDistanceText.setText(getItemDistanceString(mLoadMissionList.get(position)));
        mViewHolder.flightTimeText.setText(getItemFlightTimeString(mLoadMissionList.get(position)));
        mViewHolder.screenShotImage.setImageBitmap(getItemImageBitmap(mLoadMissionList.get(position)));

        convertView.findViewById(R.id.load_mission_grid_item_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemSelected(mLoadMissionList.get(position));
            }
        });

        return convertView;
    }

    public void setOnGridItemClickListener(final OnGridItemClickListener listener) {
        mItemClickListener = listener;
    }

    public static class ViewHolder {
        public TextView datetimeText;
        public ImageView screenShotImage;
        public TextView flightDistanceText;
        public TextView flightTimeText;
    }

    public static class HeaderViewHolder {
        public TextView headerText;
    }

    private String getItemDateTimeString(MissionLists item) {
        String mText, dateText, timeText;
        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd");
        Calendar todayTime = Calendar.getInstance();
        Calendar todayDate = new GregorianCalendar(todayTime.get(Calendar.YEAR), todayTime.get(Calendar.MONTH), todayTime.get(Calendar.DAY_OF_MONTH));

        Calendar yesterdayTime = Calendar.getInstance();
        yesterdayTime.add(Calendar.DATE, -1);
        Calendar yesterdayDate = new GregorianCalendar(yesterdayTime.get(Calendar.YEAR), yesterdayTime.get(Calendar.MONTH), yesterdayTime.get(Calendar.DAY_OF_MONTH));

        Calendar itemDate = new GregorianCalendar(item.getDateYear(), item.getDateMonth() - 1, item.getDateDay());

        if (itemDate.equals(todayDate)) {
            dateText = "Today";
        } else if (itemDate.equals(yesterdayDate)) {
            dateText = "Yesterday";
        } else if (diffInDaysOfTwoDates(itemDate, todayDate) < 7) {
            dateText = DAY_OF_WEEK[itemDate.get(Calendar.DAY_OF_WEEK)];
        } else {
            dateText = sdfDate.format(itemDate.getTime());
        }

        if (item.getDateHour() < 12) {
            timeText = String.format("am%d:%02d", item.getDateHour(), item.getDateMinute());
        } else {
            timeText = String.format("pm%d:%02d", item.getDateHour() - 12, item.getDateMinute());
        }

        mText = dateText + " " + timeText;
        return mText;
    }

    private int diffInDaysOfTwoDates(Calendar c1, Calendar c2) {
        long ms1 = c1.getTimeInMillis();
        long ms2 = c2.getTimeInMillis();
        long diffs = Math.abs(ms1 - ms2);
        return (int) (diffs / (24 * 60 * 60 * 1000));
    }

    private String getItemYMString(MissionLists item) {
        String mText;
        mText = String.format("%02d/%d", item.getDateMonth(), item.getDateYear());
        return mText;
    }

    private String getHeaderText(MissionLists item) {
        String mText;
        Calendar todayTime = Calendar.getInstance();

        if (item.getDateYear() == todayTime.get(Calendar.YEAR)) {
            mText = MONTH_OF_YEAR[item.getDateMonth()];
        } else {
            mText = getItemYMString(item);
        }
        return mText;
    }

    private String getItemDistanceString(MissionLists item) {
        String mText;
        float value = item.getDistance();

        if (value > 1000) {
            mText = (value / 1000) + "km";
        } else {
            mText = value + "m";
        }

        return mText;
    }

    private String getItemFlightTimeString(MissionLists item) {
        String mText;
        long value = item.getFlightTime();
        long minutes = TimeUnit.SECONDS.toMinutes(value);
        long seconds = TimeUnit.SECONDS.toSeconds(value) - (TimeUnit.SECONDS.toMinutes(value) * 60);

        mText = String.format("%02d:%02d", minutes, seconds);

        return mText;
    }

    private Bitmap getItemImageBitmap(MissionLists item) {
        byte[] imgByte = item.getImageContent();
        return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
    }

    public void update(List<MissionLists> updateList) {
        mLoadMissionList = updateList;
        notifyDataSetChanged();
    }
}
