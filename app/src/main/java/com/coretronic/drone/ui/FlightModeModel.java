package com.coretronic.drone.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.coretronic.drone.R;

/**
 * Created by karot.chuang on 2016/2/17.
 */
public class FlightModeModel extends LinearLayout {
    public final static int FLIGHT_MODE_LOCK_TYPE_NONE = 0;
    public final static int FLIGHT_MODE_LOCK_TYPE_HEADING = 1;
    public final static int FLIGHT_MODE_LOCK_TYPE_HOME = 2;

    private FlightModeModelButtonClickListener mFlightModeModelButtonClickListener;
    private int mResourceId;
    private Button mModeTypeButton;
    private RadioGroup mRadioGroup;
    private int mFlightModeLockType = FLIGHT_MODE_LOCK_TYPE_NONE;

    public FlightModeModel(Context context) {
        super(context);
        init(context, null, 0);
    }

    public FlightModeModel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public FlightModeModel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.flight_mode_model, this);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlightModeModel, defStyle, 0);

            ((TextView) this.findViewById(R.id.label_text)).setText(a.getString(R.styleable.FlightModeModel_label));
            ((TextView) this.findViewById(R.id.range_text)).setText(a.getString(R.styleable.FlightModeModel_range));

            a.recycle();
        }
        mModeTypeButton = (Button) this.findViewById(R.id.mode_type_button);
        mModeTypeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewLocation[] = new int[2];
                v.getLocationOnScreen(viewLocation);
                mFlightModeModelButtonClickListener.onModeTypeButtonClick(mResourceId, viewLocation);
            }
        });


        mRadioGroup = (RadioGroup) this.findViewById(R.id.flight_mode_radio_group);
        this.findViewById(R.id.heading_lock_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightModeLockType == FLIGHT_MODE_LOCK_TYPE_HEADING) {
                    mRadioGroup.clearCheck();
                    mFlightModeLockType = FLIGHT_MODE_LOCK_TYPE_NONE;
                } else {
                    mFlightModeLockType = FLIGHT_MODE_LOCK_TYPE_HEADING;
                }
                mFlightModeModelButtonClickListener.onFlightModeLockTypeCheck(mResourceId, mFlightModeLockType);
            }
        });

        this.findViewById(R.id.home_lock_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightModeLockType == FLIGHT_MODE_LOCK_TYPE_HOME) {
                    mRadioGroup.clearCheck();
                    mFlightModeLockType = FLIGHT_MODE_LOCK_TYPE_NONE;
                } else {
                    mFlightModeLockType = FLIGHT_MODE_LOCK_TYPE_HOME;
                }
                mFlightModeModelButtonClickListener.onFlightModeLockTypeCheck(mResourceId, mFlightModeLockType);
            }
        });
    }

    public void setModeTypeButtonText(String typeString) {
        mModeTypeButton.setText(typeString);
    }

    public void setModeTypeButtonOnFocus(boolean isFocus) {
        if (isFocus) {
            mModeTypeButton.setBackgroundResource(R.color.primary_color_normal);
        } else {
            mModeTypeButton.setBackgroundResource(R.drawable.settings_button_bg);
        }
    }

    public void setCheckStatus(int checkStatus) {
        switch (checkStatus) {
            case FLIGHT_MODE_LOCK_TYPE_NONE:
                mRadioGroup.clearCheck();
                break;
            case FLIGHT_MODE_LOCK_TYPE_HEADING:
                mRadioGroup.check(R.id.heading_lock_button);
                break;
            case FLIGHT_MODE_LOCK_TYPE_HOME:
                mRadioGroup.check(R.id.home_lock_button);
                break;
        }
    }

    public interface FlightModeModelButtonClickListener {
        void onModeTypeButtonClick(int resourceId, int[] viewLocation);

        void onFlightModeLockTypeCheck(int resourceId, int type);
    }

    public void registerFlightModeModelButtonClickListener(int id, FlightModeModelButtonClickListener listener) {
        this.mFlightModeModelButtonClickListener = listener;
        this.mResourceId = id;
    }
}
