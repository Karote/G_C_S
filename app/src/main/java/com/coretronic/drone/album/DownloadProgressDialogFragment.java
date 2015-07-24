package com.coretronic.drone.album;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.utility.AppConfig;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by karot.chuang on 2015/7/9.
 */
public class DownloadProgressDialogFragment extends DialogFragment {
    private static final String TAG = DownloadProgressDialogFragment.class.getSimpleName();

    private static final String ARGUMENT_FILENAME = "filename";
    private static final String ARGUMENT_FILESIZE = "filesize";
    private static final int INTERVAL_DOWNLOAD_TIME = 1000;
    private static final boolean IS_DOWNLOAD_FINISHED_DELETE_FILE = false;

    private Context context;

    private LinearLayout llWarning;
    private TextView tvTime;
    private ProgressBar progressBar;

    private Thread progressThread;

    private Timer speedTimer;

    private float intervalCalculateSum = 0;
    private long totalFileSize = 0;
    private long getFileSize = 0;
    private DroneController droneController;
    private String albumFilePath = "";
    private String fileName = "";

    public static DownloadProgressDialogFragment newInstance(MediaListItem mediaListItem) {
        DownloadProgressDialogFragment dialogFragment = new DownloadProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_FILENAME, mediaListItem.getMediaFileName());
        bundle.putString(ARGUMENT_FILESIZE, mediaListItem.getMediaSize());
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        albumFilePath = AppConfig.getMediaFolderPosition();

        Dialog dialog = new Dialog(getActivity());
        context = dialog.getContext();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.fragment_album_warning);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        llWarning = (LinearLayout) dialog.findViewById(R.id.wraning_ll);
        tvTime = (TextView) dialog.findViewById(R.id.timetv);

        TextView btnStop = (TextView) dialog.findViewById(R.id.stopdownload_btn);

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDownloadFragment();
            }
        });

        progressBar = (ProgressBar) dialog.findViewById(R.id.downloadbar);
        dialog.show();
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            Log.i(TAG, "bundle!=null");
            fileName = bundle.getString(ARGUMENT_FILENAME);
            String fileSize = bundle.getString(ARGUMENT_FILESIZE);

            Log.i(TAG, "fileName:" + fileName);
            Log.i(TAG, "fileSize:" + fileSize);

            int fileSizeNumber = Integer.valueOf((fileSize.split(" bytes"))[0]);

            // if > 50MB show warning
            if (fileSizeNumber > 50 * 1000 * 1000) {
                // set if warning
                llWarning.setVisibility(View.VISIBLE);
            } else {
                llWarning.setVisibility(View.GONE);
            }
        }

        progressThread = new Thread() {
            @Override
            public void run() {
                connectToAMBA();
            }
        };
        progressThread.start();

        speedTimer = new Timer();
        speedTimer.schedule(new SpeedTimerTask(), 500, INTERVAL_DOWNLOAD_TIME);
    }

    private void closeDownloadFragment() {
        if (progressThread != null && progressThread.isAlive()) {
            progressThread.interrupt();
            progressThread = null;
        }
        speedTimer.cancel();
        dismiss();
    }

    private void deleteAMMBAFile() {
        droneController.removeContent(fileName, new DroneController.MediaCommandListener<Void>() {
            @Override
            public void onCommandResult(MediaCommand mediaCommand, boolean isSuccess, Void data) {
                Log.i(TAG, "delete file completed");
                closeDownloadFragment();
            }
        });
    }

    private class SpeedTimerTask extends java.util.TimerTask {

        @Override
        public void run() {
            Log.i(TAG, "getFileSize:" + getFileSize + "/totalFileSize:" + totalFileSize);

            intervalCalculateSum++;
            if (getFileSize != 0 && totalFileSize != 0) {

                final float wasteTime = ((float) (totalFileSize - getFileSize) / ((float) getFileSize / (INTERVAL_DOWNLOAD_TIME * intervalCalculateSum)));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTime.setText(millisecondsToHumanRead(wasteTime));
                    }
                });
            }
        }

    }

    private String millisecondsToHumanRead(float mills) {
        int seconds = (int) (mills / 1000) % 60;
//        int minutes = (int) ((mills / (1000 * 60)) % 60);
        int minutes = (int) (mills / (1000 * 60));
//        int hours = (int) ((mills / (1000 * 60 * 60)) % 24);
//        Log.i(TAG, "mills:" + mills + " /seconds:" + seconds + " /minutes:" + minutes + " /hours:" + hours);
        String returnValue = "--:--";
        if (seconds >= 0 | minutes >= 0) {
            returnValue = String.format("%02d:%02d", minutes, seconds);
        }
        if (seconds == 0 && minutes == 0) {
            returnValue = "00:00";
        }
//        Log.i(TAG, "waste download time returnValue:" + returnValue);
        return returnValue;
    }

    private void connectToAMBA() {
        try {
            droneController = ((MainActivity) getActivity()).getDroneController();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "fileName:" + fileName);
        droneController.downloadMedia(albumFilePath, fileName, new DroneController.OnProgressUpdatedListener() {
            @Override
            public void onProgressUpdated(final long downloadedSize, final long totalSize) {

                getFileSize = downloadedSize;
                totalFileSize = totalSize;

                final int progressValue = (int) (downloadedSize * 100 / totalSize);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progressValue);
                    }
                });
            }

            @Override
            public void onCompleted(long size) {
                Log.i(TAG, "download image onCompleted");

                ArrayList<String> toBeScanned = new ArrayList<String>();
                toBeScanned.add(albumFilePath + fileName);
                String[] toBeScannedStr = new String[toBeScanned.size()];
                toBeScannedStr = toBeScanned.toArray(toBeScannedStr);
                MediaScannerConnection.scanFile(context, toBeScannedStr, null, null);

                if (IS_DOWNLOAD_FINISHED_DELETE_FILE) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            deleteAMMBAFile();
                        }
                    }).start();
                } else {
                    closeDownloadFragment();
                }
            }
        });

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (IS_DOWNLOAD_FINISHED_DELETE_FILE) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
        }
    }
}
