package com.coretronic.drone.utility;

import android.content.Context;
import android.view.View;

/**
 * Created by james on 15/6/5.
 */
public class AppUtils {

    public static CustomerTwoBtnAlertDialog getAlertDialog(final Context mContext, String msg, String okStr, String cancelStr, View.OnClickListener listener) {
        CustomerTwoBtnAlertDialog dialog = new CustomerTwoBtnAlertDialog(mContext);
        dialog.setMsg(msg)
                .setPositiveBtnText(okStr)
                .setNegativeBtnText(cancelStr)
                .setPositiveListener(listener);
        return dialog;
    }

}
