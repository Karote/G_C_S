package com.coretronic.drone.uvc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Poming on 2015/10/26.
 */
public class Size implements Parcelable {
    private int mType;
    private int mIndex;
    private int mWidth;
    private int mHeight;

    public Size(int type, int index, int width, int height) {
        mType = type;
        mIndex = index;
        mWidth = width;
        mHeight = height;
    }

    private Size(final Parcel source) {
        mType = source.readInt();
        mIndex = source.readInt();
        mWidth = source.readInt();
        mHeight = source.readInt();
    }

    public double getRatio() {
        return mWidth * 1.0 / mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mType);
        dest.writeInt(mIndex);
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
    }

    @Override
    public String toString() {
        return "Size{" +
                "mType=" + mType +
                ", mIndex=" + mIndex +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                '}';
    }

    public static final Creator<Size> CREATOR = new Parcelable.Creator<Size>() {
        @Override
        public Size createFromParcel(final Parcel source) {
            return new Size(source);
        }

        @Override
        public Size[] newArray(final int size) {
            return new Size[size];
        }
    };
}
