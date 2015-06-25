package com.coretronic.drone.album;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.coretronic.drone.R;
import com.coretronic.drone.album.adapter.AlbumListViewAdapter;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.ambarlla.message.AMBACmdClient;
import com.coretronic.drone.ambarlla.message.AMBACommand;
import com.coretronic.drone.ambarlla.message.FileItem;
import com.coretronic.drone.log.ColorLog;
import com.coretronic.drone.utility.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 15/6/1.
 */
public class AlbumDroneTagFragment extends Fragment {

    private static String TAG = AlbumDroneTagFragment.class.getSimpleName();

    private Context mContext = null;
    private ProgressBar progressbar = null;
    private RecyclerView albumListView = null;
    private AlbumListViewAdapter albumListViewAdapter = null;
    private ArrayList<MediaListItem> albumImgList = new ArrayList<MediaListItem>();
    private String albumFilePath = "";

    private TextView notFindListTV = null;
    private Thread connectThread = null;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.obj == "complete") {
                Log.i(TAG, "handler message:" + msg.obj);
                albumListViewAdapter.notifyDataSetChanged();
                albumListView.setVisibility(View.VISIBLE);
            }

            progressbar.setVisibility(View.GONE);
//            connectThread.interrupt();
//            cmdClient.close();

        }
    };

    Handler processUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    progressbar.setVisibility(View.GONE);
                    notFindListTV.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    progressbar.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    // AMBAClient
    AMBACmdClient cmdClient = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "=== AlbumDroneTagFragment onCreateView===");
        View view = inflater.inflate(R.layout.fragment_album_dronetag, container, false);
        mContext = view.getContext();

        albumFilePath = AppConfig.getMediaFolderPosition(mContext);

        progressbar = (ProgressBar) view.findViewById(R.id.progressbar);
        albumListView = (RecyclerView) view.findViewById(R.id.album_list_view);
        notFindListTV = (TextView) view.findViewById(R.id.not_get_list_tv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        albumListView.setLayoutManager(linearLayoutManager);

        Log.i(TAG, "albumFilePath:" + albumFilePath);
        File albumFolder = new File(albumFilePath);
        if (!albumFolder.exists()) {
            albumFolder.mkdir();
        }


        albumListViewAdapter = new AlbumListViewAdapter(mContext, albumImgList);
        albumListView.setAdapter(albumListViewAdapter);
        albumListViewAdapter.SetOnItemClickListener(recyclerItemClickListener);

        progressbar.setVisibility(View.VISIBLE);

        connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                connectToAMBA();
            }
        });
        connectThread.start();

        return view;
    }


    AlbumListViewAdapter.OnItemClickListener recyclerItemClickListener = new AlbumListViewAdapter.OnItemClickListener() {

        @Override
        public void onItemDeleteClick(View view, final int position) {
            Log.i(TAG, "delete:" + position);


            new Thread(new Runnable() {
                @Override
                public void run() {

                    AMBACmdClient.DeleteFileListener cmdDeleFileReceiver = new AMBACmdClient.DeleteFileListener() {

                        @Override
                        public void onCompleted(boolean blSuccess) {
                            Log.i(TAG, "delete file completed");
                            Toast.makeText(mContext, "delete file completed", Toast.LENGTH_LONG).show();
                        }
                    };

                    cmdClient.cmdDeleteFile(albumImgList.get(position).getMediaFileName(), cmdDeleFileReceiver);


                    AMBACmdClient.CmdListFileReceiver cmdListFileReceiver = new AMBACmdClient.CmdListFileReceiver() {
                        @Override
                        public void onCompleted(List<FileItem> listItems) {
                            getDate(listItems);

                        }
                    };
                    cmdClient.getFileList(cmdListFileReceiver);
                }
            }).start();

        }

        @Override
        public void onDownloadClick(View view, int position) {
            Log.i(TAG, "download:" + position);

            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaListItemData", albumImgList.get(position));

            Toast.makeText(mContext, "download " + view.getTag(), Toast.LENGTH_SHORT).show();
            DownloadWarningFragment downloadWarningFragment = new DownloadWarningFragment();
            downloadWarningFragment.setArguments(bundle);

            Fragment cuttentFragment = getActivity().getSupportFragmentManager().findFragmentByTag("fragment");
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .hide(cuttentFragment)
                    .replace(R.id.frame_view, downloadWarningFragment, "DownloadWarningFragment")
                    .addToBackStack("DownloadWarningFragment")
                    .commit();

            connectThread.interrupt();
        }
    };

    private void getDate(List<FileItem> listItems) {
        albumImgList.clear();

        for (int i = 0; i < listItems.size(); i++) {
            albumImgList.add(new MediaListItem(listItems.get(i)));
        }

        Message message = new Message();
        message = handler.obtainMessage(0, "complete");
        handler.sendMessage(message);

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "album drone tag fragment onDestroyView");
        if (cmdClient != null) {
            cmdClient.close();
        }

        if (connectThread != null && !connectThread.isInterrupted()) {
            connectThread.interrupt();
            connectThread = null;
        }
    }


    private void connectToAMBA() {
        Log.i(TAG, "connectToAMBA");
        cmdClient = new AMBACmdClient();

        AMBACmdClient.ClientNotifer errReceiver = new AMBACmdClient.ClientNotifer() {

            @Override
            public void onNotify(int status, String strMsg) {
                Log.i(TAG, "on Notify status:" + status + " / strMsg:" + strMsg);
                // 0 is error, 1 is ok
                if (status == 0) {
                    cmdClient.close();
                    processUIHandler.sendEmptyMessage(0);
                }
            }
        };

        AMBACmdClient.CmdReceiver cmdReceiver = new AMBACmdClient.CmdReceiver() {
            @Override
            public void onMessage(AMBACommand objMessage) {
//                objMessage.toLog();
                Log.i(TAG, "objMessage:" + objMessage);
            }

        };


        AMBACmdClient.CmdListFileReceiver cmdListFileReceiver = new AMBACmdClient.CmdListFileReceiver() {
            @Override
            public void onCompleted(List<FileItem> listItems) {
                getDate(listItems);

            }
        };


        try {

            Boolean connectStatus = cmdClient.connectToServer(AppConfig.SERVER_IP, AppConfig.COMMAND_PORT, AppConfig.DATA_PORT, errReceiver);
            Log.i(TAG, "connectStatus:" + connectStatus);

            if (!connectStatus) {
                connectThread.interrupt();
                connectThread = null;
                return;
            }

            if (cmdClient.isRun) {
                cmdClient.setFileSavePath(albumFilePath);
                cmdClient.start();
                cmdClient.cmdStartSession();
                cmdClient.getFileList(cmdListFileReceiver);
            } else {
                cmdClient.close();
                progressbar.setVisibility(View.GONE);
            }


        } catch (IOException e) {
            e.printStackTrace();
            if (cmdClient != null) {
                cmdClient.close();
            }
//            processUIHandler.sendEmptyMessage(0);

            Log.e(TAG, "connect error:" + e.getMessage());
        }


    }

}
