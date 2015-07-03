package com.coretronic.drone.album;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * Created by james on 15/6/18.
 */
public class DownloadWarningFragment extends Fragment {

    private static String TAG = DownloadWarningFragment.class.getSimpleName();
    private Context context = null;
    MediaListItem mediaListItem = null;

    // fragment declare
    private FragmentManager fragmentManager = null;
    private FragmentTransaction previewFragmentTransaction = null;
    private FragmentActivity fragmentActivity = null;
    // ui element declare
    private LinearLayout wraningLL = null;
    private TextView timeTV = null;
    private Button stopBtn = null;

    // progress bar events declare
    private Thread progressThread = null;
    private Runnable progressRunnable = null;
    private ProgressBar downloadBar = null;
    private int progressValue = 0;
    private long totalFileSize = 0;
    private long getFileSize = 0;

    // speed thread and timer
    private Timer speedTimer = null;
    private static int INTERVAL_DOWNLOAD_TIME = 1000;
    private float intervalCalculateSum = 0;
    // AMBAClient
//    AMBACmdClient cmdClient = null;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = getActivity();
        fragmentManager = getFragmentManager();

        previewFragmentTransaction = fragmentManager.beginTransaction();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_warning, container, false);
        context = view.getContext();
        albumFilePath = AppConfig.getMediaFolderPosition(context);

        // get media list data
        Bundle bundle = getArguments();
        Log.i(TAG, "get bundle:" + bundle);
        if (bundle == null) {
            return view;
        }
        mediaListItem = (MediaListItem) bundle.getSerializable("mediaListItemData");
        Log.i(TAG, "file name:" + mediaListItem.getMediaFileName());
        Log.i(TAG, "file size:" + mediaListItem.getMediaSize());
        Log.i(TAG, "file date:" + mediaListItem.getMediaDate());


        findViews(view);

        int fileSizeNumber = Integer.valueOf((mediaListItem.getMediaSize().split(" bytes"))[0]);

        // if > 50MB show warning
        if (fileSizeNumber > 50 * 1000 * 1000) {
            // set if warning
            wraningLL.setVisibility(View.VISIBLE);

        } else {
            wraningLL.setVisibility(View.GONE);
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

        return view;
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
        int minutes = (int) ((mills / (1000 * 60)) % 60);
        int hours = (int) ((mills / (1000 * 60 * 60)) % 24);
        Log.i(TAG, "mills:" + mills + " /seconds:" + seconds + " /minutes:" + minutes + " /hours:" + hours);
        String returnValue = "calculate the download time..";
        if (seconds >= 0) {
            returnValue = seconds + " seconds";
        } else if (minutes > 0) {
            returnValue = seconds + " minutes" + seconds + " seconds";
        } else if (hours > 0) {
            returnValue = hours + " hours" + seconds + " minutes" + seconds + " seconds";
        }
        if (seconds == 0 && seconds == 0 && hours == 0) {
            returnValue = "calculate the download time..";
        }
        Log.i(TAG, "waste download time returnValue:" + returnValue);
        return returnValue;
    }

    private void findViews(View view) {
        wraningLL = (LinearLayout) view.findViewById(R.id.wraning_ll);
        timeTV = (TextView) view.findViewById(R.id.timetv);
        stopBtn = (Button) view.findViewById(R.id.stopdownload_btn);
        stopBtn.setOnClickListener(stopbtnListner);
        downloadBar = (ProgressBar) view.findViewById(R.id.downloadbar);

    }


    @Override
    public void onPause() {
        super.onPause();
//        fragmentManager.popBackStack();
        Log.i(TAG, "==== on pause ====");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "==== onResume ====");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "==== onDestroyView ====");
