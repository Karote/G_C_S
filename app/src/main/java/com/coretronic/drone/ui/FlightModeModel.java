package com.coretronic.drone.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coretronic.drone.R;

/**
 * Created by karot.chuang on 2016/2/17.
 */
public class FlightModeModel extends LinearLayout {

    private FlightModeModelButtonClickListener mFlightModeModelButtonClickListener;
    private int mResourceId;
    private Button mModeTypeButton;

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

    public interface FlightModeModelButtonClickListener {
        void onModeTypeButtonClick(int resourceId, int[] viewLocation);
    }

    public void registerFlightModeModelButtonClickListener(int id, FlightModeModelButtonClickListener listener) {
        this.mFlightModeModelButtonClickListener = listener;
        this.mResourceId = id;
    }

    public void setViewDisable() {
        this.findViewById(R.id.mode_type_button).setEnabled(false);
        ((Button) this.findViewById(R.id.mode_type_button)).setText("");
    }
}
