package com.coretronic.drone.album;

import android.content.Context;
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
import android.widget.*;
import com.coretronic.drone.R;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.ambarlla.message.AMBACmdClient;
import com.coretronic.drone.ambarlla.message.AMBACommand;
import com.coretronic.drone.ambarlla.message.FileItem;
import com.coretronic.drone.log.ColorLog;
import com.coretronic.drone.utility.AppConfig;

import java.io.IOException;
import java.util.List;


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

    Handler progressHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            progressValue++;
            downloadBar.setProgress(progressValue);
            if( progressValue == 100)
            {
                closeDownloadFragment();
            }
        }
    };


    // AMBAClient
    AMBACmdClient cmdClient = null;
    private String albumFilePath = "";


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
        albumFilePath = context.getExternalCacheDir()+AppConfig.ALBUM_PATH_SD_CARD;

        // get media list data
        Bundle bundle = getArguments();
        if (bundle == null) {
            return view;
        }
        mediaListItem = (MediaListItem) bundle.getSerializable("mediaListItemData");
        Log.i(TAG, "file name:" + mediaListItem.getMediaFileName());
        Log.i(TAG, "file size:" + mediaListItem.getMediaSize());
        Log.i(TAG, "file date:" + mediaListItem.getMediaDate());


        findViews(view);

//        if( mediaListItem.getMediaSize())
        // set if warning
        wraningLL.setVisibility(View.VISIBLE);
//        wraningLL.setVisibility(View.GONE);

        downloadBar.setProgress(progressValue);

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                connectToAMBA();
                while(progressValue < 100 ){
                    try{
                        progressHandler.sendMessage(progressHandler.obtainMessage());
                        Thread.sleep(1000);
                    }catch (Throwable t)
                    {

                    }
                }
            }
        };
        progressThread = new Thread(progressRunnable);
        progressThread.start();

        return view;
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
        cmdClient.close();
    }

    View.OnClickListener stopbtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "stop btn click listener");
            closeDownloadFragment();
        }
    };

    private void closeDownloadFragment()
    {
        if( progressThread != null && progressThread.isAlive() )
        {
            progressThread.interrupt();
            progressThread = null;
        }

        fragmentManager.popBackStack();
//        AlbumFragment albumFragment = new AlbumFragment();
//        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_view, albumFragment).commit();
    }

    private void connectToAMBA() {
        cmdClient = new AMBACmdClient();

        AMBACmdClient.ClientNotifer errReceiver = new AMBACmdClient.ClientNotifer(){

            @Override
            public void onNotify(int status, String strMsg) {
                // 0 is error, 1 is ok
                if( status == 0 )
                {
                    cmdClient.close();
                }
            }
        };


        AMBACmdClient.CmdReceiver cmdReceiver = new AMBACmdClient.CmdReceiver() {
            @Override
            public void onMessage(AMBACommand objMessage) {
//                objMessage.toLog();
                Log.i(TAG,"objMessage:"+objMessage);
            }

        };


        AMBACmdClient.CmdListFileReceiver cmdListFileReceiver = new AMBACmdClient.CmdListFileReceiver() {
            @Override
            public void onCompleted(List<FileItem> listItems) {

            }
        };


        try {
            cmdClient.connectToServer(AppConfig.SERVER_IP, AppConfig.COMMAND_PORT, AppConfig.DATA_PORT,errReceiver);
            cmdClient.setFileSavePath(albumFilePath);
            cmdClient.start();
            cmdClient.cmdStartSession();
            Log.i(TAG,"mediaListItem.getMediaFileName():" + mediaListItem.getMediaFileName());
            cmdClient.cmdGetFile(mediaListItem.getMediaFileName(), new AMBACmdClient.GetFileListener() {
                @Override
                public void onProgress(float downloadPercentage) {
                    ColorLog.debug("Progress:" + downloadPercentage);
                    Log.i(TAG,"Progress:" + downloadPercentage);
                }

                @Override
                public void onCompleted(long size) {
                    Log.i(TAG,"downlaod image onCompleted");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            cmdClient.close();
            Log.e(TAG, "connect error:" + e.getMessage());
        }


    }

}