//        cmdClient.close();
    }

    View.OnClickListener stopbtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "stop btn click listener");
            closeDownloadFragment();
        }
    };

    private void deleteAMMBAFile() {
        Log.i(TAG, "dele AMMBA File Name:" + mediaListItem.getMediaFileName());

//        AMBACmdClient.DeleteFileListener cmdDeleFileReceiver = new AMBACmdClient.DeleteFileListener() {
//
//            @Override
//            public void onCompleted(boolean blSuccess) {
//                Log.i(TAG, "delete file completed");
//                deleteCompletedHandler.sendMessage(deleteCompletedHandler.obtainMessage());
//            }
//        };
//
//        cmdClient.deleteFile(mediaListItem.getMediaFileName(), cmdDeleFileReceiver);

        DroneController.MediaCommandListener droneDeleFileReceiver = new DroneController.MediaCommandListener() {
            @Override
            public void onCommandResult(MediaCommand mediaCommand, boolean isSuccess, Object data) {
                if (MediaCommand.REMOVE_CONTENT == mediaCommand) {
                    Log.i(TAG, "delete file completed");
                    deleteCompletedHandler.sendMessage(deleteCompletedHandler.obtainMessage());
                }
            }
        };
        droneController.removeContent(mediaListItem.getMediaFileName(), droneDeleFileReceiver);

//        AMBACmdClient.listFileReceiver cmdListFileReceiver = new AMBACmdClient.listFileReceiver() {
//            @Override
//            public void onCompleted(List<FileItem> listItems) {
//                closeDownloadFragment();
//            }
//        };
        droneController.getMediaContents(new DroneController.MediaCommandListener() {
            @Override
            public void onCommandResult(MediaCommand mediaCommand, boolean isSuccess, Object data) {
                if (MediaCommand.LIST_CONTENTS == mediaCommand) {
                    closeDownloadFragment();
                }
            }
        });
    }

    private void closeDownloadFragment() {


        if (progressThread != null && progressThread.isAlive()) {
            progressThread.interrupt();
            progressThread = null;
        }
        speedTimer.cancel();
        fragmentManager.popBackStack();
//        AlbumFragment albumFragment = new AlbumFragment();
//        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_view, albumFragment).commit();
    }

    private void connectToAMBA() {
        try {
//            cmdClient = new AMBACmdClient();
            droneController = ((MainActivity) getActivity()).getDroneController();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        AMBACmdClient.ClientNotifer errReceiver = new AMBACmdClient.ClientNotifer() {
//
//            @Override
//            public void onNotify(int status, String strMsg) {
//                // 0 is error, 1 is ok
//                if (status == 0) {
//                    cmdClient.close();
//                }
//            }
//        };


//        AMBACmdClient.CmdReceiver cmdReceiver = new AMBACmdClient.CmdReceiver() {
//            @Override
//            public void onMessage(AMBACommand objMessage) {
//                Log.i(TAG, "objMessage:" + objMessage);
//            }
//
//        };


//        AMBACmdClient.listFileReceiver cmdListFileReceiver = new AMBACmdClient.listFileReceiver() {
//            @Override
//            public void onCompleted(List<FileItem> listItems) {
//
//            }
//        };


//        try {
//            Boolean connectStatus = cmdClient.connectToServer(AppConfig.SERVER_IP, AppConfig.COMMAND_PORT, AppConfig.DATA_PORT, errReceiver);
//            Boolean connectStatus = cmdClient.connectToServer(AppConfig.SERVER_IP, AppConfig.COMMAND_PORT, AppConfig.DATA_PORT);

//            if (!connectStatus) {
//                cmdClient.close();
//                return;
//            }

//            cmdClient.setFileSavePath(albumFilePath);
//            cmdClient.start();
//            cmdClient.cmdStartSession(new AMBACmdClient.SessionListener() {
//                @Override
//                public void onStartSession(boolean Success) {
//
//                }
//            });

        Log.i(TAG, "mediaListItem.getMediaFileName():" + mediaListItem.getMediaFileName());
//            cmdClient.getFile(mediaListItem.getMediaFileName(), new AMBACmdClient.GetFileListener() {
//
//                @Override
//                public void onProgress(long downloadedSize, long fileSize) {
//                    Log.i(TAG, "downloadedSize / fileSize / 100/(int):" + downloadedSize + "/" + fileSize + "/" + (downloadedSize / fileSize) + "/" + (int) (downloadedSize * 100 / fileSize));
//
//                    getFileSize = downloadedSize;
//                    totalFileSize = fileSize;
//
//                    Message msg = Message.obtain();
//                    msg.arg1 = (int) (downloadedSize * 100 / fileSize);
//                    Log.i(TAG, "msg.arg1:" + msg.arg1);
//                    progressHandler.sendMessage(msg);
//                }
//
//                @Override
//                public void onCompleted(long size) {
//                    Log.i(TAG, "downlaod image onCompleted");
//
//                    ArrayList<String> toBeScanned = new ArrayList<String>();
//                    toBeScanned.add(albumFilePath + mediaListItem.getMediaFileName());
//                    String[] toBeScannedStr = new String[toBeScanned.size()];
//                    toBeScannedStr = toBeScanned.toArray(toBeScannedStr);
//                    MediaScannerConnection.scanFile(getActivity(), toBeScannedStr, null, new MediaScannerConnection.OnScanCompletedListener() {
//
//                        @Override
//                        public void onScanCompleted(String path, Uri uri) {
//                            System.out.println("SCAN COMPLETED: " + path);
//
//                        }
//                    });
//                }
//            });
        droneController.downloadMedia(albumFilePath, mediaListItem.getMediaFileName(), new DroneController.OnProgressUpdatedListener() {
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
                toBeScanned.add(albumFilePath + mediaListItem.getMediaFileName());
                String[] toBeScannedStr = new String[toBeScanned.size()];
                toBeScannedStr = toBeScanned.toArray(toBeScannedStr);
                MediaScannerConnection.scanFile(context, toBeScannedStr, null, null);
            }
        });
//        } catch (IOException e) {
//            e.printStackTrace();
//            cmdClient.close();
//            Log.e(TAG, "connect error:" + e.getMessage());
//        }


    }

}
