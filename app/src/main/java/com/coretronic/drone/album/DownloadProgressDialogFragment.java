package com.coretronic.drone.album;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

    private static String TAG = DownloadProgressDialogFragment.class.getSimpleName();
    private Context context = null;

    private static final String ARGUMENT_FILENAME = "filename";
    private static final String ARGUMENT_FILESIZE = "filesize";
    String fileName = "";
    String fileSize = "";

    private LinearLayout wraningLL = null;
    private TextView timeTV = null;
    private TextView stopBtn = null;
    private ProgressBar downloadBar = null;

    // progress bar events declare
    private Thread progressThread = null;
    private Runnable progressRunnable = null;
    private int progressValue = 0;
    private long totalFileSize = 0;
    private long getFileSize = 0;

    // speed thread and timer
    private Timer speedTimer = null;
    private static int INTERVAL_DOWNLOAD_TIME = 1000;
    private float intervalCalculateSum = 0;
    DroneController droneController = null;
    private String albumFilePath = "";

    Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressValue = msg.arg1;
            downloadBar.setProgress(progressValue);
            Log.i(TAG, "progressValue:" + progressValue);
            if (progressValue == 100) {
                Log.i(TAG, " progressValue == 100");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        deleteAMMBAFile();
                    }
                }).start();
//                closeDownloadFragment();
            }
        }
    };


    public Handler timeHandler = new Handler() {
        public void handleMessage(Message msg) {
            int wasteTime = msg.what;
            timeTV.setText(millisecondsToHumanRead((int) wasteTime));
        }
    };


    public Handler deleteCompletedHandler = new Handler() {
        public void handleMessage(Message msg) {
            closeDownloadFragment();
        }
    };

    public static DownloadProgressDialogFragment newInstance(MediaListItem mediaListItem) {
        DownloadProgressDialogFragment df = new DownloadProgressDialogFragment();
        Bundle args = new Bundle();
        Log.i(TAG, "putString fileName:" + mediaListItem.getMediaFileName());
        Log.i(TAG, "putString fileSize:" + mediaListItem.getMediaSize());
        args.putString(ARGUMENT_FILENAME, mediaListItem.getMediaFileName());
        args.putString(ARGUMENT_FILESIZE, mediaListItem.getMediaSize());
        df.setArguments(args);

        return df;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        final Dialog dialog = new Dialog(getActivity());
        context = dialog.getContext();
        albumFilePath = AppConfig.getMediaFolderPosition(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.fragment_album_warning);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        wraningLL = (LinearLayout) dialog.findViewById(R.id.wraning_ll);
        timeTV = (TextView) dialog.findViewById(R.id.timetv);
        stopBtn = (TextView) dialog.findViewById(R.id.stopdownload_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDownloadFragment();
            }
        });
        downloadBar = (ProgressBar) dialog.findViewById(R.id.downloadbar);
        dialog.show();

        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();

        if (arguments != null) {
            Log.i(TAG, "arguments!=null");
            fileName = arguments.getString(ARGUMENT_FILENAME);
            fileSize = arguments.getString(ARGUMENT_FILESIZE);


            Log.i(TAG, "fileName:" + fileName);
            Log.i(TAG, "fileSize:" + fileSize);

            int fileSizeNumber = Integer.valueOf((fileSize.split(" bytes"))[0]);

            // if > 50MB show warning
            if (fileSizeNumber > 50 * 1000 * 1000) {
                // set if warning
                wraningLL.setVisibility(View.VISIBLE);

            } else {
                wraningLL.setVisibility(View.GONE);
            }
        }

        downloadBar.setProgress(progressValue);

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                connectToAMBA();
            }
        };
        progressThread = new Thread(progressRunnable);
        progressThread.start();

        speedTimer = new Timer();
        intervalCalculateSum = 0;
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
        DroneController.MediaCommandListener droneDeleFileReceiver = new DroneController.MediaCommandListener() {
            @Override
            public void onCommandResult(MediaCommand mediaCommand, boolean isSuccess, Object data) {
                if (MediaCommand.REMOVE_CONTENT == mediaCommand) {
                    Log.i(TAG, "delete file completed");
                    deleteCompletedHandler.sendMessage(deleteCompletedHandler.obtainMessage());
                }
            }
        };
        droneController.removeContent(fileName, droneDeleFileReceiver);

        droneController.getMediaContents(new DroneController.MediaCommandListener() {
            @Override
            public void onCommandResult(MediaCommand mediaCommand, boolean isSuccess, Object data) {
                if (MediaCommand.LIST_CONTENTS == mediaCommand) {
                    closeDownloadFragment();
                }
            }
        });
    }

    class SpeedTimerTask extends java.util.TimerTask {

        @Override
        public void run() {
            Log.i(TAG, "getFileSize:" + getFileSize + "/totalFileSize:" + totalFileSize);

            intervalCalculateSum++;
            if (getFileSize != 0 && totalFileSize != 0) {


                float wasteTime = ((float) (totalFileSize - getFileSize) / ((float) getFileSize / (INTERVAL_DOWNLOAD_TIME * intervalCalculateSum)));


                Message msg = Message.obtain();
                msg.what = (int) wasteTime;
                timeHandler.sendMessage(msg);
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
            public void onProgressUpdated(long downloadedSize, long totalSize) {
                Log.i(TAG, "downloadedSize / fileSize / 100/(int):" + downloadedSize + "/" + totalSize + "/" + (downloadedSize / totalSize) + "/" + (int) (downloadedSize * 100 / totalSize));

                getFileSize = downloadedSize;
                totalFileSize = totalSize;

                Message msg = Message.obtain();
                msg.arg1 = (int) (downloadedSize * 100 / totalSize);
                Log.i(TAG, "msg.arg1:" + msg.arg1);
                progressHandler.sendMessage(msg);
            }

            @Override
            public void onCompleted(long size) {
                Log.i(TAG, "downlaod image onCompleted");

                ArrayList<String> toBeScanned = new ArrayList<String>();
                toBeScanned.add(albumFilePath + fileName);
                String[] toBeScannedStr = new String[toBeScanned.size()];
                toBeScannedStr = toBeScanned.toArray(toBeScannedStr);
                MediaScannerConnection.scanFile(context, toBeScannedStr, null, null);
            }
        });

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
    }
}
