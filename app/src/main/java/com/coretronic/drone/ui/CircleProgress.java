package com.coretronic.drone.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coretronic.drone.R;

/**
 * Created by karot.chuang on 2015/9/22.
 */
public class CircleProgress extends RelativeLayout {
    SeekArc mCircleProgressBar;
    TextView mTitleTextView;
    TextView mUnitTextView;
    TextView mContentTextView;

    public CircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.circle_progress, this);

        mCircleProgressBar = (SeekArc) this.findViewById(R.id.circle_progress_bar);
        mTitleTextView = (TextView) this.findViewById(R.id.circle_progress_title);
        mUnitTextView = (TextView) this.findViewById(R.id.circle_progress_unit);
        mContentTextView = (TextView) this.findViewById(R.id.circle_progress_content);

        initFromAttributes(context, attrs);
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.CircleProgress_cp_title:
                    this.mTitleTextView.setText(a.getString(attr));
                    break;
                case R.styleable.CircleProgress_cp_unit:
                    this.mUnitTextView.setText(a.getString(attr));
                    break;
            }
        }
        a.recycle();
    }

    public void setCircleProgressBar(int value) {
        mCircleProgressBar.setProgress(value);
    }

    public void setContentTextView(String content) {
        mContentTextView.setText(content);
    }
}
